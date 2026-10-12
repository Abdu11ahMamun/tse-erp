package com.tse.erp.module.admin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.Menu;
import com.tse.erp.module.admin.entity.Permission;
import com.tse.erp.module.admin.repository.MenuRepository;
import com.tse.erp.module.admin.repository.ModuleRepository;
import com.tse.erp.module.admin.repository.PermissionRepository;
import com.tse.erp.module.admin.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Menu> getMenusByModuleId(Long moduleId) {
        moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + moduleId));

        return menuRepository.findByModuleIdOrderByIdDesc(moduleId);
    }

    @Override
    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found with id: " + id));
    }

    @Override
    public Menu createMenu(Menu menu) {

        StatusUtil.validate(menu.getStatus());

        Menu target = new Menu();
        validateAndApply(target, menu, null);

        if (menu.getStatus() != null) {
            target.setStatus(menu.getStatus());
        }

        // Status default 1, BaseEntity @PrePersist handle korbe
        return menuRepository.save(target);
    }

    @Override
    public Menu updateMenu(Long id, Menu menu) {

        Menu existing = getMenuById(id);

        StatusUtil.validate(menu.getStatus());

        boolean wasParent = Integer.valueOf(1).equals(existing.getIsParent());

        validateAndApply(existing, menu, id);

        // Parent theke non-parent korle child thakle block
        if (wasParent && !Integer.valueOf(1).equals(existing.getIsParent())
                && menuRepository.existsByParentMenuId(id)) {
            throw new BadRequestException(
                    "Cannot change: child menus exist under this menu");
        }

        if (menu.getStatus() != null) {
            existing.setStatus(menu.getStatus());
        }

        // BaseEntity @PreUpdate handle korbe
        return menuRepository.save(existing);
    }

    @Override
    public void deleteMenu(Long id) {
        Menu existing = getMenuById(id);

        if (menuRepository.existsByParentMenuId(id)) {
            throw new BadRequestException(
                    "Cannot delete: child menus exist under this menu");
        }
        // TODO: Role Details e use hole delete block (RoleDetailRepository.existsByMenuId)

        menuRepository.delete(existing);
    }

    // =========================================
    // VALIDATE + APPLY
    // =========================================
    private void validateAndApply(Menu target, Menu src, Long selfId) {

        // Menu name
        String name = validateMenuName(src.getMenuName());

        // Module
        if (src.getModuleId() == null) {
            throw new BadRequestException("Module id cannot be empty");
        }
        moduleRepository.findById(src.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + src.getModuleId()));

        // Flags
        int isTop = flag(src.getIsTopMenu(), "Is top menu");
        int isParent = flag(src.getIsParent(), "Is parent menu");

        // Duplicate (same module + same name)
        boolean duplicate = menuRepository
                .findByMenuNameIgnoreCaseAndModuleId(name, src.getModuleId())
                .stream()
                .anyMatch(m -> selfId == null || !m.getId().equals(selfId));
        if (duplicate) {
            throw new DuplicateResourceException(
                    "Menu '" + name + "' already exists in this module");
        }

        // Parent menu (top menu hole parent thakbe na)
        Long parentId = null;
        if (isTop == 0 && src.getParentMenuId() != null) {
            parentId = src.getParentMenuId();
            if (selfId != null && selfId.equals(parentId)) {
                throw new BadRequestException(
                        "Menu cannot be its own parent");
            }
            Menu parent = menuRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent menu not found with id: "
                                    + src.getParentMenuId()));
            if (!Integer.valueOf(1).equals(parent.getIsParent())) {
                throw new BadRequestException(
                        "Selected parent menu is not a parent menu");
            }
            if (!src.getModuleId().equals(parent.getModuleId())) {
                throw new BadRequestException(
                        "Parent menu does not belong to selected module");
            }
        }

        // Route name
        String route;
        if (isTop == 1) {
            route = "#";
        } else {
            route = validateRouteName(src.getRouteName());
        }

        // Permission
        String permissionJson = validatePermissions(
                src.getPermissionId(), src.getModuleId(), isTop == 1);

        // Sort order
        String sortOrder = validateSortOrder(src.getSortOrder());

        target.setMenuName(name);
        target.setModuleId(src.getModuleId());
        target.setIsTopMenu(isTop);
        target.setIsParent(isParent);
        target.setParentMenuId(parentId);
        target.setRouteName(route);
        target.setPermissionId(permissionJson);
        target.setSortOrder(sortOrder);
    }

    // =========================================
    // VALIDATION HELPERS
    // =========================================
    private String validateMenuName(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException("Menu name cannot be empty");
        }
        String s = v.trim();
        if (s.length() < 2) {
            throw new BadRequestException(
                    "Menu name must be at least 2 characters");
        }
        if (s.length() > 100) {
            throw new BadRequestException(
                    "Menu name cannot exceed 100 characters");
        }
        return s;
    }

    private String validateRouteName(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException("Route name cannot be empty");
        }
        String s = v.trim();
        if (s.length() < 2) {
            throw new BadRequestException(
                    "Route name must be at least 2 characters");
        }
        if (s.length() > 100) {
            throw new BadRequestException(
                    "Route name cannot exceed 100 characters");
        }
        return s;
    }

    // null hole 0, 0/1 chhara 400
    private int flag(Integer v, String label) {
        if (v == null) {
            return 0;
        }
        if (v != 0 && v != 1) {
            throw new BadRequestException(label + " must be 0 or 1");
        }
        return v;
    }

    // Sort order optional, decimal > 0 (fraction allowed)
    private String validateSortOrder(String v) {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        String s = v.trim();
        try {
            if (new BigDecimal(s).compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(
                        "Sort order must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Sort order must be a number");
        }
        return s;
    }

    // Permission JSON array: top menu hole optional, na hole required
    private String validatePermissions(
            String json, Long moduleId, boolean topMenu) {

        List<Long> ids = parsePermissionIds(json);

        if (ids.isEmpty()) {
            if (topMenu) {
                return null;
            }
            throw new BadRequestException(
                    "Permission is required for this menu");
        }

        List<Long> distinct = new ArrayList<>(new LinkedHashSet<>(ids));

        for (Long pid : distinct) {
            Permission p = permissionRepository.findById(pid)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Permission not found with id: " + pid));
            if (!Integer.valueOf(1).equals(p.getStatus())) {
                throw new BadRequestException(
                        "Selected Permission is not active: " + pid);
            }
            if (!moduleId.equals(p.getModuleId())) {
                throw new BadRequestException(
                        "Permission " + pid
                                + " does not belong to selected module");
            }
        }

        try {
            return objectMapper.writeValueAsString(distinct);
        } catch (Exception e) {
            throw new BadRequestException("Invalid permission list");
        }
    }

    private List<Long> parsePermissionIds(String json) {
        if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            throw new BadRequestException(
                    "Permission must be a JSON array of ids, e.g. [1,2]");
        }
    }
}