package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.UserEmplMap;
import com.tse.erp.module.admin.repository.UserEmplMapRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.admin.service.UserService;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final UserEmplMapRepository userEmplMapRepository;

    @Override
    public List<User> getAllUsers() {
        List<User> list = userRepository.findAllByOrderByIdDesc();
        attachEmployeeIds(list);
        return list;
    }

    @Override
    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        attachEmployeeId(user);
        return user;
    }

    @Override
    @Transactional
    public User createUser(User user) {

        validateUserName(user.getUserName());
        validateFullName(user.getFullName());
        String email = normalizeEmail(user.getEmail());
        String mobile = normalizeMobile(user.getMobileNo());
        validatePassword(user.getPassword());
        validateDates(user);
        StatusUtil.validate(user.getStatus());

        // Employee must
        Employee employee = requireAssignableEmployee(user.getEmployeeId());

        String userName = user.getUserName().trim();

        if (!userRepository.findByUserNameIgnoreCase(userName).isEmpty()) {
            throw new DuplicateResourceException(
                    "Username already exists: " + userName);
        }

        if (email != null && !userRepository
                .findByEmailIgnoreCase(email).isEmpty()) {
            throw new DuplicateResourceException(
                    "Email already exists: " + email);
        }

        user.setId(null);
        user.setUserName(userName);
        user.setFullName(user.getFullName().trim());
        user.setEmail(email);
        user.setMobileNo(mobile);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // BG, BU, Dept, Desig employee theke (request-er value ignore)
        user.setBgId(employee.getBgId());
        user.setBuId(employee.getBuId());
        user.setDeptId(employee.getDeptId());
        user.setDesigId(employee.getDesigId());

        // Status default 1, BaseEntity @PrePersist handle korbe
        User saved = userRepository.save(user);

        // Employee link, ekই transaction
        UserEmplMap map = new UserEmplMap();
        map.setUserId(saved.getId());
        map.setEmplId(employee.getId());
        map.setStatus(1);
        userEmplMapRepository.save(map);

        saved.setEmployeeId(employee.getId());
        return saved;
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        validateUserName(user.getUserName());
        validateFullName(user.getFullName());
        String email = normalizeEmail(user.getEmail());
        String mobile = normalizeMobile(user.getMobileNo());
        validateDates(user);
        StatusUtil.validate(user.getStatus());

        // Inactive theke active korte hole employee linked thakte hobe
        boolean activating = Integer.valueOf(1).equals(user.getStatus())
                && !Integer.valueOf(1).equals(existing.getStatus());
        if (activating) {
            requireEmployeeLinked(id);
        }

        String userName = user.getUserName().trim();

        boolean usernameDuplicate = userRepository
                .findByUserNameIgnoreCase(userName).stream()
                .anyMatch(u -> !u.getId().equals(id));
        if (usernameDuplicate) {
            throw new DuplicateResourceException(
                    "Username already exists: " + userName);
        }

        if (email != null) {
            boolean emailDuplicate = userRepository
                    .findByEmailIgnoreCase(email).stream()
                    .anyMatch(u -> !u.getId().equals(id));
            if (emailDuplicate) {
                throw new DuplicateResourceException(
                        "Email already exists: " + email);
            }
        }

        existing.setBgId(user.getBgId());
        existing.setBuId(user.getBuId());
        existing.setDeptId(user.getDeptId());
        existing.setDesigId(user.getDesigId());
        existing.setUserName(userName);
        existing.setFullName(user.getFullName().trim());
        existing.setEmail(email);
        existing.setMobileNo(mobile);
        existing.setRoleId(user.getRoleId());
        existing.setUserTypeId(user.getUserTypeId());
        existing.setFromDate(user.getFromDate());
        existing.setToDate(user.getToDate());
        existing.setIsAndroidFixed(user.getIsAndroidFixed());
        existing.setAndroidId(user.getAndroidId());
        if (user.getStatus() != null) {
            existing.setStatus(user.getStatus());
        }

        // Password change korte chaile
        if (user.getPassword() != null
                && !user.getPassword().trim().isEmpty()) {
            validatePassword(user.getPassword());
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        User saved = userRepository.save(existing);
        attachEmployeeId(saved);
        return saved;
    }

    @Override
    @Transactional
    public User changeStatus(Long id, Integer status) {
        if (status == null) {
            throw new BadRequestException("Status cannot be empty");
        }
        StatusUtil.validate(status);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        // Nijeke inactive kora jabe na
        if (status == 0) {
            CurrentUser.get().ifPresent(u -> {
                if (u.userId().equals(id)) {
                    throw new BadRequestException(
                            "You cannot deactivate your own account");
                }
            });
        }

        // Active korte hole employee linked thakte hobe
        if (status == 1 && !Integer.valueOf(1).equals(existing.getStatus())) {
            requireEmployeeLinked(id);
        }

        existing.setStatus(status);
        User saved = userRepository.save(existing);
        attachEmployeeId(saved);
        return saved;
    }

    @Override
    public void deleteUser(Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        userRepository.delete(existing);
    }

    // =========================================
    // EMPLOYEE HELPERS
    // =========================================
    private Employee requireAssignableEmployee(Long employeeId) {
        if (employeeId == null) {
            throw new BadRequestException(
                    "Employee is required to create a user");
        }
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));
        if (!Integer.valueOf(1).equals(e.getStatus())) {
            throw new BadRequestException("Selected Employee is not active");
        }
        if (!userEmplMapRepository
                .findByEmplIdAndStatus(employeeId, 1).isEmpty()) {
            throw new DuplicateResourceException(
                    "Employee is already assigned to another user");
        }
        return e;
    }

    private void requireEmployeeLinked(Long userId) {
        boolean linked = userEmplMapRepository
                .findByUserIdOrderByIdAsc(userId).stream()
                .anyMatch(m -> Integer.valueOf(1).equals(m.getStatus()));
        if (!linked) {
            throw new BadRequestException(
                    "Assign an Employee to this user before activating it");
        }
    }

    private void attachEmployeeId(User user) {
        user.setEmployeeId(userEmplMapRepository
                .findByUserIdOrderByIdAsc(user.getId()).stream()
                .filter(m -> Integer.valueOf(1).equals(m.getStatus()))
                .map(UserEmplMap::getEmplId)
                .findFirst()
                .orElse(null));
    }

    // List-er jonno ekbar-i query (N+1 na)
    private void attachEmployeeIds(List<User> users) {
        Map<Long, Long> byUser = new HashMap<>();
        for (UserEmplMap m : userEmplMapRepository.findByStatus(1)) {
            byUser.putIfAbsent(m.getUserId(), m.getEmplId());
        }
        users.forEach(u -> u.setEmployeeId(byUser.get(u.getId())));
    }

    // =========================================
    // VALIDATION HELPERS
    // =========================================
    private void validateUserName(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (v.trim().length() < 2) {
            throw new BadRequestException(
                    "Username must be at least 2 characters");
        }
        if (v.trim().length() > 20) {
            throw new BadRequestException(
                    "Username cannot exceed 20 characters");
        }
    }

    private void validateFullName(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException("Full name cannot be empty");
        }
        if (v.trim().length() < 2) {
            throw new BadRequestException(
                    "Full name must be at least 2 characters");
        }
        if (v.trim().length() > 100) {
            throw new BadRequestException(
                    "Full name cannot exceed 100 characters");
        }
    }

    // Email optional
    private String normalizeEmail(String v) {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        String e = v.trim();
        if (e.length() > 100) {
            throw new BadRequestException(
                    "Email cannot exceed 100 characters");
        }
        if (!e.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Invalid email format");
        }
        return e;
    }

    // Mobile optional, BD format
    private String normalizeMobile(String v) {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        String m = v.trim();
        if (!m.matches("^01[0-9]{9}$")) {
            throw new BadRequestException(
                    "Invalid mobile number. Must be 11 digits starting with 01");
        }
        return m;
    }

    private void validatePassword(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        if (!v.matches(PASSWORD_REGEX)) {
            throw new BadRequestException(
                    "Password must be 8-64 characters and contain an uppercase letter, "
                            + "a lowercase letter, a number and a special character");
        }
    }

    private void validateDates(User user) {
        if (user.getFromDate() != null && user.getToDate() != null
                && user.getFromDate().isAfter(user.getToDate())) {
            throw new BadRequestException(
                    "Valid From Date cannot be after Valid To Date");
        }
    }
}