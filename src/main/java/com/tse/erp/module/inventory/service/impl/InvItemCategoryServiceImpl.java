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
import com.tse.erp.module.inventory.dto.InvItemCategoryRequestDto;
import com.tse.erp.module.inventory.dto.InvItemCategoryResponseDto;
import com.tse.erp.module.inventory.entity.InvItemCategory;
import com.tse.erp.module.inventory.repository.InvItemCategoryRepository;
import com.tse.erp.module.inventory.service.InvItemCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvItemCategoryServiceImpl implements InvItemCategoryService {

    private final InvItemCategoryRepository itemCategoryRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<InvItemCategoryResponseDto> getAllItemCategories(
            Long bgId, Long buId, String categoryName, String categoryCode, Integer status) {
        return itemCategoryRepository.searchItemCategories(bgId, buId, categoryName, categoryCode, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvItemCategoryResponseDto getItemCategoryById(Long id) {
        return toResponse(findCategory(id));
    }

    @Override
    public String generateNextCategoryCode(Long buId) {
        if (buId == null) {
            throw new BadRequestException("Business Unit ID is required to generate next Category Code");
        }
        long count = itemCategoryRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("CAT-%05d", next);
        while (itemCategoryRepository.existsByBuIdAndCategoryCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("CAT-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public InvItemCategoryResponseDto createItemCategory(InvItemCategoryRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new BadRequestException("Category Name is required");
        }
        String categoryName = request.getCategoryName().trim();
        if (categoryName.length() > 150) {
            throw new BadRequestException("Category Name cannot exceed 150 characters");
        }

        if (itemCategoryRepository.existsByBuIdAndCategoryNameIgnoreCase(request.getBuId(), categoryName)) {
            throw new DuplicateResourceException("Category Name '" + categoryName + "' already exists for this Business Unit");
        }

        String categoryCode = request.getCategoryCode();
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            categoryCode = generateNextCategoryCode(request.getBuId());
        } else {
            categoryCode = categoryCode.trim();
            if (categoryCode.length() > 50) {
                throw new BadRequestException("Category Code cannot exceed 50 characters");
            }
            if (itemCategoryRepository.existsByBuIdAndCategoryCodeIgnoreCase(request.getBuId(), categoryCode)) {
                throw new DuplicateResourceException("Category Code '" + categoryCode + "' already exists for this Business Unit");
            }
        }

        StatusUtil.validate(request.getStatus());

        InvItemCategory entity = InvItemCategory.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .categoryName(categoryName)
                .categoryCode(categoryCode)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toResponse(itemCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public InvItemCategoryResponseDto updateItemCategory(Long id, InvItemCategoryRequestDto request) {
        InvItemCategory existing = findCategory(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new BadRequestException("Category Name is required");
        }
        String categoryName = request.getCategoryName().trim();
        if (categoryName.length() > 150) {
            throw new BadRequestException("Category Name cannot exceed 150 characters");
        }

        itemCategoryRepository.findByBuIdAndCategoryNameIgnoreCase(request.getBuId(), categoryName)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Category Name '" + categoryName + "' already exists for this Business Unit");
                    }
                });

        if (request.getCategoryCode() == null || request.getCategoryCode().trim().isEmpty()) {
            throw new BadRequestException("Category Code cannot be empty");
        }
        String categoryCode = request.getCategoryCode().trim();
        if (categoryCode.length() > 50) {
            throw new BadRequestException("Category Code cannot exceed 50 characters");
        }

        itemCategoryRepository.findByBuIdAndCategoryCodeIgnoreCase(request.getBuId(), categoryCode)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Category Code '" + categoryCode + "' already exists for this Business Unit");
                    }
                });

        StatusUtil.validate(request.getStatus());

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setCategoryName(categoryName);
        existing.setCategoryCode(categoryCode);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return toResponse(itemCategoryRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteItemCategory(Long id) {
        InvItemCategory existing = findCategory(id);
        itemCategoryRepository.delete(existing);
    }

    private InvItemCategory findCategory(Long id) {
        return itemCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item Category not found with id: " + id));
    }

    private InvItemCategoryResponseDto toResponse(InvItemCategory entity) {
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

        return InvItemCategoryResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .categoryName(entity.getCategoryName())
                .categoryCode(entity.getCategoryCode())
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

