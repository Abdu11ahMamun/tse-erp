package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.dto.EmployeeUserCreateDto;
import com.tse.erp.module.admin.dto.EmployeeUserDto;
import com.tse.erp.module.admin.entity.Role;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.UserEmplMap;
import com.tse.erp.module.admin.entity.UserType;
import com.tse.erp.module.admin.repository.BuRoleMapRepository;
import com.tse.erp.module.admin.repository.RoleRepository;
import com.tse.erp.module.admin.repository.UserEmplMapRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.admin.repository.UserTypeRepository;
import com.tse.erp.module.admin.service.EmployeeUserService;
import com.tse.erp.module.admin.service.UserService;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeUserServiceImpl implements EmployeeUserService {

    private final EmployeeRepository employeeRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserEmplMapRepository userEmplMapRepository;
    private final RoleRepository roleRepository;
    private final BuRoleMapRepository buRoleMapRepository;
    private final UserTypeRepository userTypeRepository;

    // =========================================
    // GET (UI: User Creation button dekhabe kina)
    // =========================================
    @Override
    public EmployeeUserDto getByEmployee(Long employeeId) {
        Employee e = findEmployee(employeeId);
        EmployeeUserDto d = baseDto(e);

        activeLink(employeeId).ifPresent(link ->
                userRepository.findById(link.getUserId()).ifPresent(u -> {
                    d.setUserId(u.getId());
                    d.setUserName(u.getUserName());
                    d.setRoleId(u.getRoleId());
                    d.setUserTypeId(u.getUserTypeId());
                    d.setStatus(u.getStatus());
                }));
        return d;
    }

    // =========================================
    // CREATE USER FOR EMPLOYEE
    // =========================================
    @Override
    @Transactional
    public EmployeeUserDto createForEmployee(
            Long employeeId, EmployeeUserCreateDto dto) {

        Employee e = findEmployee(employeeId);

        if (!Integer.valueOf(1).equals(e.getStatus())) {
            throw new BadRequestException("Selected Employee is not active");
        }
        if (activeLink(employeeId).isPresent()) {
            throw new DuplicateResourceException(
                    "A user already exists for this Employee");
        }

        // Confirm Password (backend-e check hoy, UserService-e na)
        if (dto.getPassword() == null
                || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException(
                    "Password and Confirm Password do not match");
        }

        // Role: active + oi BU-te assign kora
        if (dto.getRoleId() == null) {
            throw new BadRequestException("User role cannot be empty");
        }
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + dto.getRoleId()));
        if (!Integer.valueOf(1).equals(role.getStatus())) {
            throw new BadRequestException("Selected Role is not active");
        }
        if (buRoleMapRepository
                .findByBuIdAndRoleId(e.getBuId(), dto.getRoleId()).isEmpty()) {
            throw new BadRequestException(
                    "Role is not assigned to the Employee's Business Unit");
        }

        // User Type: active + same BG
        if (dto.getUserTypeId() == null) {
            throw new BadRequestException("User type cannot be empty");
        }
        UserType ut = userTypeRepository.findById(dto.getUserTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User Type not found with id: " + dto.getUserTypeId()));
        if (!Integer.valueOf(1).equals(ut.getStatus())) {
            throw new BadRequestException("Selected User Type is not active");
        }
        if (ut.getBgId() != null && !ut.getBgId().equals(e.getBgId())) {
            throw new BadRequestException(
                    "User Type does not belong to the Employee's Business Group");
        }

        // Android Id Fixed (Yes/No)
        Integer fixed = dto.getIsAndroidFixed();
        if (fixed == null) {
            throw new BadRequestException(
                    "Android Id Fixed cannot be empty");
        }
        if (fixed != 0 && fixed != 1) {
            throw new BadRequestException("Android Id Fixed must be 0 or 1");
        }
        String androidId = null;
        if (fixed == 1) {
            if (dto.getAndroidId() == null || dto.getAndroidId().isBlank()) {
                throw new BadRequestException(
                        "Android Id is required when Android Id Fixed is Yes");
            }
            androidId = dto.getAndroidId().trim();
            if (androidId.length() > 100) {
                throw new BadRequestException(
                        "Android Id cannot exceed 100 characters");
            }
        }

        // User build: BG, BU, Dept, Desig, naam, email, mobile employee theke
        User u = new User();
        u.setBgId(e.getBgId());
        u.setBuId(e.getBuId());
        u.setDeptId(e.getDeptId());
        u.setDesigId(e.getDesigId());
        u.setUserName(dto.getUserName());
        u.setFullName(limit(e.getEmployeeName(), 100));
        u.setEmail(e.getEmail() != null && e.getEmail().length() <= 100
                ? e.getEmail() : null);
        u.setMobileNo(e.getMobileNo());
        u.setPassword(dto.getPassword());
        u.setFromDate(dto.getFromDate() == null
                ? null : dto.getFromDate().atStartOfDay());
        u.setToDate(dto.getToDate() == null
                ? null : dto.getToDate().atTime(LocalTime.of(23, 59, 59)));
        u.setRoleId(dto.getRoleId());
        u.setUserTypeId(dto.getUserTypeId());
        u.setIsAndroidFixed(fixed);
        u.setAndroidId(androidId);
        u.setStatus(dto.getStatus());

        // Username unique, password rule, date, status validation UserService-e
        User saved = userService.createUser(u);

        // Employee link (ekই transaction)
        UserEmplMap map = new UserEmplMap();
        map.setUserId(saved.getId());
        map.setEmplId(employeeId);
        map.setStatus(1);
        userEmplMapRepository.save(map);

        EmployeeUserDto d = baseDto(e);
        d.setUserId(saved.getId());
        d.setUserName(saved.getUserName());
        d.setRoleId(saved.getRoleId());
        d.setUserTypeId(saved.getUserTypeId());
        d.setStatus(saved.getStatus());
        return d;
    }

    // =========================================
    // HELPERS
    // =========================================
    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));
    }

    private Optional<UserEmplMap> activeLink(Long employeeId) {
        List<UserEmplMap> rows =
                userEmplMapRepository.findByEmplIdAndStatus(employeeId, 1);
        return rows.stream().findFirst();
    }

    private EmployeeUserDto baseDto(Employee e) {
        EmployeeUserDto d = new EmployeeUserDto();
        d.setEmployeeId(e.getId());
        d.setEmployeeName(e.getEmployeeName());
        d.setEmployeeCode(e.getEmployeeCode());
        d.setBgId(e.getBgId());
        d.setBuId(e.getBuId());
        d.setDeptId(e.getDeptId());
        d.setDesigId(e.getDesigId());
        return d;
    }

    private String limit(String v, int max) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}