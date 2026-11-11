package com.tse.erp.module.inventory.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.AssignWarehouseEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseEmployeeMapDto;
import com.tse.erp.module.inventory.entity.InvItemWarehouse;
import com.tse.erp.module.inventory.entity.InvItemWarehouseEmployeeMap;
import com.tse.erp.module.inventory.repository.InvItemWarehouseEmployeeMapRepository;
import com.tse.erp.module.inventory.repository.InvItemWarehouseRepository;
import com.tse.erp.module.inventory.service.InvItemWarehouseEmployeeMapService;
import com.tse.erp.security.AuthUser;
import com.tse.erp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InvItemWarehouseEmployeeMapServiceImpl implements InvItemWarehouseEmployeeMapService {

    private final InvItemWarehouseEmployeeMapRepository warehouseEmployeeMapRepository;
    private final InvItemWarehouseRepository warehouseRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;

    @Override
    public List<InvItemWarehouseEmployeeMapDto> getEmployeesByWarehouseId(Long warehouseId) {
        if (warehouseId == null) {
            return Collections.emptyList();
        }

        return warehouseEmployeeMapRepository.findByWarehouseIdOrderByIdAsc(warehouseId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<InvItemWarehouseEmployeeMapDto> assignEmployeesToWarehouse(AssignWarehouseEmployeesRequestDto request) {
        if (request == null || request.getWarehouseId() == null) {
            throw new BadRequestException("Warehouse ID is required");
        }

        InvItemWarehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

        Long userBgId = CurrentUser.get().map(AuthUser::bgId).orElse(null);
        Long userBuId = CurrentUser.get().map(AuthUser::buId).orElse(null);

        Long bgId = userBgId != null ? userBgId : (request.getBgId() != null ? request.getBgId() : warehouse.getBgId());
        Long buId = userBuId != null ? userBuId : (request.getBuId() != null ? request.getBuId() : warehouse.getBuId());

        Set<Long> targetEmployeeIds = new LinkedHashSet<>();
        if (request.getEmployeeIds() != null) {
            request.getEmployeeIds().stream().filter(Objects::nonNull).forEach(targetEmployeeIds::add);
        }
        if (request.getEmployeeId() != null && !request.getEmployeeId().trim().isEmpty()) {
            for (String part : request.getEmployeeId().split(",")) {
                String clean = part.trim();
                if (!clean.isEmpty()) {
                    try {
                        targetEmployeeIds.add(Long.parseLong(clean));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (targetEmployeeIds.isEmpty()) {
            throw new BadRequestException("At least one Employee must be selected");
        }

        Integer status = request.getStatus() != null ? request.getStatus() : 1;
        StatusUtil.validate(status);

        for (Long empId : targetEmployeeIds) {
            Employee emp = employeeRepository.findById(empId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + empId));

            if (buId != null && emp.getBuId() != null && !buId.equals(emp.getBuId())) {
                throw new BadRequestException("Selected Employee '" + emp.getEmployeeName() + "' does not belong to the same Business Unit");
            }

            Optional<InvItemWarehouseEmployeeMap> existingOpt =
                    warehouseEmployeeMapRepository.findByWarehouseIdAndEmployeeId(warehouse.getId(), empId);

            if (existingOpt.isPresent()) {
                InvItemWarehouseEmployeeMap existing = existingOpt.get();
                existing.setStatus(status);
                existing.setUpdatedApp(AppConstants.APP_NAME);
                warehouseEmployeeMapRepository.save(existing);
            } else {
                InvItemWarehouseEmployeeMap newMap = InvItemWarehouseEmployeeMap.builder()
                        .bgId(bgId)
                        .buId(buId)
                        .warehouseId(warehouse.getId())
                        .employeeId(empId)
                        .build();
                newMap.setStatus(status);
                newMap.setCreatedApp(AppConstants.APP_NAME);
                warehouseEmployeeMapRepository.save(newMap);
            }
        }

        return getEmployeesByWarehouseId(warehouse.getId());
    }

    @Override
    @Transactional
    public void removeEmployeeFromWarehouse(Long warehouseId, Long employeeId) {
        if (warehouseId == null || employeeId == null) {
            throw new BadRequestException("Warehouse ID and Employee ID are required");
        }
        warehouseEmployeeMapRepository.deleteByWarehouseIdAndEmployeeId(warehouseId, employeeId);
    }

    @Override
    @Transactional
    public void deleteEmployeeMap(Long mapId) {
        InvItemWarehouseEmployeeMap existing = warehouseEmployeeMapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse Employee mapping not found with id: " + mapId));
        warehouseEmployeeMapRepository.delete(existing);
    }

    @Override
    @Transactional
    public InvItemWarehouseEmployeeMapDto updateMapStatus(Long mapId, Integer status) {
        StatusUtil.validate(status);
        InvItemWarehouseEmployeeMap existing = warehouseEmployeeMapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse Employee mapping not found with id: " + mapId));
        existing.setStatus(status);
        existing.setUpdatedApp(AppConstants.APP_NAME);
        return toDto(warehouseEmployeeMapRepository.save(existing));
    }

    @Override
    @Transactional
    public List<InvItemWarehouseEmployeeMapDto> syncWarehouseEmployees(Long warehouseId, Long bgId, Long buId, List<Long> employeeIds) {
        if (warehouseId == null) {
            return Collections.emptyList();
        }

        List<InvItemWarehouseEmployeeMap> currentMaps = warehouseEmployeeMapRepository.findByWarehouseIdOrderByIdAsc(warehouseId);
        Set<Long> incomingIds = new HashSet<>(employeeIds != null ? employeeIds : Collections.emptyList());

        // Delete ones no longer in list
        List<InvItemWarehouseEmployeeMap> toDelete = currentMaps.stream()
                .filter(m -> !incomingIds.contains(m.getEmployeeId()))
                .toList();
        if (!toDelete.isEmpty()) {
            warehouseEmployeeMapRepository.deleteAll(toDelete);
        }

        // Add new ones
        Set<Long> existingIds = new HashSet<>();
        currentMaps.forEach(m -> existingIds.add(m.getEmployeeId()));

        Long userBgId = CurrentUser.get().map(AuthUser::bgId).orElse(null);
        Long userBuId = CurrentUser.get().map(AuthUser::buId).orElse(null);
        Long effectiveBgId = userBgId != null ? userBgId : (bgId != null ? bgId : 1L);
        Long effectiveBuId = userBuId != null ? userBuId : (buId != null ? buId : 1L);

        List<InvItemWarehouseEmployeeMap> toAdd = new ArrayList<>();
        for (Long empId : incomingIds) {
            if (empId == null || existingIds.contains(empId)) continue;
            InvItemWarehouseEmployeeMap newMap = InvItemWarehouseEmployeeMap.builder()
                    .bgId(effectiveBgId)
                    .buId(effectiveBuId)
                    .warehouseId(warehouseId)
                    .employeeId(empId)
                    .build();
            newMap.setStatus(1);
            newMap.setCreatedApp(AppConstants.APP_NAME);
            toAdd.add(newMap);
        }

        if (!toAdd.isEmpty()) {
            warehouseEmployeeMapRepository.saveAll(toAdd);
        }

        return getEmployeesByWarehouseId(warehouseId);
    }

    public InvItemWarehouseEmployeeMapDto toDto(InvItemWarehouseEmployeeMap entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String warehouseName = null;
        String warehouseCode = null;
        Optional<InvItemWarehouse> whOpt = warehouseRepository.findById(entity.getWarehouseId());
        if (whOpt.isPresent()) {
            warehouseName = whOpt.get().getWarehouseName();
            warehouseCode = whOpt.get().getWarehouseCode();
        }

        String employeeName = null;
        String employeeCode = null;
        String employeeDisplay = null;
        if (entity.getEmployeeId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getEmployeeId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                employeeName = emp.getEmployeeName();
                employeeCode = emp.getEmployeeCode();
                String codeStr = employeeCode != null && !employeeCode.trim().isEmpty() ? " (" + employeeCode.trim() + ")" : "";
                employeeDisplay = (employeeName != null ? employeeName : "") + codeStr;
            } else {
                employeeDisplay = "Employee #" + entity.getEmployeeId();
            }
        }

        String createdUserName = entity.getCreatedUserId() != null
                ? userRepository.findById(entity.getCreatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String createdEmpName = null;
        if (entity.getCreatedEmpId() != null) {
            Optional<Employee> auditEmp = employeeRepository.findById(entity.getCreatedEmpId());
            if (auditEmp.isPresent()) {
                Employee e = auditEmp.get();
                createdEmpName = (e.getEmployeeCode() != null ? e.getEmployeeCode() : "") + ": " + (e.getEmployeeName() != null ? e.getEmployeeName() : "");
            }
        }

        String updatedUserName = entity.getUpdatedUserId() != null
                ? userRepository.findById(entity.getUpdatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String updatedEmpName = null;
        if (entity.getUpdatedEmpId() != null) {
            Optional<Employee> auditEmp = employeeRepository.findById(entity.getUpdatedEmpId());
            if (auditEmp.isPresent()) {
                Employee e = auditEmp.get();
                updatedEmpName = (e.getEmployeeCode() != null ? e.getEmployeeCode() : "") + ": " + (e.getEmployeeName() != null ? e.getEmployeeName() : "");
            }
        }

        return InvItemWarehouseEmployeeMapDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .warehouseId(entity.getWarehouseId())
                .warehouseName(warehouseName)
                .warehouseCode(warehouseCode)
                .employeeId(entity.getEmployeeId())
                .employeeName(employeeName)
                .employeeCode(employeeCode)
                .employeeDisplay(employeeDisplay)
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
}
