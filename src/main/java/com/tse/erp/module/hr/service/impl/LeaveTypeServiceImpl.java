package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.hr.entity.LeaveType;
import com.tse.erp.module.hr.repository.LeaveTypeGroupRepository;
import com.tse.erp.module.hr.repository.LeaveTypeRepository;
import com.tse.erp.module.hr.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final LeaveTypeGroupRepository leaveTypeGroupRepository;

    @Override
    public List<LeaveType> getAllLeaveTypes(Long bgId) {
        if (bgId == null) {
            return leaveTypeRepository.findAllByOrderByIdDesc();
        }
        return leaveTypeRepository.findByBgIdOrderByIdDesc(bgId);
    }

    @Override
    public LeaveType getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave Type not found with id: " + id));
    }

    @Override
    public LeaveType createLeaveType(LeaveType leaveType) {
        validateGroup(leaveType.getBgId());
        validateTypeName(leaveType.getTypeName());
        validateShortName(leaveType.getShortName());
        validateSortOrder(leaveType.getSortOrder());
        StatusUtil.validate(leaveType.getStatus());

        String typeName = leaveType.getTypeName().trim();
        String shortName = normalizeShortName(leaveType.getShortName());

        boolean typeNameExists = !leaveTypeRepository
                .findByBgIdAndTypeNameIgnoreCase(
                        leaveType.getBgId(), typeName)
                .isEmpty();
        if (typeNameExists) {
            throw new DuplicateResourceException(
                    "Leave Type already exists: " + typeName);
        }

        if (shortName != null) {
            boolean shortNameExists = !leaveTypeRepository
                    .findByBgIdAndShortNameIgnoreCase(
                            leaveType.getBgId(), shortName)
                    .isEmpty();
            if (shortNameExists) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        leaveType.setId(null);
        leaveType.setTypeName(typeName);
        leaveType.setShortName(shortName);

        return leaveTypeRepository.save(leaveType);
    }

    @Override
    public LeaveType updateLeaveType(Long id, LeaveType leaveType) {
        LeaveType existing = getLeaveTypeById(id);

        validateGroup(leaveType.getBgId());
        validateTypeName(leaveType.getTypeName());
        validateShortName(leaveType.getShortName());
        validateSortOrder(leaveType.getSortOrder());
        StatusUtil.validate(leaveType.getStatus());

        String typeName = leaveType.getTypeName().trim();
        String shortName = normalizeShortName(leaveType.getShortName());

        boolean typeNameDuplicate = leaveTypeRepository
                .findByBgIdAndTypeNameIgnoreCase(
                        leaveType.getBgId(), typeName)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (typeNameDuplicate) {
            throw new DuplicateResourceException(
                    "Leave Type already exists: " + typeName);
        }

        if (shortName != null) {
            boolean shortNameDuplicate = leaveTypeRepository
                    .findByBgIdAndShortNameIgnoreCase(
                            leaveType.getBgId(), shortName)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (shortNameDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setBgId(leaveType.getBgId());
        existing.setTypeName(typeName);
        existing.setShortName(shortName);
        existing.setSortOrder(leaveType.getSortOrder());
        if (leaveType.getStatus() != null) {
            existing.setStatus(leaveType.getStatus());
        }

        return leaveTypeRepository.save(existing);
    }

    @Override
    public void deleteLeaveType(Long id) {
        LeaveType existing = getLeaveTypeById(id);

        // TODO: block delete when Leave Applications use this type, after Leave module exists.
        if (leaveTypeGroupRepository.existsByLeaveTypeId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Leave Type is mapped to Business Unit(s)");
        }

        leaveTypeRepository.delete(existing);
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
