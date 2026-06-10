package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.RoleDetail;
import com.tse.erp.module.admin.repository.ModuleRepository;
import com.tse.erp.module.admin.repository.RoleDetailRepository;
import com.tse.erp.module.admin.repository.RoleRepository;
import com.tse.erp.module.admin.service.RoleDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleDetailServiceImpl implements RoleDetailService {

    private final RoleDetailRepository roleDetailRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;

    @Override
    public List<RoleDetail> getAllRoleDetails() {
        return roleDetailRepository.findAll();
    }

    @Override
    public List<RoleDetail> getRoleDetailsByRoleId(Long roleId) {
        // Role exist kore kina check
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + roleId));

        return roleDetailRepository.findByRoleId(roleId);
    }

    @Override
    public RoleDetail getRoleDetailById(Long id) {
        return roleDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role detail not found with id: " + id));
    }

    @Override
    public RoleDetail createRoleDetail(RoleDetail roleDetail) {

        // Validation
        if (roleDetail.getRoleId() == null) {
            throw new BadRequestException("Role id cannot be empty");
        }

        if (roleDetail.getModuleId() == null) {
            throw new BadRequestException("Module id cannot be empty");
        }

        if (roleDetail.getMenuId() == null) {
            throw new BadRequestException("Menu id cannot be empty");
        }

        if (roleDetail.getPermissionId() == null ||
                roleDetail.getPermissionId().trim().isEmpty()) {
            throw new BadRequestException(
                    "Permission id cannot be empty");
        }

        // Role exist kore kina check
        roleRepository.findById(roleDetail.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: "
                                + roleDetail.getRoleId()));

        // Module exist kore kina check
        moduleRepository.findById(roleDetail.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + roleDetail.getModuleId()));

        // ✅ Duplicate check — same role + module + menu
        boolean exists = !roleDetailRepository
                .findByRoleIdAndModuleIdAndMenuId(
                        roleDetail.getRoleId(),
                        roleDetail.getModuleId(),
                        roleDetail.getMenuId())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Role detail already exists for " +
                            "role id: " + roleDetail.getRoleId() +
                            ", module id: " + roleDetail.getModuleId() +
                            ", menu id: " + roleDetail.getMenuId());
        }

        return roleDetailRepository.save(roleDetail);
    }

    @Override
    public RoleDetail updateRoleDetail(Long id, RoleDetail roleDetail) {

        RoleDetail existing = getRoleDetailById(id);

        // Validation
        if (roleDetail.getRoleId() == null) {
            throw new BadRequestException("Role id cannot be empty");
        }

        if (roleDetail.getModuleId() == null) {
            throw new BadRequestException("Module id cannot be empty");
        }

        if (roleDetail.getMenuId() == null) {
            throw new BadRequestException("Menu id cannot be empty");
        }

        if (roleDetail.getPermissionId() == null ||
                roleDetail.getPermissionId().trim().isEmpty()) {
            throw new BadRequestException(
                    "Permission id cannot be empty");
        }

        // Role exist kore kina check
        roleRepository.findById(roleDetail.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: "
                                + roleDetail.getRoleId()));

        // Module exist kore kina check
        moduleRepository.findById(roleDetail.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + roleDetail.getModuleId()));

        // Duplicate check — nijer id bade
        List<RoleDetail> found = roleDetailRepository
                .findByRoleIdAndModuleIdAndMenuId(
                        roleDetail.getRoleId(),
                        roleDetail.getModuleId(),
                        roleDetail.getMenuId());

        boolean duplicateExists = found.stream()
                .anyMatch(r -> !r.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Role detail already exists for " +
                            "role id: " + roleDetail.getRoleId() +
                            ", module id: " + roleDetail.getModuleId() +
                            ", menu id: " + roleDetail.getMenuId());
        }

        existing.setRoleId(roleDetail.getRoleId());
        existing.setModuleId(roleDetail.getModuleId());
        existing.setMenuId(roleDetail.getMenuId());
        existing.setPermissionId(roleDetail.getPermissionId());

        return roleDetailRepository.save(existing);
    }

    @Override
    public void deleteRoleDetail(Long id) {
        RoleDetail existing = getRoleDetailById(id);
        roleDetailRepository.delete(existing);
    }
}