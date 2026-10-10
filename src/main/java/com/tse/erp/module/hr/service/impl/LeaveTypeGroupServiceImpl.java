package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.LeaveType;
import com.tse.erp.module.hr.entity.LeaveTypeGroup;
import com.tse.erp.module.hr.repository.LeaveTypeGroupRepository;
import com.tse.erp.module.hr.repository.LeaveTypeRepository;
import com.tse.erp.module.hr.service.LeaveTypeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveTypeGroupServiceImpl
        implements LeaveTypeGroupService {

    private final LeaveTypeGroupRepository leaveTypeGroupRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    public List<LeaveTypeGroup> getAllLeaveTypeGroups(Long leaveTypeId) {
        if (leaveTypeId == null) {
            return leaveTypeGroupRepository.findAllByOrderByIdDesc();
        }
        return leaveTypeGroupRepository
                .findByLeaveTypeIdOrderByIdDesc(leaveTypeId);
    }

    @Override
    public LeaveTypeGroup getLeaveTypeGroupById(Long id) {
        return leaveTypeGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave Type Group not found with id: " + id));
    }

    @Override
    public LeaveTypeGroup createLeaveTypeGroup(
            LeaveTypeGroup leaveTypeGroup) {
        validateParents(leaveTypeGroup.getBgId(),
                leaveTypeGroup.getBuId(),
                leaveTypeGroup.getLeaveTypeId());
        StatusUtil.validate(leaveTypeGroup.getStatus());

        boolean exists = !leaveTypeGroupRepository
                .findByLeaveTypeIdAndBuId(
                        leaveTypeGroup.getLeaveTypeId(),
                        leaveTypeGroup.getBuId())
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "This Leave Type is already mapped to the selected Business Unit");
        }

        leaveTypeGroup.setId(null);
        return leaveTypeGroupRepository.save(leaveTypeGroup);
    }

    @Override
    public LeaveTypeGroup updateLeaveTypeGroup(
            Long id, LeaveTypeGroup leaveTypeGroup) {
        LeaveTypeGroup existing = getLeaveTypeGroupById(id);

        validateParents(leaveTypeGroup.getBgId(),
                leaveTypeGroup.getBuId(),
                leaveTypeGroup.getLeaveTypeId());
        StatusUtil.validate(leaveTypeGroup.getStatus());

        boolean duplicateExists = leaveTypeGroupRepository
                .findByLeaveTypeIdAndBuId(
                        leaveTypeGroup.getLeaveTypeId(),
                        leaveTypeGroup.getBuId())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "This Leave Type is already mapped to the selected Business Unit");
        }

        existing.setBgId(leaveTypeGroup.getBgId());
        existing.setBuId(leaveTypeGroup.getBuId());
        existing.setLeaveTypeId(leaveTypeGroup.getLeaveTypeId());
        if (leaveTypeGroup.getStatus() != null) {
            existing.setStatus(leaveTypeGroup.getStatus());
        }

        return leaveTypeGroupRepository.save(existing);
    }

    @Override
    public void deleteLeaveTypeGroup(Long id) {
        LeaveTypeGroup existing = getLeaveTypeGroupById(id);
        leaveTypeGroupRepository.delete(existing);
    }

    private void validateParents(Long bgId, Long buId, Long leaveTypeId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        if (leaveTypeId == null) {
            throw new BadRequestException(
                    "Leave Type cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave Type not found with id: " + leaveTypeId));

        if (!Integer.valueOf(1).equals(leaveType.getStatus())) {
            throw new BadRequestException(
                    "Selected Leave Type is not active");
        }
        if (!bgId.equals(leaveType.getBgId())) {
            throw new BadRequestException(
                    "Leave Type does not belong to selected Business Group");
        }
    }
}
