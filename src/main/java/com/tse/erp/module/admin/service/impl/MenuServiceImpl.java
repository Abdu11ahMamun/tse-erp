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

import java.time.LocalDateTime;
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

        // Validation
        if (menu.getMenuName() == null ||
                menu.getMenuName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Menu name cannot be empty");
        }

        if (menu.getModuleId() == null) {
            throw new BadRequestException(
                    "Module id cannot be empty");
        }

        // Module exist kore kina check
        moduleRepository.findById(menu.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + menu.getModuleId()));

        // Duplicate check — same module e same menu name
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
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());

        return menuRepository.save(menu);
    }

    @Override
    public Menu updateMenu(Long id, Menu menu) {

        Menu existing = getMenuById(id);

        // Validation
        if (menu.getMenuName() == null ||
                menu.getMenuName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Menu name cannot be empty");
        }

        if (menu.getModuleId() == null) {
            throw new BadRequestException(
                    "Module id cannot be empty");
        }

        // Module exist kore kina check
        moduleRepository.findById(menu.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + menu.getModuleId()));

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
        existing.setPermissionId(menu.getPermissionId());
        existing.setSortOrder(menu.getSortOrder());
        existing.setRouteName(menu.getRouteName());
        existing.setIsTopMenu(menu.getIsTopMenu());
        existing.setIsActive(menu.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());

        return menuRepository.save(existing);
    }

    @Override
    public void deleteMenu(Long id) {
        Menu existing = getMenuById(id);
        menuRepository.delete(existing);
    }
}