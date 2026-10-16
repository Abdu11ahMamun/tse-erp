package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.EmploymentType;
import com.tse.erp.module.hr.entity.LeaveAllocation;
import com.tse.erp.module.hr.entity.LeaveType;
import com.tse.erp.module.hr.repository.EmploymentTypeRepository;
import com.tse.erp.module.hr.repository.LeaveAllocationRepository;
import com.tse.erp.module.hr.repository.LeaveTypeGroupRepository;
import com.tse.erp.module.hr.repository.LeaveTypeRepository;
import com.tse.erp.module.hr.service.LeaveAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveAllocationServiceImpl
        implements LeaveAllocationService {

    private final LeaveAllocationRepository leaveAllocationRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeGroupRepository leaveTypeGroupRepository;

    @Override
    public List<LeaveAllocation> getAllLeaveAllocations(
            Long buId, Integer leaveYear) {
        if (buId != null && leaveYear != null) {
            return leaveAllocationRepository
                    .findByBuIdAndLeaveYearOrderByIdDesc(
                            buId, leaveYear);
        }
        if (buId != null) {
            return leaveAllocationRepository
                    .findByBuIdOrderByIdDesc(buId);
        }
        if (leaveYear != null) {
            return leaveAllocationRepository
                    .findByLeaveYearOrderByIdDesc(leaveYear);
        }
        return leaveAllocationRepository.findAllByOrderByIdDesc();
    }

    @Override
    public LeaveAllocation getLeaveAllocationById(Long id) {
        return leaveAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave Allocation not found with id: " + id));
    }

    @Override
    public LeaveAllocation createLeaveAllocation(
            LeaveAllocation leaveAllocation) {
        prepare(leaveAllocation);
        StatusUtil.validate(leaveAllocation.getStatus());
        int effectiveStatus = leaveAllocation.getStatus() == null
                ? 1 : leaveAllocation.getStatus();
        if (effectiveStatus == 1) {
            checkDuplicate(leaveAllocation, null);
        }
        leaveAllocation.setId(null);
        return leaveAllocationRepository.save(leaveAllocation);
    }

    @Override
    public LeaveAllocation updateLeaveAllocation(
            Long id, LeaveAllocation leaveAllocation) {
        LeaveAllocation existing = getLeaveAllocationById(id);
        prepare(leaveAllocation);
        StatusUtil.validate(leaveAllocation.getStatus());
        int effectiveStatus = leaveAllocation.getStatus() != null
                ? leaveAllocation.getStatus()
                : (existing.getStatus() == null
                        ? 1 : existing.getStatus());
        if (effectiveStatus == 1) {
            checkDuplicate(leaveAllocation, id);
        }

        existing.setBuId(leaveAllocation.getBuId());
        existing.setEmpTypeId(leaveAllocation.getEmpTypeId());
        existing.setLeaveCategoryId(leaveAllocation.getLeaveCategoryId());
        existing.setTotalLeaveDays(leaveAllocation.getTotalLeaveDays());
        existing.setLeaveYear(leaveAllocation.getLeaveYear());
        existing.setSandwichLeaveApplicable(
                leaveAllocation.getSandwichLeaveApplicable());
        existing.setWithPay(leaveAllocation.getWithPay());
        if (leaveAllocation.getStatus() != null) {
            existing.setStatus(leaveAllocation.getStatus());
        }
        return leaveAllocationRepository.save(existing);
    }

    @Override
    public void deleteLeaveAllocation(Long id) {
        LeaveAllocation existing = getLeaveAllocationById(id);
        // TODO: block delete when Leave Balances/Applications use this allocation.
        leaveAllocationRepository.delete(existing);
    }

    private void prepare(LeaveAllocation allocation) {
        if (allocation.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository
                .findById(allocation.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: "
                                + allocation.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }

        if (allocation.getEmpTypeId() == null) {
            throw new BadRequestException(
                    "Employee type cannot be empty");
        }
        EmploymentType employmentType = employmentTypeRepository
                .findById(allocation.getEmpTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment Type not found with id: "
                                + allocation.getEmpTypeId()));
        if (!Integer.valueOf(1).equals(employmentType.getStatus())) {
            throw new BadRequestException(
                    "Selected Employee Type is not active");
        }

        if (allocation.getLeaveCategoryId() == null) {
            throw new BadRequestException(
                    "Leave category cannot be empty");
        }
        LeaveType leaveType = leaveTypeRepository
                .findById(allocation.getLeaveCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave Type not found with id: "
                                + allocation.getLeaveCategoryId()));
        if (!Integer.valueOf(1).equals(leaveType.getStatus())) {
            throw new BadRequestException(
                    "Selected Leave Category is not active");
        }
        if (!bu.getBgId().equals(leaveType.getBgId())) {
            throw new BadRequestException(
                    "Leave Category does not belong to the Business Unit's Group");
        }
        if (leaveTypeGroupRepository.findByLeaveTypeIdAndBuId(
                allocation.getLeaveCategoryId(), allocation.getBuId())
                .isEmpty()) {
            throw new BadRequestException(
                    "Leave Category is not mapped to selected Business Unit");
        }

        if (allocation.getTotalLeaveDays() == null) {
            throw new BadRequestException(
                    "Total leave days cannot be empty");
        }
        BigDecimal totalLeaveDays = allocation.getTotalLeaveDays();
        if (totalLeaveDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Total leave days must be greater than 0");
        }
        if (totalLeaveDays.compareTo(new BigDecimal("366")) > 0) {
            throw new BadRequestException(
                    "Total leave days cannot exceed 366");
        }
        if (totalLeaveDays.multiply(new BigDecimal("2"))
                .stripTrailingZeros().scale() > 0) {
            throw new BadRequestException(
                    "Total leave days must be in steps of 0.5");
        }
        allocation.setTotalLeaveDays(
                totalLeaveDays.setScale(2, RoundingMode.HALF_UP));

        if (allocation.getLeaveYear() == null) {
            throw new BadRequestException("Year cannot be empty");
        }
        if (allocation.getLeaveYear() < 2000
                || allocation.getLeaveYear() > 2100) {
            throw new BadRequestException(
                    "Year must be between 2000 and 2100");
        }

        if (allocation.getSandwichLeaveApplicable() == null) {
            allocation.setSandwichLeaveApplicable(0);
        }
        if (allocation.getSandwichLeaveApplicable() != 0
                && allocation.getSandwichLeaveApplicable() != 1) {
            throw new BadRequestException(
                    "Sandwich leave must be 0 or 1");
        }

        if (allocation.getWithPay() == null) {
            allocation.setWithPay(1);
        }
        if (allocation.getWithPay() != 0
                && allocation.getWithPay() != 1) {
            throw new BadRequestException("With pay must be 0 or 1");
        }
    }

    private void checkDuplicate(
            LeaveAllocation allocation, Long selfId) {
        boolean duplicate = leaveAllocationRepository
                .findByBuIdAndEmpTypeIdAndLeaveCategoryIdAndLeaveYear(
                        allocation.getBuId(), allocation.getEmpTypeId(),
                        allocation.getLeaveCategoryId(),
                        allocation.getLeaveYear())
                .stream()
                .anyMatch(x -> Integer.valueOf(1).equals(x.getStatus())
                        && (selfId == null
                                || !x.getId().equals(selfId)));
        if (duplicate) {
            throw new DuplicateResourceException(
                    "An active allocation already exists for this Business Unit, "
                            + "Employee Type, Leave Category and Year");
        }
    }
}
