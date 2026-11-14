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
import com.tse.erp.module.inventory.dto.AssignStoreEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreEmployeeMapDto;
import com.tse.erp.module.inventory.entity.InvItemStore;
import com.tse.erp.module.inventory.entity.InvItemStoreEmployeeMap;
import com.tse.erp.module.inventory.repository.InvItemStoreEmployeeMapRepository;
import com.tse.erp.module.inventory.repository.InvItemStoreRepository;
import com.tse.erp.module.inventory.service.InvItemStoreEmployeeMapService;
import com.tse.erp.security.AuthUser;
import com.tse.erp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InvItemStoreEmployeeMapServiceImpl implements InvItemStoreEmployeeMapService {

    private final InvItemStoreEmployeeMapRepository storeEmployeeMapRepository;
    private final InvItemStoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;

    @Override
    public List<InvItemStoreEmployeeMapDto> getEmployeesByStoreId(Long storeId) {
        if (storeId == null) {
            return Collections.emptyList();
        }

        return storeEmployeeMapRepository.findByStoreIdOrderByIdAsc(storeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<InvItemStoreEmployeeMapDto> assignEmployeesToStore(AssignStoreEmployeesRequestDto request) {
        if (request == null || request.getStoreId() == null) {
            throw new BadRequestException("Store ID is required");
        }

        InvItemStore store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getStoreId()));

        Long userBgId = CurrentUser.get().map(AuthUser::bgId).orElse(null);
        Long userBuId = CurrentUser.get().map(AuthUser::buId).orElse(null);

        Long bgId = userBgId != null ? userBgId : (request.getBgId() != null ? request.getBgId() : store.getBgId());
        Long buId = userBuId != null ? userBuId : (request.getBuId() != null ? request.getBuId() : store.getBuId());

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
                throw new BadRequestException("Selected Employee '" + emp.getEmployeeName() + "' does not belong to the user's Business Unit");
            }

            Optional<InvItemStoreEmployeeMap> existingOpt =
                    storeEmployeeMapRepository.findByStoreIdAndEmployeeId(store.getId(), empId);

            if (existingOpt.isPresent()) {
                InvItemStoreEmployeeMap existing = existingOpt.get();
                existing.setStatus(status);
                existing.setUpdatedApp(AppConstants.APP_NAME);
                storeEmployeeMapRepository.save(existing);
            } else {
                InvItemStoreEmployeeMap newMap = InvItemStoreEmployeeMap.builder()
                        .bgId(bgId)
                        .buId(buId)
                        .storeId(store.getId())
                        .employeeId(empId)
                        .build();
                newMap.setStatus(status);
                newMap.setCreatedApp(AppConstants.APP_NAME);
                storeEmployeeMapRepository.save(newMap);
            }
        }

        return getEmployeesByStoreId(store.getId());
    }

    @Override
    @Transactional
    public void removeEmployeeFromStore(Long storeId, Long employeeId) {
        if (storeId == null || employeeId == null) {
            throw new BadRequestException("Store ID and Employee ID are required");
        }
        storeEmployeeMapRepository.deleteByStoreIdAndEmployeeId(storeId, employeeId);
    }

    @Override
    @Transactional
    public void deleteEmployeeMap(Long mapId) {
        InvItemStoreEmployeeMap existing = storeEmployeeMapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("Store Employee mapping not found with id: " + mapId));
        storeEmployeeMapRepository.delete(existing);
    }

    @Override
    @Transactional
    public InvItemStoreEmployeeMapDto updateMapStatus(Long mapId, Integer status) {
        StatusUtil.validate(status);
        InvItemStoreEmployeeMap existing = storeEmployeeMapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("Store Employee mapping not found with id: " + mapId));
        existing.setStatus(status);
        existing.setUpdatedApp(AppConstants.APP_NAME);
        return toDto(storeEmployeeMapRepository.save(existing));
    }

    @Override
    @Transactional
    public List<InvItemStoreEmployeeMapDto> syncStoreEmployees(Long storeId, Long bgId, Long buId, List<Long> employeeIds) {
        if (storeId == null) {
            return Collections.emptyList();
        }

        List<InvItemStoreEmployeeMap> currentMaps = storeEmployeeMapRepository.findByStoreIdOrderByIdAsc(storeId);
        Set<Long> incomingIds = new HashSet<>(employeeIds != null ? employeeIds : Collections.emptyList());

        // Delete ones no longer in list
        List<InvItemStoreEmployeeMap> toDelete = currentMaps.stream()
                .filter(m -> !incomingIds.contains(m.getEmployeeId()))
                .toList();
        if (!toDelete.isEmpty()) {
            storeEmployeeMapRepository.deleteAll(toDelete);
        }

        // Add new ones
        Set<Long> existingIds = new HashSet<>();
        currentMaps.forEach(m -> existingIds.add(m.getEmployeeId()));

        Long userBgId = CurrentUser.get().map(AuthUser::bgId).orElse(null);
        Long userBuId = CurrentUser.get().map(AuthUser::buId).orElse(null);
        Long effectiveBgId = userBgId != null ? userBgId : (bgId != null ? bgId : 1L);
        Long effectiveBuId = userBuId != null ? userBuId : (buId != null ? buId : 1L);

        List<InvItemStoreEmployeeMap> toAdd = new ArrayList<>();
        for (Long empId : incomingIds) {
            if (empId == null || existingIds.contains(empId)) continue;
            InvItemStoreEmployeeMap newMap = InvItemStoreEmployeeMap.builder()
                    .bgId(effectiveBgId)
                    .buId(effectiveBuId)
                    .storeId(storeId)
                    .employeeId(empId)
                    .build();
            newMap.setStatus(1);
            newMap.setCreatedApp(AppConstants.APP_NAME);
            toAdd.add(newMap);
        }

        if (!toAdd.isEmpty()) {
            storeEmployeeMapRepository.saveAll(toAdd);
        }

        return getEmployeesByStoreId(storeId);
    }

    private InvItemStoreEmployeeMapDto toDto(InvItemStoreEmployeeMap entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String storeName = null;
        String storeCode = null;
        Optional<InvItemStore> storeOpt = storeRepository.findById(entity.getStoreId());
        if (storeOpt.isPresent()) {
            storeName = storeOpt.get().getStoreName();
            storeCode = storeOpt.get().getStoreCode();
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

        return InvItemStoreEmployeeMapDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .storeId(entity.getStoreId())
                .storeName(storeName)
                .storeCode(storeCode)
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

