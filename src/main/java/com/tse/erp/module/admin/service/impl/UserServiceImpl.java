package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

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

        // Validation
        if (user.getUserName() == null ||
                user.getUserName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Username cannot be empty");
        }

        if (user.getEmail() == null ||
                user.getEmail().trim().isEmpty()) {
            throw new BadRequestException(
                    "Email cannot be empty");
        }

        if (user.getPassword() == null ||
                user.getPassword().trim().isEmpty()) {
            throw new BadRequestException(
                    "Password cannot be empty");
        }

        // Duplicate username check
        boolean usernameExists = !userRepository
                .findByUserNameIgnoreCase(user.getUserName().trim())
                .isEmpty();

        if (usernameExists) {
            throw new DuplicateResourceException(
                    "Username already exists: " + user.getUserName());
        }

        // Duplicate email check
        boolean emailExists = !userRepository
                .findByEmailIgnoreCase(user.getEmail().trim())
                .isEmpty();

        if (emailExists) {
            throw new DuplicateResourceException(
                    "Email already exists: " + user.getEmail());
        }

        // Password hash koro
        user.setPassword(passwordEncoder.encode(
                user.getPassword()));

        user.setUserName(user.getUserName().trim());
        user.setEmail(user.getEmail().trim());
        user.setIsActive(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {

        User existing = getUserById(id);

        // Validation
        if (user.getUserName() == null ||
                user.getUserName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Username cannot be empty");
        }

        if (user.getEmail() == null ||
                user.getEmail().trim().isEmpty()) {
            throw new BadRequestException(
                    "Email cannot be empty");
        }

        // Duplicate username check — nijer id bade
        List<User> foundUsername = userRepository
                .findByUserNameIgnoreCase(user.getUserName().trim());

        boolean usernameDuplicate = foundUsername.stream()
                .anyMatch(u -> !u.getId().equals(id));

        if (usernameDuplicate) {
            throw new DuplicateResourceException(
                    "Username already exists: " + user.getUserName());
        }

        // Duplicate email check — nijer id bade
        List<User> foundEmail = userRepository
                .findByEmailIgnoreCase(user.getEmail().trim());

        boolean emailDuplicate = foundEmail.stream()
                .anyMatch(u -> !u.getId().equals(id));

        if (emailDuplicate) {
            throw new DuplicateResourceException(
                    "Email already exists: " + user.getEmail());
        }

        existing.setUserName(user.getUserName().trim());
        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail().trim());
        existing.setMobileNo(user.getMobileNo());
        existing.setRoleId(user.getRoleId());
        existing.setUserTypeId(user.getUserTypeId());
        existing.setIsActive(user.getIsActive());
        existing.setFromDate(user.getFromDate());
        existing.setToDate(user.getToDate());
        existing.setGender(user.getGender());
        existing.setUpdatedAt(LocalDateTime.now());

        // Password change korte chaile
        if (user.getPassword() != null &&
                !user.getPassword().trim().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(
                    user.getPassword()));
        }

        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        User existing = getUserById(id);
        userRepository.delete(existing);
    }
}