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
import com.tse.erp.module.inventory.dto.InvItemStoreRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreResponseDto;
import com.tse.erp.module.inventory.entity.InvItemStore;
import com.tse.erp.module.inventory.repository.InvItemStoreRepository;
import com.tse.erp.module.inventory.service.InvItemStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvItemStoreServiceImpl implements InvItemStoreService {

    private final InvItemStoreRepository itemStoreRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<InvItemStoreResponseDto> getAllItemStores(
            Long bgId, Long buId, String storeName, String storeCode, Integer isDefault, Integer status) {
        return itemStoreRepository.searchItemStores(bgId, buId, storeName, storeCode, isDefault, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvItemStoreResponseDto getItemStoreById(Long id) {
        return toResponse(findStore(id));
    }

    @Override
    public String generateNextStoreCode(Long buId) {
        if (buId == null) {
            throw new BadRequestException("Business Unit ID is required to generate next Store Code");
        }
        long count = itemStoreRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("STR-%05d", next);
        while (itemStoreRepository.existsByBuIdAndStoreCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("STR-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public InvItemStoreResponseDto createItemStore(InvItemStoreRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getStoreName() == null || request.getStoreName().trim().isEmpty()) {
            throw new BadRequestException("Store Name is required");
        }
        String storeName = request.getStoreName().trim();
        if (storeName.length() > 150) {
            throw new BadRequestException("Store Name cannot exceed 150 characters");
        }

        if (itemStoreRepository.existsByBuIdAndStoreNameIgnoreCase(request.getBuId(), storeName)) {
            throw new DuplicateResourceException("Store Name '" + storeName + "' already exists for this Business Unit");
        }

        String storeCode = request.getStoreCode();
        if (storeCode == null || storeCode.trim().isEmpty()) {
            storeCode = generateNextStoreCode(request.getBuId());
        } else {
            storeCode = storeCode.trim();
            if (storeCode.length() > 50) {
                throw new BadRequestException("Store Code cannot exceed 50 characters");
            }
            if (itemStoreRepository.existsByBuIdAndStoreCodeIgnoreCase(request.getBuId(), storeCode)) {
                throw new DuplicateResourceException("Store Code '" + storeCode + "' already exists for this Business Unit");
            }
        }

        StatusUtil.validate(request.getStatus());

        Integer isDefault = (request.getIsDefault() != null && request.getIsDefault() == 1) ? 1 : 0;
        if (isDefault == 1) {
            itemStoreRepository.resetOtherDefaults(request.getBuId(), null);
        }

        InvItemStore entity = InvItemStore.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .storeName(storeName)
                .storeCode(storeCode)
                .isDefault(isDefault)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toResponse(itemStoreRepository.save(entity));
    }

    @Override
    @Transactional
    public InvItemStoreResponseDto updateItemStore(Long id, InvItemStoreRequestDto request) {
        InvItemStore existing = findStore(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getStoreName() == null || request.getStoreName().trim().isEmpty()) {
            throw new BadRequestException("Store Name is required");
        }
        String storeName = request.getStoreName().trim();
        if (storeName.length() > 150) {
            throw new BadRequestException("Store Name cannot exceed 150 characters");
        }

        itemStoreRepository.findByBuIdAndStoreNameIgnoreCase(request.getBuId(), storeName)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Store Name '" + storeName + "' already exists for this Business Unit");
                    }
                });

        if (request.getStoreCode() == null || request.getStoreCode().trim().isEmpty()) {
            throw new BadRequestException("Store Code cannot be empty");
        }
        String storeCode = request.getStoreCode().trim();
        if (storeCode.length() > 50) {
            throw new BadRequestException("Store Code cannot exceed 50 characters");
        }

        itemStoreRepository.findByBuIdAndStoreCodeIgnoreCase(request.getBuId(), storeCode)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Store Code '" + storeCode + "' already exists for this Business Unit");
                    }
                });

        StatusUtil.validate(request.getStatus());

        Integer isDefault = (request.getIsDefault() != null && request.getIsDefault() == 1) ? 1 : 0;
        if (isDefault == 1) {
            itemStoreRepository.resetOtherDefaults(request.getBuId(), id);
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setStoreName(storeName);
        existing.setStoreCode(storeCode);
        existing.setIsDefault(isDefault);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return toResponse(itemStoreRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteItemStore(Long id) {
        InvItemStore existing = findStore(id);
        itemStoreRepository.delete(existing);
    }

    private InvItemStore findStore(Long id) {
        return itemStoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item Store not found with id: " + id));
    }

    private InvItemStoreResponseDto toResponse(InvItemStore entity) {
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

        return InvItemStoreResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .storeName(entity.getStoreName())
                .storeCode(entity.getStoreCode())
                .isDefault(entity.getIsDefault())
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

