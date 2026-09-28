package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.Role;
import com.tse.erp.module.admin.entity.RoleDetail;
import com.tse.erp.module.admin.repository.ModuleRepository;
import com.tse.erp.module.admin.repository.RoleDetailRepository;
import com.tse.erp.module.admin.repository.RoleRepository;
import com.tse.erp.module.admin.service.RoleDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tse.erp.module.admin.dto.*;
import com.tse.erp.module.admin.entity.Menu;
import com.tse.erp.module.admin.entity.Permission;
import com.tse.erp.module.admin.repository.MenuRepository;
import com.tse.erp.module.admin.repository.PermissionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleDetailServiceImpl implements RoleDetailService {

    private final RoleDetailRepository roleDetailRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<RoleDetail> getAllRoleDetails() {
        return roleDetailRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<RoleDetail> getRoleDetailsByRoleId(Long roleId) {
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

        roleRepository.findById(roleDetail.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: "
                                + roleDetail.getRoleId()));

        moduleRepository.findById(roleDetail.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + roleDetail.getModuleId()));

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

        // ✅ BaseEntity @PrePersist handle korbe
        return roleDetailRepository.save(roleDetail);
    }

    @Override
    public RoleDetail updateRoleDetail(Long id, RoleDetail roleDetail) {

        RoleDetail existing = getRoleDetailById(id);

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

        roleRepository.findById(roleDetail.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: "
                                + roleDetail.getRoleId()));

        moduleRepository.findById(roleDetail.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + roleDetail.getModuleId()));

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

        // ✅ BaseEntity @PreUpdate handle korbe
        return roleDetailRepository.save(existing);
    }

    @Override
    public void deleteRoleDetail(Long id) {
        RoleDetail existing = getRoleDetailById(id);
        roleDetailRepository.delete(existing);
    }

    // =========================================
    // GET ROLE WITH GROUPED PERMISSIONS
    // =========================================
    @Override
    public RoleDetailResponseDto getRoleWithGroupedPermissions(
            Long roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + roleId));

        List<RoleDetail> details =
                roleDetailRepository.findByRoleId(roleId);

        List<AssignedPermissionGroupDto> groups = details.stream()
                .map(detail -> {

                    String moduleName = moduleRepository
                            .findById(detail.getModuleId())
                            .map(m -> m.getModuleName())
                            .orElse("Unknown");

                    String menuName = menuRepository
                            .findById(detail.getMenuId())
                            .map(m -> m.getMenuName())
                            .orElse("Unknown");

                    List<PermissionDto> permissionDtos =
                            parsePermissionIds(
                                    detail.getPermissionId())
                                    .stream()
                                    .map(pid -> permissionRepository
                                            .findById(pid)
                                            .map(p -> PermissionDto.builder()
                                                    .id(p.getId())
                                                    .permissionName(
                                                            p.getPermissionName())
                                                    .build())
                                            .orElse(null))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());

                    return AssignedPermissionGroupDto.builder()
                            .id(detail.getId())
                            .moduleId(detail.getModuleId())
                            .moduleName(moduleName)
                            .menuId(detail.getMenuId())
                            .menuName(menuName)
                            .permissions(permissionDtos)
                            .build();
                })
                .collect(Collectors.toList());

        int assignedMenuCount = groups.size();
        int assignedPermissionCount = groups.stream()
                .mapToInt(g -> g.getPermissions().size())
                .sum();

        String createdAt = role.getCreatedDatetime() != null
                ? role.getCreatedDatetime().toLocalDate().toString()
                : null;

        return RoleDetailResponseDto.builder()
                .roleId(role.getId())
                .roleName(role.getRoleName())
                .status(role.getIsActive() == 1)
                .createdAt(createdAt)
                .assignedMenuCount(assignedMenuCount)
                .assignedPermissionCount(assignedPermissionCount)
                .assignedPermissions(groups)
                .build();
    }

    // =========================================
    // GET AVAILABLE PERMISSIONS
    // =========================================
    @Override
    public List<PermissionDto> getAvailablePermissions(
            Long roleId, Long moduleId, Long menuId) {

        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + roleId));

        moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + moduleId));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found with id: " + menuId));

        if (!menu.getModuleId().equals(moduleId)) {
            throw new BadRequestException(
                    "Menu does not belong to selected module");
        }

        List<Long> menuPermissionIds =
                parsePermissionIds(menu.getPermissionId());

        if (menuPermissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> assignedPermissionIds =
                roleDetailRepository
                        .findFirstByRoleIdAndModuleIdAndMenuId(
                                roleId, moduleId, menuId)
                        .map(rd -> new HashSet<>(
                                parsePermissionIds(rd.getPermissionId())))
                        .orElse(new HashSet<>());

        return menuPermissionIds.stream()
                .filter(pid -> !assignedPermissionIds.contains(pid))
                .map(pid -> permissionRepository.findById(pid)
                        .map(p -> PermissionDto.builder()
                                .id(p.getId())
                                .permissionName(p.getPermissionName())
                                .build())
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================================
    // ASSIGN PERMISSIONS (MERGE SAFE)
    // =========================================
    @Override
    @Transactional
    public RoleDetailResponseDto assignPermissions(
            Long roleId,
            AssignPermissionRequestDto request) {

        Long moduleId = request.getModuleId();
        Long menuId = request.getMenuId();

        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + roleId));

        moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + moduleId));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found with id: " + menuId));

        if (!menu.getModuleId().equals(moduleId)) {
            throw new BadRequestException(
                    "Menu does not belong to selected module");
        }

        if (request.getPermissionIds() == null ||
                request.getPermissionIds().isEmpty()) {
            throw new BadRequestException(
                    "Permission list cannot be empty");
        }

        List<Long> menuPermissionIds =
                parsePermissionIds(menu.getPermissionId());

        List<Long> invalidPermissions =
                request.getPermissionIds().stream()
                        .filter(pid -> !menuPermissionIds.contains(pid))
                        .collect(Collectors.toList());

        if (!invalidPermissions.isEmpty()) {
            throw new BadRequestException(
                    "Permissions " + invalidPermissions +
                            " do not belong to this menu");
        }

        Optional<RoleDetail> existingOpt =
                roleDetailRepository
                        .findFirstByRoleIdAndModuleIdAndMenuId(
                                roleId, moduleId, menuId);

        if (existingOpt.isPresent()) {
            RoleDetail existing = existingOpt.get();
            List<Long> currentIds =
                    parsePermissionIds(existing.getPermissionId());

            List<Long> newIds = request.getPermissionIds()
                    .stream()
                    .filter(pid -> !currentIds.contains(pid))
                    .collect(Collectors.toList());

            currentIds.addAll(newIds);

            List<Long> mergedIds = currentIds.stream()
                    .distinct()
                    .collect(Collectors.toList());

            existing.setPermissionId(
                    serializePermissionIds(mergedIds));
            // ✅ BaseEntity @PreUpdate handle korbe
            roleDetailRepository.save(existing);

        } else {
            List<Long> distinctIds = request.getPermissionIds()
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

            RoleDetail newDetail = RoleDetail.builder()
                    .roleId(roleId)
                    .moduleId(moduleId)
                    .menuId(menuId)
                    .permissionId(serializePermissionIds(distinctIds))
                    .build();

            // ✅ BaseEntity @PrePersist handle korbe
            roleDetailRepository.save(newDetail);
        }

        return getRoleWithGroupedPermissions(roleId);
    }

    // =========================================
    // HELPER METHODS
    // =========================================
    private List<Long> parsePermissionIds(String json) {
        if (json == null || json.trim().isEmpty()
                || json.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String serializePermissionIds(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (Exception e) {
            return "[]";
        }
    }

    // =========================================
    // REMOVE SINGLE PERMISSION FROM ROLE DETAIL
    // =========================================
    @Override
    @Transactional
    public ApiResponse<RoleDetailResponseDto>
    removePermissionFromRoleDetail(
            Long roleDetailId, Long permissionId) {

        RoleDetail roleDetail = roleDetailRepository
                .findById(roleDetailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RoleDetail not found with id: "
                                + roleDetailId));

        List<Long> currentPermissions =
                parsePermissionIds(roleDetail.getPermissionId());

        if (!currentPermissions.contains(permissionId)) {
            throw new BadRequestException(
                    "Permission id " + permissionId +
                            " does not belong to this RoleDetail");
        }

        currentPermissions.remove(permissionId);

        if (currentPermissions.isEmpty()) {
            Long roleId = roleDetail.getRoleId();
            roleDetailRepository.delete(roleDetail);

            return ApiResponse.<RoleDetailResponseDto>builder()
                    .success(true)
                    .message("Permission removed successfully. " +
                            "RoleDetail deleted as no permissions remain.")
                    .data(getRoleWithGroupedPermissions(roleId))
                    .build();
        }

        roleDetail.setPermissionId(
                serializePermissionIds(currentPermissions));
        // ✅ BaseEntity @PreUpdate handle korbe
        roleDetailRepository.save(roleDetail);

        return ApiResponse.<RoleDetailResponseDto>builder()
                .success(true)
                .message("Permission removed successfully.")
                .data(getRoleWithGroupedPermissions(
                        roleDetail.getRoleId()))
                .build();
    }
}