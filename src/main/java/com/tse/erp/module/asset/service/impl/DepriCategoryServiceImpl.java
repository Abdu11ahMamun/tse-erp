package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.DepriCategory;
import com.tse.erp.module.asset.repository.DepriCategoryRepository;
import com.tse.erp.module.asset.service.DepriCategoryService;
import com.tse.erp.module.common.entity.LookupValue;
import com.tse.erp.module.common.repository.LookupValueRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.repository.DepartmentGroupRepository;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepriCategoryServiceImpl implements DepriCategoryService {

    private final DepriCategoryRepository depriCategoryRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final LookupValueRepository lookupValueRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentGroupRepository departmentGroupRepository;

    @Override
    public List<DepriCategory> getAllDepriCategories(Long buId) {
        if (buId == null) {
            return depriCategoryRepository.findAllByOrderByIdDesc();
        }
        return depriCategoryRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public DepriCategory getDepriCategoryById(Long id) {
        return depriCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depreciation Category not found with id: " + id));
    }

    @Override
    public DepriCategory createDepriCategory(DepriCategory d) {
        validateParents(d.getBgId(), d.getBuId());
        validateCategoryName(d.getDepriCategory());
        validateLookupValue(d.getDepriMethodId(), d.getBuId(),
                "Depreciation method");
        validateLookupValue(d.getAssetTypeId(), d.getBuId(), "Asset type");
        validateLife(d.getDepriLife());
        validatePercentage(d.getDepriPercentage());
        validateOwner(d.getAssetOwnerId(), d.getBgId(), d.getBuId());
        StatusUtil.validate(d.getStatus());

        String name = d.getDepriCategory().trim();
        boolean exists = !depriCategoryRepository
                .findByBuIdAndDepriCategoryIgnoreCaseAndDepriMethodIdAndAssetTypeIdAndAssetOwnerId(
                        d.getBuId(), name, d.getDepriMethodId(),
                        d.getAssetTypeId(), d.getAssetOwnerId())
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Depreciation category already exists with same method, asset type and owner: "
                            + name);
        }

        d.setId(null);
        d.setDepriCategory(name);
        return depriCategoryRepository.save(d);
    }

    @Override
    public DepriCategory updateDepriCategory(Long id, DepriCategory d) {
        DepriCategory existing = getDepriCategoryById(id);

        validateParents(d.getBgId(), d.getBuId());
        validateCategoryName(d.getDepriCategory());
        validateLookupValue(d.getDepriMethodId(), d.getBuId(),
                "Depreciation method");
        validateLookupValue(d.getAssetTypeId(), d.getBuId(), "Asset type");
        validateLife(d.getDepriLife());
        validatePercentage(d.getDepriPercentage());
        validateOwner(d.getAssetOwnerId(), d.getBgId(), d.getBuId());
        StatusUtil.validate(d.getStatus());

        String name = d.getDepriCategory().trim();
        boolean duplicateExists = depriCategoryRepository
                .findByBuIdAndDepriCategoryIgnoreCaseAndDepriMethodIdAndAssetTypeIdAndAssetOwnerId(
                        d.getBuId(), name, d.getDepriMethodId(),
                        d.getAssetTypeId(), d.getAssetOwnerId())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Depreciation category already exists with same method, asset type and owner: "
                            + name);
        }

        existing.setBgId(d.getBgId());
        existing.setBuId(d.getBuId());
        existing.setDepriCategory(name);
        existing.setDepriMethodId(d.getDepriMethodId());
        existing.setAssetTypeId(d.getAssetTypeId());
        existing.setDepriLife(d.getDepriLife());
        existing.setDepriPercentage(d.getDepriPercentage());
        existing.setAssetOwnerId(d.getAssetOwnerId());
        if (d.getStatus() != null) {
            existing.setStatus(d.getStatus());
        }

        return depriCategoryRepository.save(existing);
    }

    @Override
    public void deleteDepriCategory(Long id) {
        DepriCategory existing = getDepriCategoryById(id);
        depriCategoryRepository.delete(existing);
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }
    }

    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException(
                    "Depreciation category cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Depreciation category must be at least 2 characters");
        }
        if (name.trim().length() > 100) {
            throw new BadRequestException(
                    "Depreciation category cannot exceed 100 characters");
        }
    }

    private void validateLife(Integer life) {
        if (life == null) {
            throw new BadRequestException(
                    "Depreciation life cannot be empty");
        }
        if (life < 1) {
            throw new BadRequestException(
                    "Depreciation life must be at least 1 month");
        }
    }

    private void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new BadRequestException(
                    "Depreciation percentage cannot be empty");
        }
        if (percentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Depreciation percentage must be greater than 0");
        }
        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException(
                    "Depreciation percentage cannot exceed 100");
        }
    }

    private void validateLookupValue(Long valueId, Long buId, String label) {
        if (valueId == null) {
            throw new BadRequestException(label + " cannot be empty");
        }

        LookupValue lookupValue = lookupValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " not found with id: " + valueId));

        if (!Integer.valueOf(1).equals(lookupValue.getStatus())) {
            throw new BadRequestException(
                    "Selected " + label + " is not active");
        }
        if (!buId.equals(lookupValue.getBuId())) {
            throw new BadRequestException(
                    label + " does not belong to selected Business Unit");
        }
    }

    private void validateOwner(Long ownerId, Long bgId, Long buId) {
        if (ownerId == null) {
            throw new BadRequestException("Asset owner cannot be empty");
        }

        Department department = departmentRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + ownerId));

        if (!Integer.valueOf(1).equals(department.getStatus())) {
            throw new BadRequestException(
                    "Selected Department is not active");
        }
        if (!bgId.equals(department.getBgId())) {
            throw new BadRequestException(
                    "Department does not belong to selected Business Group");
        }
        if (departmentGroupRepository
                .findByDeptIdAndBuId(ownerId, buId).isEmpty()) {
            throw new BadRequestException(
                    "Department is not mapped to selected Business Unit");
        }
    }
}
