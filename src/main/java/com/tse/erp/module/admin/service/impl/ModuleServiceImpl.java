package com.tse.erp.module.admin.service.impl;

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
        return moduleRepository.findAll();
    }

    @Override
    public Module getModuleById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id: " + id));
    }

    @Override
    public Module createModule(Module module) {

        // Validation
        if (module.getModuleName() == null ||
                module.getModuleName().trim().isEmpty()) {
            throw new BadRequestException("Module name cannot be empty");
        }

        // ✅ Duplicate check — fixed
        boolean exists = !moduleRepository
                .findByModuleNameIgnoreCase(module.getModuleName().trim())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Module already exists with name: " + module.getModuleName());
        }

        module.setModuleName(module.getModuleName().trim());
        module.setIsActive(1);

        return moduleRepository.save(module);
    }

    @Override
    public Module updateModule(Long id, Module module) {

        Module existing = getModuleById(id);

        // Validation
        if (module.getModuleName() == null ||
                module.getModuleName().trim().isEmpty()) {
            throw new BadRequestException("Module name cannot be empty");
        }

        // ✅ Duplicate check — fixed
        List<Module> found = moduleRepository
                .findByModuleNameIgnoreCase(module.getModuleName().trim());

        boolean duplicateExists = found.stream()
                .anyMatch(m -> !m.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Module already exists with name: " + module.getModuleName());
        }

        existing.setModuleName(module.getModuleName().trim());
        existing.setIsActive(module.getIsActive());

        return moduleRepository.save(existing);
    }

    @Override
    public void deleteModule(Long id) {
        Module existing = getModuleById(id);
        moduleRepository.delete(existing);
    }
}