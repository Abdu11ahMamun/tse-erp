package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.Role;
import com.tse.erp.module.admin.repository.RoleRepository;
import com.tse.erp.module.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + id));
    }

    @Override
    public Role createRole(Role role) {

        validateRoleName(role.getRoleName());
        StatusUtil.validate(role.getStatus());

        String name = role.getRoleName().trim();

        // Duplicate check
        boolean exists = !roleRepository
                .findByRoleNameIgnoreCase(name)
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Role already exists with name: " + name);
        }

        role.setId(null);
        role.setRoleName(name);

        // Status default 1, BaseEntity @PrePersist handle korbe
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {

        Role existing = getRoleById(id);

        validateRoleName(role.getRoleName());
        StatusUtil.validate(role.getStatus());

        String name = role.getRoleName().trim();

        // Duplicate check, nijer id bade
        boolean duplicateExists = roleRepository
                .findByRoleNameIgnoreCase(name)
                .stream()
                .anyMatch(r -> !r.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Role already exists with name: " + name);
        }

        existing.setRoleName(name);
        if (role.getStatus() != null) {
            existing.setStatus(role.getStatus());
        }

        // BaseEntity @PreUpdate handle korbe
        return roleRepository.save(existing);
    }

    @Override
    public void deleteRole(Long id) {
        Role existing = getRoleById(id);
        // TODO: Users ba Role Details thakle delete block (existsByRoleId)
        roleRepository.delete(existing);
    }

    private void validateRoleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Role name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Role name must be at least 2 characters");
        }
        if (name.trim().length() > 100) {
            throw new BadRequestException(
                    "Role name cannot exceed 100 characters");
        }
    }
}