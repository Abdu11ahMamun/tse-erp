package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.Permission;
import com.tse.erp.module.admin.repository.ModuleRepository;
import com.tse.erp.module.admin.repository.PermissionRepository;
import com.tse.erp.module.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        String name = validateAndGetName(permission);
        StatusUtil.validate(permission.getStatus());

        // Duplicate check, same module + same name
        boolean exists = !permissionRepository
                .findByPermissionNameIgnoreCaseAndModuleId(
                        name, permission.getModuleId())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Permission '" + name
                            + "' already exists in this module");
        }

        permission.setId(null);
        permission.setPermissionName(name);

        // Status default 1, BaseEntity @PrePersist handle korbe
        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Long id, Permission permission) {

        Permission existing = getPermissionById(id);

        String name = validateAndGetName(permission);
        StatusUtil.validate(permission.getStatus());

        // Duplicate check, nijer id bade
        boolean duplicateExists = permissionRepository
                .findByPermissionNameIgnoreCaseAndModuleId(
                        name, permission.getModuleId())
                .stream()
                .anyMatch(p -> !p.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Permission '" + name
                            + "' already exists in this module");
        }

        existing.setPermissionName(name);
        existing.setModuleId(permission.getModuleId());
        if (permission.getStatus() != null) {
            existing.setStatus(permission.getStatus());
        }

        // BaseEntity @PreUpdate handle korbe
        return permissionRepository.save(existing);
    }

    @Override
    public void deletePermission(Long id) {
        Permission existing = getPermissionById(id);
        // TODO: Menu e use hole delete block
        permissionRepository.delete(existing);
    }

    // Name + module validate kore trimmed name return kore
    private String validateAndGetName(Permission permission) {
        String v = permission.getPermissionName();
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException(
                    "Permission name cannot be empty");
        }
        if (v.trim().length() < 2) {
            throw new BadRequestException(
                    "Permission name must be at least 2 characters");
        }
        if (v.trim().length() > 100) {
            throw new BadRequestException(
                    "Permission name cannot exceed 100 characters");
        }
        if (permission.getModuleId() == null) {
            throw new BadRequestException("Module id cannot be empty");
        }
        moduleRepository.findById(permission.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + permission.getModuleId()));
        return v.trim();
    }
}