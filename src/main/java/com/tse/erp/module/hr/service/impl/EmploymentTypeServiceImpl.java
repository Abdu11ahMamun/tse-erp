package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.entity.EmploymentType;
import com.tse.erp.module.hr.repository.EmploymentTypeRepository;
import com.tse.erp.module.hr.service.EmploymentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmploymentTypeServiceImpl
        implements EmploymentTypeService {

    private final EmploymentTypeRepository employmentTypeRepository;

    @Override
    public List<EmploymentType> getAllEmploymentTypes() {
        return employmentTypeRepository
                .findAllByOrderBySortOrderAscIdAsc();
    }

    @Override
    public EmploymentType getEmploymentTypeById(Long id) {
        return employmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment Type not found with id: " + id));
    }

    @Override
    public EmploymentType createEmploymentType(
            EmploymentType employmentType) {
        validateTypeName(employmentType.getTypeName());
        validateShortName(employmentType.getShortName());
        validateSortOrder(employmentType.getSortOrder());
        StatusUtil.validate(employmentType.getStatus());

        String typeName = employmentType.getTypeName().trim();
        String shortName = normalizeShortName(
                employmentType.getShortName());

        boolean typeNameExists = !employmentTypeRepository
                .findByTypeNameIgnoreCase(typeName)
                .isEmpty();
        if (typeNameExists) {
            throw new DuplicateResourceException(
                    "Employment Type already exists: " + typeName);
        }

        if (shortName != null) {
            boolean shortNameExists = !employmentTypeRepository
                    .findByShortNameIgnoreCase(shortName)
                    .isEmpty();
            if (shortNameExists) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        employmentType.setId(null);
        employmentType.setTypeName(typeName);
        employmentType.setShortName(shortName);

        return employmentTypeRepository.save(employmentType);
    }

    @Override
    public EmploymentType updateEmploymentType(
            Long id, EmploymentType employmentType) {
        EmploymentType existing = getEmploymentTypeById(id);

        validateTypeName(employmentType.getTypeName());
        validateShortName(employmentType.getShortName());
        validateSortOrder(employmentType.getSortOrder());
        StatusUtil.validate(employmentType.getStatus());

        String typeName = employmentType.getTypeName().trim();
        String shortName = normalizeShortName(
                employmentType.getShortName());

        boolean typeNameDuplicate = employmentTypeRepository
                .findByTypeNameIgnoreCase(typeName)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (typeNameDuplicate) {
            throw new DuplicateResourceException(
                    "Employment Type already exists: " + typeName);
        }

        if (shortName != null) {
            boolean shortNameDuplicate = employmentTypeRepository
                    .findByShortNameIgnoreCase(shortName)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (shortNameDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setTypeName(typeName);
        existing.setShortName(shortName);
        existing.setSortOrder(employmentType.getSortOrder());
        if (employmentType.getStatus() != null) {
            existing.setStatus(employmentType.getStatus());
        }

        return employmentTypeRepository.save(existing);
    }

    @Override
    public void deleteEmploymentType(Long id) {
        EmploymentType existing = getEmploymentTypeById(id);

        // TODO: block delete when Employees use this type, after Employee module exists.
        employmentTypeRepository.delete(existing);
    }

    private void validateTypeName(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new BadRequestException(
                    "Type name cannot be empty");
        }
        if (typeName.trim().length() < 2) {
            throw new BadRequestException(
                    "Type name must be at least 2 characters");
        }
        if (typeName.trim().length() > 50) {
            throw new BadRequestException(
                    "Type name cannot exceed 50 characters");
        }
    }

    private void validateShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return;
        }
        if (shortName.trim().length() < 2) {
            throw new BadRequestException(
                    "Short name must be at least 2 characters");
        }
        if (shortName.trim().length() > 5) {
            throw new BadRequestException(
                    "Short name cannot exceed 5 characters");
        }
    }

    private String normalizeShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return null;
        }
        return shortName.trim();
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder != null && sortOrder < 1) {
            throw new BadRequestException(
                    "Sort order must be greater than 0");
        }
    }
}
