package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetGroup;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetGroupRepository;
import com.tse.erp.module.asset.service.AssetGroupService;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetGroupServiceImpl implements AssetGroupService {

    private final AssetGroupRepository assetGroupRepository;
    private final AssetItemRepository assetItemRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<AssetGroup> getAllAssetGroups(Long buId) {
        if (buId == null) {
            return assetGroupRepository.findAllByOrderByIdDesc();
        }
        return assetGroupRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public AssetGroup getAssetGroupById(Long id) {
        return assetGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Group not found with id: " + id));
    }

    @Override
    public AssetGroup createAssetGroup(AssetGroup assetGroup) {
        validateParents(assetGroup.getBgId(), assetGroup.getBuId());
        validateAssetGroupName(assetGroup.getAssetGroup());
        validateOwner(assetGroup.getAssetOwnerId(), assetGroup.getBgId());
        StatusUtil.validate(assetGroup.getStatus());

        String name = assetGroup.getAssetGroup().trim();
        boolean exists = !assetGroupRepository
                .findByBuIdAndAssetGroupIgnoreCaseAndAssetOwnerId(
                        assetGroup.getBuId(), name,
                        assetGroup.getAssetOwnerId())
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Asset Group already exists for this owner: " + name);
        }

        assetGroup.setId(null);
        assetGroup.setAssetGroup(name);

        return assetGroupRepository.save(assetGroup);
    }

    @Override
    public AssetGroup updateAssetGroup(Long id, AssetGroup assetGroup) {
        AssetGroup existing = getAssetGroupById(id);

        validateParents(assetGroup.getBgId(), assetGroup.getBuId());
        validateAssetGroupName(assetGroup.getAssetGroup());
        validateOwner(assetGroup.getAssetOwnerId(), assetGroup.getBgId());
        StatusUtil.validate(assetGroup.getStatus());

        String name = assetGroup.getAssetGroup().trim();
        boolean duplicateExists = assetGroupRepository
                .findByBuIdAndAssetGroupIgnoreCaseAndAssetOwnerId(
                        assetGroup.getBuId(), name,
                        assetGroup.getAssetOwnerId())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Asset Group already exists for this owner: " + name);
        }

        existing.setBgId(assetGroup.getBgId());
        existing.setBuId(assetGroup.getBuId());
        existing.setAssetGroup(name);
        existing.setAssetOwnerId(assetGroup.getAssetOwnerId());
        if (assetGroup.getStatus() != null) {
            existing.setStatus(assetGroup.getStatus());
        }

        return assetGroupRepository.save(existing);
    }

    @Override
    public void deleteAssetGroup(Long id) {
        AssetGroup existing = getAssetGroupById(id);
        if (assetItemRepository.existsByItemGroupId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Asset Items exist under this Asset Group");
        }
        assetGroupRepository.delete(existing);
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

    private void validateAssetGroupName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Asset group cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Asset group must be at least 2 characters");
        }
        if (name.trim().length() > 100) {
            throw new BadRequestException(
                    "Asset group cannot exceed 100 characters");
        }
    }

    private void validateOwner(Long ownerId, Long bgId) {
        if (ownerId == null) {
            throw new BadRequestException(
                    "Asset owner cannot be empty");
        }

        Department dept = departmentRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + ownerId));

        if (!Integer.valueOf(1).equals(dept.getStatus())) {
            throw new BadRequestException(
                    "Selected Department is not active");
        }

        if (!bgId.equals(dept.getBgId())) {
            throw new BadRequestException(
                    "Department does not belong to selected Business Group");
        }
    }
}