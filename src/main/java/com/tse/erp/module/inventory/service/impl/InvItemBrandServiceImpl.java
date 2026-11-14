package com.tse.erp.module.inventory.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemBrandRequestDto;
import com.tse.erp.module.inventory.dto.InvItemBrandResponseDto;
import com.tse.erp.module.inventory.entity.InvItemBrand;
import com.tse.erp.module.inventory.repository.InvItemBrandRepository;
import com.tse.erp.module.inventory.service.InvItemBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvItemBrandServiceImpl implements InvItemBrandService {

    private final InvItemBrandRepository itemBrandRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<InvItemBrandResponseDto> getAllItemBrands(
            Long bgId, Long buId, String brandName, String brandCode, Integer status) {
        return itemBrandRepository.searchItemBrands(bgId, buId, brandName, brandCode, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvItemBrandResponseDto getItemBrandById(Long id) {
        return toResponse(findBrand(id));
    }

    @Override
    public String generateNextBrandCode(Long buId) {
        if (buId == null) {
            throw new BadRequestException("Business Unit ID is required to generate next Brand Code");
        }
        long count = itemBrandRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("BRD-%05d", next);
        while (itemBrandRepository.existsByBuIdAndBrandCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("BRD-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public InvItemBrandResponseDto createItemBrand(InvItemBrandRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getBrandName() == null || request.getBrandName().trim().isEmpty()) {
            throw new BadRequestException("Brand Name is required");
        }
        String brandName = request.getBrandName().trim();
        if (brandName.length() > 150) {
            throw new BadRequestException("Brand Name cannot exceed 150 characters");
        }

        if (itemBrandRepository.existsByBuIdAndBrandNameIgnoreCase(request.getBuId(), brandName)) {
            throw new DuplicateResourceException("Brand Name '" + brandName + "' already exists for this Business Unit");
        }

        String brandCode = request.getBrandCode();
        if (brandCode == null || brandCode.trim().isEmpty()) {
            brandCode = generateNextBrandCode(request.getBuId());
        } else {
            brandCode = brandCode.trim();
            if (brandCode.length() > 50) {
                throw new BadRequestException("Brand Code cannot exceed 50 characters");
            }
            if (itemBrandRepository.existsByBuIdAndBrandCodeIgnoreCase(request.getBuId(), brandCode)) {
                throw new DuplicateResourceException("Brand Code '" + brandCode + "' already exists for this Business Unit");
            }
        }

        StatusUtil.validate(request.getStatus());

        InvItemBrand entity = InvItemBrand.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .brandName(brandName)
                .brandCode(brandCode)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toResponse(itemBrandRepository.save(entity));
    }

    @Override
    @Transactional
    public InvItemBrandResponseDto updateItemBrand(Long id, InvItemBrandRequestDto request) {
        InvItemBrand existing = findBrand(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getBrandName() == null || request.getBrandName().trim().isEmpty()) {
            throw new BadRequestException("Brand Name is required");
        }
        String brandName = request.getBrandName().trim();
        if (brandName.length() > 150) {
            throw new BadRequestException("Brand Name cannot exceed 150 characters");
        }

        itemBrandRepository.findByBuIdAndBrandNameIgnoreCase(request.getBuId(), brandName)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Brand Name '" + brandName + "' already exists for this Business Unit");
                    }
                });

        if (request.getBrandCode() == null || request.getBrandCode().trim().isEmpty()) {
            throw new BadRequestException("Brand Code cannot be empty");
        }
        String brandCode = request.getBrandCode().trim();
        if (brandCode.length() > 50) {
            throw new BadRequestException("Brand Code cannot exceed 50 characters");
        }

        itemBrandRepository.findByBuIdAndBrandCodeIgnoreCase(request.getBuId(), brandCode)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Brand Code '" + brandCode + "' already exists for this Business Unit");
                    }
                });

        StatusUtil.validate(request.getStatus());

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setBrandName(brandName);
        existing.setBrandCode(brandCode);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return toResponse(itemBrandRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteItemBrand(Long id) {
        InvItemBrand existing = findBrand(id);
        itemBrandRepository.delete(existing);
    }

    private InvItemBrand findBrand(Long id) {
        return itemBrandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item Brand not found with id: " + id));
    }

    private InvItemBrandResponseDto toResponse(InvItemBrand entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String createdUserName = entity.getCreatedUserId() != null
                ? userRepository.findById(entity.getCreatedUserId()).map(User::getUserName).orElse(null)
                : null;

        String createdEmpName = null;
        if (entity.getCreatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getCreatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                createdEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        String updatedUserName = entity.getUpdatedUserId() != null
                ? userRepository.findById(entity.getUpdatedUserId()).map(User::getUserName).orElse(null)
                : null;

        String updatedEmpName = null;
        if (entity.getUpdatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getUpdatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                updatedEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        return InvItemBrandResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .brandName(entity.getBrandName())
                .brandCode(entity.getBrandCode())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdUserName(createdUserName)
                .createdEmpId(entity.getCreatedEmpId())
                .createdEmpName(createdEmpName)
                .createdDatetime(entity.getCreatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedUserName(updatedUserName)
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedEmpName(updatedEmpName)
                .updatedDatetime(entity.getUpdatedDatetime())
                .updatedApp(entity.getUpdatedApp())
                .build();
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found with id: " + buId));

        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Selected Business Unit is not active");
        }

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException("Business Unit does not belong to selected Business Group");
        }
    }
}

