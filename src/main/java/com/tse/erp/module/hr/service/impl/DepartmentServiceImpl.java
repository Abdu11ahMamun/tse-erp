package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.asset.repository.AssetGroupRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.repository.DepartmentGroupRepository;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.hr.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final AssetGroupRepository assetGroupRepository;
    private final DepartmentGroupRepository departmentGroupRepository;

    @Override
    public List<Department> getAllDepartments(Long bgId) {
        if (bgId == null) {
            return departmentRepository.findAllByOrderByIdDesc();
        }
        return departmentRepository.findByBgIdOrderByIdDesc(bgId);
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));
    }

    @Override
    public Department createDepartment(Department department) {
        validateGroup(department.getBgId());
        validateDepartmentName(department.getDepartmentName());
        validateShortName(department.getShortName());
        validateSortOrder(department.getSortOrder());
        StatusUtil.validate(department.getStatus());

        String name = department.getDepartmentName().trim();
        String shortName = normalizeShortName(department.getShortName());

        boolean nameExists = !departmentRepository
                .findByBgIdAndDepartmentNameIgnoreCase(
                        department.getBgId(), name)
                .isEmpty();
        if (nameExists) {
            throw new DuplicateResourceException(
                    "Department already exists: " + name);
        }

        if (shortName != null) {
            boolean shortExists = !departmentRepository
                    .findByBgIdAndShortNameIgnoreCase(
                            department.getBgId(), shortName)
                    .isEmpty();
            if (shortExists) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        department.setId(null);
        department.setDepartmentName(name);
        department.setShortName(shortName);

        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long id, Department department) {
        Department existing = getDepartmentById(id);

        validateGroup(department.getBgId());
        validateDepartmentName(department.getDepartmentName());
        validateShortName(department.getShortName());
        validateSortOrder(department.getSortOrder());
        StatusUtil.validate(department.getStatus());

        String name = department.getDepartmentName().trim();
        String shortName = normalizeShortName(department.getShortName());

        boolean nameDuplicate = departmentRepository
                .findByBgIdAndDepartmentNameIgnoreCase(
                        department.getBgId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (nameDuplicate) {
            throw new DuplicateResourceException(
                    "Department already exists: " + name);
        }

        if (shortName != null) {
            boolean shortDuplicate = departmentRepository
                    .findByBgIdAndShortNameIgnoreCase(
                            department.getBgId(), shortName)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (shortDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setBgId(department.getBgId());
        existing.setDepartmentName(name);
        existing.setShortName(shortName);
        existing.setSortOrder(department.getSortOrder());
        if (department.getStatus() != null) {
            existing.setStatus(department.getStatus());
        }

        return departmentRepository.save(existing);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department existing = getDepartmentById(id);

        if (assetGroupRepository.existsByAssetOwnerId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Asset Groups use this Department as owner");
        }

        if (departmentGroupRepository.existsByDeptId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Department is mapped to Business Unit(s)");
        }

        departmentRepository.delete(existing);
    }

    private void validateGroup(Long bgId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));
    }

    private void validateDepartmentName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException(
                    "Department name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Department name must be at least 2 characters");
        }
        if (name.trim().length() > 50) {
            throw new BadRequestException(
                    "Department name cannot exceed 50 characters");
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
