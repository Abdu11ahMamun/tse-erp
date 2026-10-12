package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    @Override
    public User createUser(User user) {

        validateUserName(user.getUserName());
        validateFullName(user.getFullName());
        String email = normalizeEmail(user.getEmail());
        String mobile = normalizeMobile(user.getMobileNo());
        validatePassword(user.getPassword());
        validateDates(user);
        StatusUtil.validate(user.getStatus());

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

        // Status default 1 — BaseEntity @PrePersist handle korbe
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {

        User existing = getUserById(id);

        validateUserName(user.getUserName());
        validateFullName(user.getFullName());
        String email = normalizeEmail(user.getEmail());
        String mobile = normalizeMobile(user.getMobileNo());
        validateDates(user);
        StatusUtil.validate(user.getStatus());

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

        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        User existing = getUserById(id);
        userRepository.delete(existing);
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

    // Email optional (spec: Required = No)
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