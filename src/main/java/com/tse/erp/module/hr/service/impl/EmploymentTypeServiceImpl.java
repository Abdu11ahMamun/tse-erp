package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.common.ValidationUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.hr.entity.EmploymentType;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.hr.repository.EmploymentTypeRepository;
import com.tse.erp.module.hr.service.EmploymentTypeService;
import com.tse.erp.security.AuthUser;
import com.tse.erp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmploymentTypeServiceImpl implements EmploymentTypeService {

    private static final Set<String> CATEGORIES =
            Set.of("ET", "WS", "PS", "ES");

    private final EmploymentTypeRepository employmentTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<EmploymentType> getAllEmploymentTypes(
            Long bgId, String category, boolean activeOnly) {

        // bgId na dile logged-in user-er BG (onno group-er data leak na hoy)
        Long effectiveBgId = bgId != null
                ? bgId
                : CurrentUser.get().map(AuthUser::bgId).orElse(null);

        String cat = (category == null || category.isBlank())
                ? null : normalizeCategory(category);

        List<EmploymentType> list = effectiveBgId == null
                ? employmentTypeRepository.findAllByOrderBySortOrderAscIdAsc()
                : employmentTypeRepository
                .findByBgIdOrderBySortOrderAscIdAsc(effectiveBgId);

        return list.stream()
                .filter(t -> cat == null || cat.equals(t.getCategory()))
                .filter(t -> !activeOnly
                        || Integer.valueOf(1).equals(t.getStatus()))
                .toList();
    }

    @Override
    public EmploymentType getEmploymentTypeById(Long id) {
        return employmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment Type not found with id: " + id));
    }

    @Override
    public EmploymentType createEmploymentType(EmploymentType t) {

        prepare(t);
        StatusUtil.validate(t.getStatus());
        checkDuplicate(t, null);

        t.setId(null);
        // Status default 1, BaseEntity @PrePersist handle korbe
        return employmentTypeRepository.save(t);
    }

    @Override
    public EmploymentType updateEmploymentType(Long id, EmploymentType t) {

        EmploymentType existing = getEmploymentTypeById(id);

        prepare(t);
        StatusUtil.validate(t.getStatus());

        // Employee-te use hoye thakte pare, tai BG/category badla jabe na
        if (existing.getBgId() != null
                && !existing.getBgId().equals(t.getBgId())) {
            throw new BadRequestException("Business Group cannot be changed");
        }
        if (existing.getCategory() != null
                && !existing.getCategory().equals(t.getCategory())) {
            throw new BadRequestException("Category cannot be changed");
        }
        checkDuplicate(t, id);

        existing.setBgId(t.getBgId());
        existing.setCategory(t.getCategory());
        existing.setTypeName(t.getTypeName());
        existing.setShortName(t.getShortName());
        existing.setSortOrder(t.getSortOrder());
        existing.setIsDateFieldRequired(t.getIsDateFieldRequired());
        existing.setDateFieldLabel(t.getDateFieldLabel());
        if (t.getStatus() != null) {
            existing.setStatus(t.getStatus());
        }
        return employmentTypeRepository.save(existing);
    }

    @Override
    public void deleteEmploymentType(Long id) {
        EmploymentType existing = getEmploymentTypeById(id);

        if (employeeRepository.existsByEmploymentTypeId(id)
                || employeeRepository.existsByWorkScheduleId(id)
                || employeeRepository.existsByProbationStatusId(id)
                || employeeRepository.existsByEmployeeStatusId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Employees use this record");
        }
        employmentTypeRepository.delete(existing);
    }

    // =========================================
    // VALIDATE + CLEAN
    // =========================================
    private void prepare(EmploymentType t) {

        if (t.getBgId() == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }
        businessGroupRepository.findById(t.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + t.getBgId()));

        if (t.getCategory() == null || t.getCategory().isBlank()) {
            throw new BadRequestException("Category cannot be empty");
        }
        t.setCategory(normalizeCategory(t.getCategory()));

        if (t.getTypeName() == null || t.getTypeName().trim().isEmpty()) {
            throw new BadRequestException("Type name cannot be empty");
        }
        String name = t.getTypeName().trim();
        if (name.length() < 2) {
            throw new BadRequestException(
                    "Type name must be at least 2 characters");
        }
        if (name.length() > 50) {
            throw new BadRequestException(
                    "Type name cannot exceed 50 characters");
        }
        t.setTypeName(name);

        // Short name optional
        String shortName = null;
        if (t.getShortName() != null && !t.getShortName().trim().isEmpty()) {
            shortName = t.getShortName().trim();
            if (shortName.length() < 2) {
                throw new BadRequestException(
                        "Short name must be at least 2 characters");
            }
            if (shortName.length() > 100) {
                throw new BadRequestException(
                        "Short name cannot exceed 100 characters");
            }
        }
        t.setShortName(shortName);

        ValidationUtil.validateSortOrder(t.getSortOrder());

        // Date field
        boolean dateRequired = Boolean.TRUE.equals(t.getIsDateFieldRequired());
        t.setIsDateFieldRequired(dateRequired);
        if (dateRequired) {
            if (t.getDateFieldLabel() == null
                    || t.getDateFieldLabel().trim().isEmpty()) {
                throw new BadRequestException(
                        "Date field label is required when date field is required");
            }
            String label = t.getDateFieldLabel().trim();
            if (label.length() < 2 || label.length() > 50) {
                throw new BadRequestException(
                        "Date field label must be between 2 and 50 characters");
            }
            if (label.indexOf('<') >= 0 || label.indexOf('>') >= 0) {
                throw new BadRequestException(
                        "Date field label contains unsupported characters");
            }
            t.setDateFieldLabel(label);
        } else {
            t.setDateFieldLabel(null);
        }
    }

    private String normalizeCategory(String c) {
        String v = c.trim().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(v)) {
            throw new BadRequestException(
                    "Category must be one of ET, WS, PS, ES");
        }
        return v;
    }

    // Unique: BG + category-r moddhe, case-insensitive
    private void checkDuplicate(EmploymentType t, Long selfId) {
        boolean nameDup = employmentTypeRepository
                .findByBgIdAndCategoryAndTypeNameIgnoreCase(
                        t.getBgId(), t.getCategory(), t.getTypeName())
                .stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId));
        if (nameDup) {
            throw new DuplicateResourceException(
                    "Type name already exists in this category: "
                            + t.getTypeName());
        }
        if (t.getShortName() != null) {
            boolean shortDup = employmentTypeRepository
                    .findByBgIdAndCategoryAndShortNameIgnoreCase(
                            t.getBgId(), t.getCategory(), t.getShortName())
                    .stream()
                    .anyMatch(x -> selfId == null
                            || !x.getId().equals(selfId));
            if (shortDup) {
                throw new DuplicateResourceException(
                        "Short name already exists in this category: "
                                + t.getShortName());
            }
        }
    }
}