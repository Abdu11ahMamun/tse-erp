package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.Module;
import com.tse.erp.module.admin.repository.ModuleRepository;
import com.tse.erp.module.admin.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;

    @Override
    public List<Module> getAllModules() {
        return moduleRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Module getModuleById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + id));
    }

    @Override
    public Module createModule(Module module) {

        validateModuleName(module.getModuleName());
        StatusUtil.validate(module.getStatus());

        String name = module.getModuleName().trim();

        // Duplicate check
        boolean exists = !moduleRepository
                .findByModuleNameIgnoreCase(name)
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Module already exists with name: " + name);
        }

        module.setId(null);
        module.setModuleName(name);

        // Status default 1, BaseEntity @PrePersist handle korbe
        return moduleRepository.save(module);
    }

    @Override
    public Module updateModule(Long id, Module module) {

        Module existing = getModuleById(id);

        validateModuleName(module.getModuleName());
        StatusUtil.validate(module.getStatus());

        String name = module.getModuleName().trim();

        // Duplicate check, nijer id bade
        boolean duplicateExists = moduleRepository
                .findByModuleNameIgnoreCase(name)
                .stream()
                .anyMatch(m -> !m.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Module already exists with name: " + name);
        }

        existing.setModuleName(name);
        if (module.getStatus() != null) {
            existing.setStatus(module.getStatus());
        }

        // BaseEntity @PreUpdate handle korbe
        return moduleRepository.save(existing);
    }

    @Override
    public void deleteModule(Long id) {
        Module existing = getModuleById(id);
        // TODO: Menu/Permission thakle delete block
        moduleRepository.delete(existing);
    }

    private void validateModuleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Module name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Module name must be at least 2 characters");
        }
        if (name.trim().length() > 100) {
            throw new BadRequestException(
                    "Module name cannot exceed 100 characters");
        }
    }
}