package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.common.ValidationUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.UserType;
import com.tse.erp.module.admin.repository.UserTypeGroupRepository;
import com.tse.erp.module.admin.repository.UserTypeRepository;
import com.tse.erp.module.admin.service.UserTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTypeServiceImpl implements UserTypeService {

    private final UserTypeRepository userTypeRepository;
    private final UserTypeGroupRepository userTypeGroupRepository;

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

        validateTypeName(userType.getTypeName());
        validateShortName(userType.getShortName());
        validateSortOrder(userType.getSortOrder());
        StatusUtil.validate(userType.getStatus());

        String name = userType.getTypeName().trim();
        String shortName = normalizeShortName(userType.getShortName());

        if (!userTypeRepository.findByTypeNameIgnoreCase(name).isEmpty()) {
            throw new DuplicateResourceException(
                    "User type already exists: " + name);
        }

        if (shortName != null && !userTypeRepository
                .findByShortNameIgnoreCase(shortName).isEmpty()) {
            throw new DuplicateResourceException(
                    "Short name already exists: " + shortName);
        }

        userType.setId(null);
        userType.setTypeName(name);
        userType.setShortName(shortName);

        // Status default 1 — BaseEntity @PrePersist handle korbe
        return userTypeRepository.save(userType);
    }

    @Override
    public UserType updateUserType(Long id, UserType userType) {

        UserType existing = getUserTypeById(id);

        validateTypeName(userType.getTypeName());
        validateShortName(userType.getShortName());
        validateSortOrder(userType.getSortOrder());
        StatusUtil.validate(userType.getStatus());

        String name = userType.getTypeName().trim();
        String shortName = normalizeShortName(userType.getShortName());

        boolean nameDuplicate = userTypeRepository
                .findByTypeNameIgnoreCase(name).stream()
                .anyMatch(u -> !u.getId().equals(id));
        if (nameDuplicate) {
            throw new DuplicateResourceException(
                    "User type already exists: " + name);
        }

        if (shortName != null) {
            boolean shortDuplicate = userTypeRepository
                    .findByShortNameIgnoreCase(shortName).stream()
                    .anyMatch(u -> !u.getId().equals(id));
            if (shortDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setBgId(userType.getBgId());
        existing.setTypeName(name);
        existing.setShortName(shortName);
        existing.setSortOrder(userType.getSortOrder());
        if (userType.getStatus() != null) {
            existing.setStatus(userType.getStatus());
        }

        return userTypeRepository.save(existing);
    }

    @Override
    public void deleteUserType(Long id) {
        UserType existing = getUserTypeById(id);
        if (userTypeGroupRepository.existsByUserTypeId(id)) {
            throw new BadRequestException(
                    "Cannot delete: User Type is mapped to Business Unit(s)");
        }
        userTypeRepository.delete(existing);
    }

    private void validateTypeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("User type name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "User type name must be at least 2 characters");
        }
        if (name.trim().length() > 50) {
            throw new BadRequestException(
                    "User type name cannot exceed 50 characters");
        }
    }

    private void validateShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return;
        }
        if (shortName.trim().length() < 2) {
            throw new BadRequestException(
                    "Short name must be at least 2 characters");
        }
        if (shortName.trim().length() > 100) {
            throw new BadRequestException(
                    "Short name cannot exceed 100 characters");
        }
    }

    private String normalizeShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return null;
        }
        return shortName.trim();
    }

    private void validateSortOrder(java.math.BigDecimal sortOrder) {
        ValidationUtil.validateSortOrder(sortOrder);
    }
}