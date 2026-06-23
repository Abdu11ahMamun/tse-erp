package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.UserType;
import com.tse.erp.module.admin.repository.UserTypeRepository;
import com.tse.erp.module.admin.service.UserTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTypeServiceImpl implements UserTypeService {

    private final UserTypeRepository userTypeRepository;

    @Override
    public List<UserType> getAllUserTypes() {
        return userTypeRepository.findAllByOrderByIdDesc();
    }

    @Override
    public UserType getUserTypeById(Long id) {
        return userTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserType not found with id: " + id));
    }

    @Override
    public UserType createUserType(UserType userType) {

        if (userType.getUserType() == null ||
                userType.getUserType().trim().isEmpty()) {
            throw new BadRequestException(
                    "User type name cannot be empty");
        }

        boolean exists = !userTypeRepository
                .findByUserTypeIgnoreCase(userType.getUserType().trim())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "User type already exists: " + userType.getUserType());
        }

        userType.setUserType(userType.getUserType().trim());
        userType.setIsActive(1);
        userType.setCreatedAt(LocalDateTime.now());
        userType.setUpdatedAt(LocalDateTime.now());

        return userTypeRepository.save(userType);
    }

    @Override
    public UserType updateUserType(Long id, UserType userType) {

        UserType existing = getUserTypeById(id);

        if (userType.getUserType() == null ||
                userType.getUserType().trim().isEmpty()) {
            throw new BadRequestException(
                    "User type name cannot be empty");
        }

        List<UserType> found = userTypeRepository
                .findByUserTypeIgnoreCase(userType.getUserType().trim());

        boolean duplicateExists = found.stream()
                .anyMatch(u -> !u.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "User type already exists: " + userType.getUserType());
        }

        existing.setUserType(userType.getUserType().trim());
        existing.setIsActive(userType.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());

        return userTypeRepository.save(existing);
    }

    @Override
    public void deleteUserType(Long id) {
        UserType existing = getUserTypeById(id);
        userTypeRepository.delete(existing);
    }
}