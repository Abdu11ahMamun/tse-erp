package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.hr.entity.Designation;
import com.tse.erp.module.hr.repository.DeptDesigMapRepository;
import com.tse.erp.module.hr.repository.DesignationGroupRepository;
import com.tse.erp.module.hr.repository.DesignationRepository;
import com.tse.erp.module.hr.service.DesignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final DesignationGroupRepository designationGroupRepository;
    private final DeptDesigMapRepository deptDesigMapRepository;

    @Override
    public List<Designation> getAllDesignations(Long bgId) {
        if (bgId == null) {
            return designationRepository.findAllByOrderByIdDesc();
        }
        return designationRepository.findByBgIdOrderByIdDesc(bgId);
    }

    @Override
    public Designation getDesignationById(Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with id: " + id));
    }

    @Override
    public Designation createDesignation(Designation designation) {
        validateGroup(designation.getBgId());
        validateDesignationName(designation.getDesignationName());
        validateShortName(designation.getShortName());
        validateSortOrder(designation.getSortOrder());
        StatusUtil.validate(designation.getStatus());

        String name = designation.getDesignationName().trim();
        String shortName = normalizeShortName(designation.getShortName());

        boolean nameExists = !designationRepository
                .findByBgIdAndDesignationNameIgnoreCase(
                        designation.getBgId(), name)
                .isEmpty();
        if (nameExists) {
            throw new DuplicateResourceException(
                    "Designation already exists: " + name);
        }

        if (shortName != null) {
            boolean shortExists = !designationRepository
                    .findByBgIdAndShortNameIgnoreCase(
                            designation.getBgId(), shortName)
                    .isEmpty();
            if (shortExists) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        designation.setId(null);
        designation.setDesignationName(name);
        designation.setShortName(shortName);

        return designationRepository.save(designation);
    }

    @Override
    public Designation updateDesignation(Long id, Designation designation) {
        Designation existing = getDesignationById(id);

        validateGroup(designation.getBgId());
        validateDesignationName(designation.getDesignationName());
        validateShortName(designation.getShortName());
        validateSortOrder(designation.getSortOrder());
        StatusUtil.validate(designation.getStatus());

        String name = designation.getDesignationName().trim();
        String shortName = normalizeShortName(designation.getShortName());

        boolean nameDuplicate = designationRepository
                .findByBgIdAndDesignationNameIgnoreCase(
                        designation.getBgId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (nameDuplicate) {
            throw new DuplicateResourceException(
                    "Designation already exists: " + name);
        }

        if (shortName != null) {
            boolean shortDuplicate = designationRepository
                    .findByBgIdAndShortNameIgnoreCase(
                            designation.getBgId(), shortName)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (shortDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setBgId(designation.getBgId());
        existing.setDesignationName(name);
        existing.setShortName(shortName);
        existing.setSortOrder(designation.getSortOrder());
        if (designation.getStatus() != null) {
            existing.setStatus(designation.getStatus());
        }

        return designationRepository.save(existing);
    }

    @Override
    public void deleteDesignation(Long id) {
        Designation existing = getDesignationById(id);

        // TODO: block delete when Users use this Designation, after User module has desigId.
        if (designationGroupRepository.existsByDesigId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Designation is mapped to Business Unit(s)");
        }

        if (deptDesigMapRepository.existsByDesigId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Designation is used in Designation Mapping");
        }

        designationRepository.delete(existing);
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

    private void validateDesignationName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException(
                    "Designation name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Designation name must be at least 2 characters");
        }
        if (name.trim().length() > 50) {
            throw new BadRequestException(
                    "Designation name cannot exceed 50 characters");
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
