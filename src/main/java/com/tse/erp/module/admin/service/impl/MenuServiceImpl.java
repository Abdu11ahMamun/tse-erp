package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.Menu;
import com.tse.erp.module.admin.repository.MenuRepository;
import com.tse.erp.module.admin.repository.ModuleRepository;
import com.tse.erp.module.admin.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;

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

        // Required checks
        if (menu.getMenuName() == null ||
                menu.getMenuName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Menu name cannot be empty");
        }

        // Min/Max length
        if (menu.getMenuName().trim().length() < 2) {
            throw new BadRequestException(
                    "Menu name must be at least 2 characters");
        }

        if (menu.getMenuName().trim().length() > 100) {
            throw new BadRequestException(
                    "Menu name cannot exceed 100 characters");
        }

        if (menu.getModuleId() == null) {
            throw new BadRequestException(
                    "Module id cannot be empty");
        }

        // Route name validation (required, min 2, max 100)
        if (menu.getRouteName() != null &&
                !menu.getRouteName().trim().isEmpty()) {
            if (menu.getRouteName().trim().length() < 2) {
                throw new BadRequestException(
                        "Route name must be at least 2 characters");
            }
            if (menu.getRouteName().trim().length() > 100) {
                throw new BadRequestException(
                        "Route name cannot exceed 100 characters");
            }
        }

        // Sort order — must be integer >= 1
        if (menu.getSortOrder() != null &&
                !menu.getSortOrder().trim().isEmpty()) {
            try {
                int sortVal = Integer.parseInt(
                        menu.getSortOrder().trim());
                if (sortVal < 1) {
                    throw new BadRequestException(
                            "Sort order must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException(
                        "Sort order must be a whole number");
            }
        }

        // Module exist check
        moduleRepository.findById(menu.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + menu.getModuleId()));

        // Duplicate check — same module + same name
        boolean exists = !menuRepository
                .findByMenuNameIgnoreCaseAndModuleId(
                        menu.getMenuName().trim(),
                        menu.getModuleId())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Menu '" + menu.getMenuName()
                            + "' already exists in this module");
        }

        menu.setMenuName(menu.getMenuName().trim());
        menu.setIsActive(1);

        // ✅ BaseEntity @PrePersist handle korbe
        return menuRepository.save(menu);
    }

    @Override
    public Menu updateMenu(Long id, Menu menu) {

        Menu existing = getMenuById(id);

        // Required checks
        if (menu.getMenuName() == null ||
                menu.getMenuName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Menu name cannot be empty");
        }

        // Min/Max length
        if (menu.getMenuName().trim().length() < 2) {
            throw new BadRequestException(
                    "Menu name must be at least 2 characters");
        }

        if (menu.getMenuName().trim().length() > 100) {
            throw new BadRequestException(
                    "Menu name cannot exceed 100 characters");
        }

        if (menu.getModuleId() == null) {
            throw new BadRequestException(
                    "Module id cannot be empty");
        }

        // Route name validation
        if (menu.getRouteName() != null &&
                !menu.getRouteName().trim().isEmpty()) {
            if (menu.getRouteName().trim().length() < 2) {
                throw new BadRequestException(
                        "Route name must be at least 2 characters");
            }
            if (menu.getRouteName().trim().length() > 100) {
                throw new BadRequestException(
                        "Route name cannot exceed 100 characters");
            }
        }

        // Sort order validation
        if (menu.getSortOrder() != null &&
                !menu.getSortOrder().trim().isEmpty()) {
            try {
                int sortVal = Integer.parseInt(
                        menu.getSortOrder().trim());
                if (sortVal < 1) {
                    throw new BadRequestException(
                            "Sort order must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException(
                        "Sort order must be a whole number");
            }
        }

        // Module exist check
        moduleRepository.findById(menu.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: "
                                + menu.getModuleId()));

        // Duplicate check — nijer id bade
        List<Menu> found = menuRepository
                .findByMenuNameIgnoreCaseAndModuleId(
                        menu.getMenuName().trim(),
                        menu.getModuleId());

        boolean duplicateExists = found.stream()
                .anyMatch(m -> !m.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Menu '" + menu.getMenuName()
                            + "' already exists in this module");
        }

        existing.setMenuName(menu.getMenuName().trim());
        existing.setModuleId(menu.getModuleId());
        existing.setIsParent(menu.getIsParent());
        existing.setParentMenuId(menu.getParentMenuId());
        existing.setIsTopMenu(menu.getIsTopMenu());
        existing.setPermissionId(menu.getPermissionId());
        existing.setRouteName(menu.getRouteName());
        existing.setSortOrder(menu.getSortOrder());
        existing.setIsActive(menu.getIsActive());

        // ✅ BaseEntity @PreUpdate handle korbe
        return menuRepository.save(existing);
    }

    @Override
    public void deleteMenu(Long id) {
        Menu existing = getMenuById(id);
        menuRepository.delete(existing);
    }
}