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
import com.tse.erp.module.inventory.dto.InvItemWarehouseEmployeeMapDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseResponseDto;
import com.tse.erp.module.inventory.entity.InvItemStore;
import com.tse.erp.module.inventory.entity.InvItemWarehouse;
import com.tse.erp.module.inventory.repository.InvItemStoreRepository;
import com.tse.erp.module.inventory.repository.InvItemWarehouseRepository;
import com.tse.erp.module.inventory.service.InvItemWarehouseEmployeeMapService;
import com.tse.erp.module.inventory.service.InvItemWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InvItemWarehouseServiceImpl implements InvItemWarehouseService {

    private final InvItemWarehouseRepository itemWarehouseRepository;
    private final InvItemStoreRepository itemStoreRepository;
    private final InvItemWarehouseEmployeeMapService warehouseEmployeeMapService;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<InvItemWarehouseResponseDto> getAllItemWarehouses(
            Long bgId, Long buId, String warehouseName, String warehouseCode, String storeId, Integer isDefault, Integer status) {
        return itemWarehouseRepository.searchItemWarehouses(bgId, buId, warehouseName, warehouseCode, storeId, isDefault, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvItemWarehouseResponseDto getItemWarehouseById(Long id) {
        return toResponse(findWarehouse(id));
    }

    @Override
    public String generateNextWarehouseCode(Long buId) {
        if (buId == null) {
            throw new BadRequestException("Business Unit ID is required to generate next Warehouse Code");
        }
        long count = itemWarehouseRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("WH-%05d", next);
        while (itemWarehouseRepository.existsByBuIdAndWarehouseCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("WH-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public InvItemWarehouseResponseDto createItemWarehouse(InvItemWarehouseRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getWarehouseName() == null || request.getWarehouseName().trim().isEmpty()) {
            throw new BadRequestException("Warehouse Name is required");
        }
        String warehouseName = request.getWarehouseName().trim();
        if (warehouseName.length() > 150) {
            throw new BadRequestException("Warehouse Name cannot exceed 150 characters");
        }

        if (itemWarehouseRepository.existsByBuIdAndWarehouseNameIgnoreCase(request.getBuId(), warehouseName)) {
            throw new DuplicateResourceException("Warehouse Name '" + warehouseName + "' already exists for this Business Unit");
        }

        String warehouseCode = request.getWarehouseCode();
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            warehouseCode = generateNextWarehouseCode(request.getBuId());
        } else {
            warehouseCode = warehouseCode.trim();
            if (warehouseCode.length() > 50) {
                throw new BadRequestException("Warehouse Code cannot exceed 50 characters");
            }
            if (itemWarehouseRepository.existsByBuIdAndWarehouseCodeIgnoreCase(request.getBuId(), warehouseCode)) {
                throw new DuplicateResourceException("Warehouse Code '" + warehouseCode + "' already exists for this Business Unit");
            }
        }

        String normalizedStoreId = normalizeStoreIds(request.getStoreId());
        validateStore(normalizedStoreId, request.getBuId());
        StatusUtil.validate(request.getStatus());

        Integer isDefault = (request.getIsDefault() != null && request.getIsDefault() == 1) ? 1 : 0;
        if (isDefault == 1) {
            itemWarehouseRepository.resetOtherDefaults(request.getBuId(), null);
        }

        InvItemWarehouse entity = InvItemWarehouse.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .warehouseName(warehouseName)
                .warehouseCode(warehouseCode)
                .isDefault(isDefault)
                .storeId(normalizedStoreId)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        InvItemWarehouse saved = itemWarehouseRepository.save(entity);
        List<Long> syncEmpIds = resolveEmployeeIds(request.getEmployeeIds(), request.getEmployeeId());
        if (syncEmpIds != null && !syncEmpIds.isEmpty()) {
            warehouseEmployeeMapService.syncWarehouseEmployees(saved.getId(), saved.getBgId(), saved.getBuId(), syncEmpIds);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InvItemWarehouseResponseDto updateItemWarehouse(Long id, InvItemWarehouseRequestDto request) {
        InvItemWarehouse existing = findWarehouse(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getWarehouseName() == null || request.getWarehouseName().trim().isEmpty()) {
            throw new BadRequestException("Warehouse Name is required");
        }
        String warehouseName = request.getWarehouseName().trim();
        if (warehouseName.length() > 150) {
            throw new BadRequestException("Warehouse Name cannot exceed 150 characters");
        }

        itemWarehouseRepository.findByBuIdAndWarehouseNameIgnoreCase(request.getBuId(), warehouseName)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Warehouse Name '" + warehouseName + "' already exists for this Business Unit");
                    }
                });

        if (request.getWarehouseCode() == null || request.getWarehouseCode().trim().isEmpty()) {
            throw new BadRequestException("Warehouse Code cannot be empty");
        }
        String warehouseCode = request.getWarehouseCode().trim();
        if (warehouseCode.length() > 50) {
            throw new BadRequestException("Warehouse Code cannot exceed 50 characters");
        }

        itemWarehouseRepository.findByBuIdAndWarehouseCodeIgnoreCase(request.getBuId(), warehouseCode)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Warehouse Code '" + warehouseCode + "' already exists for this Business Unit");
                    }
                });

        String normalizedStoreId = normalizeStoreIds(request.getStoreId());
        validateStore(normalizedStoreId, request.getBuId());
        StatusUtil.validate(request.getStatus());

        Integer isDefault = (request.getIsDefault() != null && request.getIsDefault() == 1) ? 1 : 0;
        if (isDefault == 1) {
            itemWarehouseRepository.resetOtherDefaults(request.getBuId(), id);
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setWarehouseName(warehouseName);
        existing.setWarehouseCode(warehouseCode);
        existing.setIsDefault(isDefault);
        existing.setStoreId(normalizedStoreId);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        InvItemWarehouse saved = itemWarehouseRepository.save(existing);
        List<Long> updateEmpIds = resolveEmployeeIds(request.getEmployeeIds(), request.getEmployeeId());
        if (updateEmpIds != null) {
            warehouseEmployeeMapService.syncWarehouseEmployees(saved.getId(), saved.getBgId(), saved.getBuId(), updateEmpIds);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteItemWarehouse(Long id) {
        InvItemWarehouse existing = findWarehouse(id);
        warehouseEmployeeMapService.syncWarehouseEmployees(id, existing.getBgId(), existing.getBuId(), Collections.emptyList());
        itemWarehouseRepository.delete(existing);
    }

    private InvItemWarehouse findWarehouse(Long id) {
        return itemWarehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item Warehouse not found with id: " + id));
    }

    private String normalizeStoreIds(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            return null;
        }
        List<String> list = Arrays.stream(storeId.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        return list.isEmpty() ? null : String.join(",", list);
    }

    private void validateStore(String storeId, Long buId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            return;
        }
        String[] parts = storeId.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            Long sId;
            try {
                sId = Long.parseLong(trimmed);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid Store ID: " + trimmed);
            }
            InvItemStore store = itemStoreRepository.findById(sId)
                    .orElseThrow(() -> new ResourceNotFoundException("Item Store not found with id: " + sId));
            if (buId != null && !buId.equals(store.getBuId())) {
                throw new BadRequestException("Selected Store '" + store.getStoreName() + "' does not belong to the same Business Unit");
            }
        }
    }

    private InvItemWarehouseResponseDto toResponse(InvItemWarehouse entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        List<Long> storeIds = new ArrayList<>();
        List<String> storeNames = new ArrayList<>();
        List<String> storeCodes = new ArrayList<>();

        if (entity.getStoreId() != null && !entity.getStoreId().trim().isEmpty()) {
            String[] parts = entity.getStoreId().split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) continue;
                try {
                    Long sId = Long.parseLong(trimmed);
                    storeIds.add(sId);
                    itemStoreRepository.findById(sId).ifPresent(s -> {
                        if (s.getStoreName() != null && !s.getStoreName().isEmpty()) {
                            storeNames.add(s.getStoreName());
                        }
                        if (s.getStoreCode() != null && !s.getStoreCode().isEmpty()) {
                            storeCodes.add(s.getStoreCode());
                        }
                    });
                } catch (NumberFormatException ignored) {}
            }
        }

        String storeName = storeNames.isEmpty() ? null : String.join(", ", storeNames);
        String storeCode = storeCodes.isEmpty() ? null : String.join(", ", storeCodes);

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

        List<InvItemWarehouseEmployeeMapDto> assignedEmployees = null;
        if (warehouseEmployeeMapService != null && entity.getId() != null) {
            try {
                assignedEmployees = warehouseEmployeeMapService.getEmployeesByWarehouseId(entity.getId());
            } catch (Exception ignored) {
            }
        }

        return InvItemWarehouseResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .warehouseName(entity.getWarehouseName())
                .warehouseCode(entity.getWarehouseCode())
                .isDefault(entity.getIsDefault())
                .storeId(entity.getStoreId())
                .storeIds(storeIds)
                .storeName(storeName)
                .storeNames(storeNames)
                .storeCode(storeCode)
                .assignedEmployees(assignedEmployees)
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

    private List<Long> resolveEmployeeIds(List<Long> employeeIds, String employeeIdStr) {
        if (employeeIds != null) {
            return employeeIds;
        }
        if (employeeIdStr != null && !employeeIdStr.trim().isEmpty()) {
            return Arrays.stream(employeeIdStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        try {
                            return Long.parseLong(s);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
        return null;
    }
}

