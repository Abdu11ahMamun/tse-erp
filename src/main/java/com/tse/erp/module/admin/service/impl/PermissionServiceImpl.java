package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.entity.AfmCoa;
import com.tse.erp.module.admin.entity.Permission;
import com.tse.erp.module.admin.repository.ModuleRepository;
import com.tse.erp.module.admin.repository.PermissionRepository;
import com.tse.erp.module.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final ModuleRepository moduleRepository;

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Permission> getPermissionsByModuleId(Long moduleId) {
        // Module exist kore kina check
        moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + moduleId));

        return permissionRepository.findByModuleId(moduleId);
    }

    @Override
    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found with id: " + id));
    }

    @Override
    public Permission createPermission(Permission permission) {

        // Validation
        if (permission.getPermissionName() == null ||
                permission.getPermissionName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Permission name cannot be empty");
        }

        if (permission.getModuleId() == null) {
            throw new BadRequestException(
                    "Module id cannot be empty");
        }

        // Module exist kore kina check
        moduleRepository.findById(permission.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + permission.getModuleId()));

        // Duplicate check — same module e same permission name
        boolean exists = !permissionRepository
                .findByPermissionNameIgnoreCaseAndModuleId(
                        permission.getPermissionName().trim(),
                        permission.getModuleId())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Permission '" + permission.getPermissionName()
                            + "' already exists in this module");
        }

        permission.setPermissionName(permission.getPermissionName().trim());
        permission.setIsActive(1);
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Long id, Permission permission) {

        Permission existing = getPermissionById(id);

        // Validation
        if (permission.getPermissionName() == null ||
                permission.getPermissionName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Permission name cannot be empty");
        }

        if (permission.getModuleId() == null) {
            throw new BadRequestException(
                    "Module id cannot be empty");
        }

        // Module exist kore kina check
        moduleRepository.findById(permission.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + permission.getModuleId()));

        // Duplicate check — nijer id bade
        List<Permission> found = permissionRepository
                .findByPermissionNameIgnoreCaseAndModuleId(
                        permission.getPermissionName().trim(),
                        permission.getModuleId());

        boolean duplicateExists = found.stream()
                .anyMatch(p -> !p.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Permission '" + permission.getPermissionName()
                            + "' already exists in this module");
        }

        existing.setPermissionName(permission.getPermissionName().trim());
        existing.setModuleId(permission.getModuleId());
        existing.setIsActive(permission.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());
        return permissionRepository.save(existing);
    }

    @Override
    public void deletePermission(Long id) {
        Permission existing = getPermissionById(id);
        permissionRepository.delete(existing);
    }
}