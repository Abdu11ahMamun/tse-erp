package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmUserSetupRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserSetupResponseDto;
import com.tse.erp.module.accounting.dto.UserOptionDto;
import com.tse.erp.module.accounting.entity.AfmBank;
import com.tse.erp.module.accounting.entity.AfmUserLocation;
import com.tse.erp.module.accounting.entity.AfmUserSetup;
import com.tse.erp.module.accounting.repository.AfmBankDtlsRepository;
import com.tse.erp.module.accounting.repository.AfmBankRepository;
import com.tse.erp.module.accounting.repository.AfmLookupValueRepository;
import com.tse.erp.module.accounting.repository.AfmUserLocationRepository;
import com.tse.erp.module.accounting.repository.AfmUserSetupRepository;
import com.tse.erp.module.accounting.service.AfmUserSetupService;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.UserEmplMap;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserEmplMapRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AfmUserSetupServiceImpl implements AfmUserSetupService {

    private final AfmUserSetupRepository userSetupRepository;
    private final UserRepository userRepository;
    private final UserEmplMapRepository userEmplMapRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AfmLookupValueRepository lookupValueRepository;
    private final AfmUserLocationRepository userLocationRepository;
    private final AfmBankDtlsRepository bankDtlsRepository;
    private final AfmBankRepository bankRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<AfmUserSetupResponseDto> getAllUserSetups(Long bgId, Long buId, Long userId, Integer status) {
        return userSetupRepository.searchUserSetups(bgId, buId, userId, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AfmUserSetupResponseDto getUserSetupById(Long id) {
        return toResponse(findSetup(id));
    }

    @Override
    @Transactional
    public AfmUserSetupResponseDto createUserSetup(AfmUserSetupRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getUserId() == null) {
            throw new BadRequestException("User ID is required");
        }

        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (userSetupRepository.existsByBuIdAndUserId(request.getBuId(), request.getUserId())) {
            throw new DuplicateResourceException("User Setup already exists for this User in selected Business Unit");
        }

        StatusUtil.validate(request.getStatus());

        AfmUserSetup entity = AfmUserSetup.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .userId(request.getUserId())
                .voucherDate(request.getVoucherDate() != null ? request.getVoucherDate().trim() : null)
                .voucherType(request.getVoucherType() != null ? request.getVoucherType().trim() : null)
                .voucherTypeDefault(request.getVoucherTypeDefault() != null ? request.getVoucherTypeDefault().trim() : null)
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .locationDefault(request.getLocationDefault() != null ? request.getLocationDefault().trim() : null)
                .bankAccount(request.getBankAccount() != null ? request.getBankAccount().trim() : null)
                .bankAccountDefault(request.getBankAccountDefault() != null ? request.getBankAccountDefault().trim() : null)
                .costCenter(request.getCostCenter() != null ? request.getCostCenter().trim() : null)
                .costCenterDefault(request.getCostCenterDefault() != null ? request.getCostCenterDefault().trim() : null)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toResponse(userSetupRepository.save(entity));
    }

    @Override
    @Transactional
    public AfmUserSetupResponseDto updateUserSetup(Long id, AfmUserSetupRequestDto request) {
        AfmUserSetup existing = findSetup(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getUserId() == null) {
            throw new BadRequestException("User ID is required");
        }

        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        userSetupRepository.findByBuIdAndUserId(request.getBuId(), request.getUserId())
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("User Setup already exists for this User in selected Business Unit");
                    }
                });

        StatusUtil.validate(request.getStatus());

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setUserId(request.getUserId());
        existing.setVoucherDate(request.getVoucherDate() != null ? request.getVoucherDate().trim() : null);
        existing.setVoucherType(request.getVoucherType() != null ? request.getVoucherType().trim() : null);
        existing.setVoucherTypeDefault(request.getVoucherTypeDefault() != null ? request.getVoucherTypeDefault().trim() : null);
        existing.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        existing.setLocationDefault(request.getLocationDefault() != null ? request.getLocationDefault().trim() : null);
        existing.setBankAccount(request.getBankAccount() != null ? request.getBankAccount().trim() : null);
        existing.setBankAccountDefault(request.getBankAccountDefault() != null ? request.getBankAccountDefault().trim() : null);
        existing.setCostCenter(request.getCostCenter() != null ? request.getCostCenter().trim() : null);
        existing.setCostCenterDefault(request.getCostCenterDefault() != null ? request.getCostCenterDefault().trim() : null);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return toResponse(userSetupRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteUserSetup(Long id) {
        AfmUserSetup existing = findSetup(id);
        userSetupRepository.delete(existing);
    }

    @Override
    public List<UserOptionDto> getAvailableUsers(Long bgId, Long buId) {
        List<User> users;
        if (bgId != null && buId != null) {
            users = userRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null) {
            users = userRepository.findByBuIdOrderByIdDesc(buId);
        } else {
            users = userRepository.findAllByOrderByIdDesc();
        }

        List<UserOptionDto> list = new ArrayList<>();
        for (User user : users) {
            if (user.getStatus() != null && user.getStatus() != 1) continue;

            Long empId = null;
            String empCode = null;
            String empName = null;

            List<UserEmplMap> maps = userEmplMapRepository.findByUserIdOrderByIdAsc(user.getId());
            UserEmplMap activeMap = maps.stream()
                    .filter(m -> m.getStatus() == null || m.getStatus() == 1)
                    .findFirst()
                    .orElse(null);

            if (activeMap != null && activeMap.getEmplId() != null) {
                empId = activeMap.getEmplId();
                Optional<Employee> empOpt = employeeRepository.findById(empId);
                if (empOpt.isPresent()) {
                    empCode = empOpt.get().getEmployeeCode();
                    empName = empOpt.get().getEmployeeName();
                }
            }

            String label = String.format("%s : %s : %s",
                    user.getUserName() != null ? user.getUserName() : "-",
                    empCode != null ? empCode : "-",
                    empName != null ? empName : (user.getFullName() != null ? user.getFullName() : "-"));

            list.add(UserOptionDto.builder()
                    .userId(user.getId())
                    .userName(user.getUserName())
                    .employeeId(empId)
                    .employeeCode(empCode)
                    .employeeName(empName)
                    .displayLabel(label)
                    .build());
        }
        return list;
    }

    private AfmUserSetup findSetup(Long id) {
        return userSetupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Setup not found with id: " + id));
    }

    private AfmUserSetupResponseDto toResponse(AfmUserSetup entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String userName = null;
        Long empId = null;
        String empCode = null;
        String empName = null;

        if (entity.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(entity.getUserId());
            if (userOpt.isPresent()) {
                userName = userOpt.get().getUserName();
                List<UserEmplMap> maps = userEmplMapRepository.findByUserIdOrderByIdAsc(entity.getUserId());
                UserEmplMap activeMap = maps.stream()
                        .filter(m -> m.getStatus() == null || m.getStatus() == 1)
                        .findFirst()
                        .orElse(null);
                if (activeMap != null && activeMap.getEmplId() != null) {
                    empId = activeMap.getEmplId();
                    Optional<Employee> empOpt = employeeRepository.findById(empId);
                    if (empOpt.isPresent()) {
                        empCode = empOpt.get().getEmployeeCode();
                        empName = empOpt.get().getEmployeeName();
                    }
                }
            }
        }

        String displayLabel = String.format("%s : %s : %s",
                userName != null ? userName : "-",
                empCode != null ? empCode : "-",
                empName != null ? empName : "-");

        // Resolve default names
        String voucherTypeDefaultName = entity.getVoucherTypeDefault();
        if (entity.getVoucherTypeDefault() != null && entity.getVoucherTypeDefault().matches("\\d+")) {
            voucherTypeDefaultName = lookupValueRepository.findById(Long.parseLong(entity.getVoucherTypeDefault()))
                    .map(v -> v.getValue() != null ? v.getValue() : v.getValueCode())
                    .orElse(entity.getVoucherTypeDefault());
        }

        String locationDefaultName = entity.getLocationDefault();
        if (entity.getLocationDefault() != null && entity.getLocationDefault().matches("\\d+")) {
            locationDefaultName = userLocationRepository.findById(Long.parseLong(entity.getLocationDefault()))
                    .map(AfmUserLocation::getLocation)
                    .orElse(entity.getLocationDefault());
        }

        String bankAccountDefaultName = entity.getBankAccountDefault();
        if (entity.getBankAccountDefault() != null && entity.getBankAccountDefault().matches("\\d+")) {
            bankAccountDefaultName = bankDtlsRepository.findById(Long.parseLong(entity.getBankAccountDefault()))
                    .map(b -> {
                        String bankName = bankRepository.findById(b.getAfmBankId())
                                .map(AfmBank::getBankName)
                                .orElse("");
                        String accNum = b.getAccountNumber() != null ? b.getAccountNumber() : "";
                        String branch = (b.getBranchName() != null && !b.getBranchName().trim().isEmpty())
                                ? " (" + b.getBranchName().trim() + ")" : "";
                        return (!bankName.isEmpty() ? bankName + " : " : "") + accNum + branch;
                    })
                    .orElse(entity.getBankAccountDefault());
        }

        String costCenterDefaultName = entity.getCostCenterDefault();
        if (entity.getCostCenterDefault() != null && entity.getCostCenterDefault().matches("\\d+")) {
            costCenterDefaultName = departmentRepository.findById(Long.parseLong(entity.getCostCenterDefault()))
                    .map(Department::getDepartmentName)
                    .orElse(entity.getCostCenterDefault());
        }

        // Audit names
        String createdUserName = entity.getCreatedUserId() != null
                ? userRepository.findById(entity.getCreatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String createdEmpName = entity.getCreatedEmpId() != null
                ? employeeRepository.findById(entity.getCreatedEmpId())
                .map(e -> e.getEmployeeCode() + ": " + e.getEmployeeName()).orElse(null)
                : null;
        String updatedUserName = entity.getUpdatedUserId() != null
                ? userRepository.findById(entity.getUpdatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String updatedEmpName = entity.getUpdatedEmpId() != null
                ? employeeRepository.findById(entity.getUpdatedEmpId())
                .map(e -> e.getEmployeeCode() + ": " + e.getEmployeeName()).orElse(null)
                : null;

        return AfmUserSetupResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .userId(entity.getUserId())
                .userName(userName)
                .employeeId(empId)
                .employeeCode(empCode)
                .employeeName(empName)
                .userDisplayLabel(displayLabel)
                .voucherDate(entity.getVoucherDate())
                .voucherType(entity.getVoucherType())
                .voucherTypeDefault(entity.getVoucherTypeDefault())
                .voucherTypeDefaultName(voucherTypeDefaultName)
                .location(entity.getLocation())
                .locationDefault(entity.getLocationDefault())
                .locationDefaultName(locationDefaultName)
                .bankAccount(entity.getBankAccount())
                .bankAccountDefault(entity.getBankAccountDefault())
                .bankAccountDefaultName(bankAccountDefaultName)
                .costCenter(entity.getCostCenter())
                .costCenterDefault(entity.getCostCenterDefault())
                .costCenterDefaultName(costCenterDefaultName)
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
        if (bgId == null) throw new BadRequestException("Business Group id cannot be empty");
        if (buId == null) throw new BadRequestException("Business Unit id cannot be empty");

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
