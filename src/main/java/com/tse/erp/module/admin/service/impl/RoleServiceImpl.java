package com.tse.erp.module.admin.service.impl;

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
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + id));
    }

    @Override
    public Role createRole(Role role) {

        // Validation
        if (role.getRoleName() == null ||
                role.getRoleName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Role name cannot be empty");
        }

        boolean exists = !roleRepository
                .findByRoleNameIgnoreCase(role.getRoleName().trim())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Role already exists with name: " + role.getRoleName());
        }

        role.setRoleName(role.getRoleName().trim());
        role.setIsActive(1);

        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {

        Role existing = getRoleById(id);

        // Validation
        if (role.getRoleName() == null ||
                role.getRoleName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Role name cannot be empty");
        }

        List<Role> found = roleRepository
                .findByRoleNameIgnoreCase(role.getRoleName().trim());

        boolean duplicateExists = found.stream()
                .anyMatch(r -> !r.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Role already exists with name: " + role.getRoleName());
        }

        existing.setRoleName(role.getRoleName().trim());
        existing.setIsActive(role.getIsActive());

        return roleRepository.save(existing);
    }
    @Override
    public void deleteRole(Long id) {
        Role existing = getRoleById(id);
        roleRepository.delete(existing);
    }
}