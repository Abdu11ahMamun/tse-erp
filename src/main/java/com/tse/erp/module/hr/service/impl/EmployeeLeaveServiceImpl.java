package com.tse.erp.module.hr.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.dto.LeaveBalanceDto;
import com.tse.erp.module.hr.dto.LeaveCalcDto;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.entity.EmployeeLeave;
import com.tse.erp.module.hr.entity.HrLookupValue;
import com.tse.erp.module.hr.entity.LeaveAllocation;
import com.tse.erp.module.hr.entity.LeaveType;
import com.tse.erp.module.hr.entity.OfficeCalendar;
import com.tse.erp.module.hr.entity.OfficeCalendarDtl;
import com.tse.erp.module.hr.entity.Shift;
import com.tse.erp.module.hr.repository.EmployeeLeaveRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.hr.repository.HrLookupValueRepository;
import com.tse.erp.module.hr.repository.LeaveAllocationRepository;
import com.tse.erp.module.hr.repository.LeaveTypeRepository;
import com.tse.erp.module.hr.repository.OfficeCalendarDtlRepository;
import com.tse.erp.module.hr.repository.OfficeCalendarRepository;
import com.tse.erp.module.hr.repository.ShiftRepository;
import com.tse.erp.module.hr.service.EmployeeLeaveService;
import com.tse.erp.security.AuthUser;
import com.tse.erp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tse.erp.module.hr.repository.EmpAttConfigRepository;
import com.tse.erp.module.hr.repository.EmpAttShiftRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeLeaveServiceImpl
        implements EmployeeLeaveService {

    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveAllocationRepository leaveAllocationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final ShiftRepository shiftRepository;
    private final OfficeCalendarRepository officeCalendarRepository;
    private final OfficeCalendarDtlRepository officeCalendarDtlRepository;
    private final HrLookupValueRepository hrLookupValueRepository;
    private final EmpAttConfigRepository empAttConfigRepository;
    private final EmpAttShiftRepository empAttShiftRepository;

    @Override
    public List<EmployeeLeave> getAllEmployeeLeaves(
            String view, Long buId, Long employeeId) {
        if (view == null || "my".equalsIgnoreCase(view)) {
            Long employee = currentEmpId();
            if (employee == null) {
                throw new BadRequestException(
                        "No Employee is assigned to your user, "
                                + "cannot show your applications");
            }
            return employeeLeaveRepository
                    .findByEmployeeIdOrderByIdDesc(employee);
        }
        if ("approval".equalsIgnoreCase(view)) {
            return employeeLeaveRepository.findByStatusOrderByIdDesc(1)
                    .stream()
                    .filter(row -> buId == null || buId.equals(row.getBuId()))
                    .filter(row -> employeeId == null
                            || employeeId.equals(row.getEmployeeId()))
                    .toList();
        }
        throw new BadRequestException(
                "view must be 'my' or 'approval'");
    }

    @Override
    public EmployeeLeave getEmployeeLeaveById(Long id) {
        EmployeeLeave leave = employeeLeaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee Leave not found with id: " + id));
        if (Integer.valueOf(0).equals(leave.getStatus())
                && !canSeeDraft(leave)) {
            throw new ResourceNotFoundException(
                    "Employee Leave not found with id: " + id);
        }
        return leave;
    }

    @Override
    @Transactional
    public EmployeeLeave createEmployeeLeave(
            EmployeeLeave row, boolean submit) {
        EmployeeLeave target = new EmployeeLeave();
        validateAndApply(target, row, null, submit);
        target.setStatus(submit ? 1 : 0);
        return employeeLeaveRepository.save(target);
    }

    @Override
    @Transactional
    public EmployeeLeave updateEmployeeLeave(
            Long id, EmployeeLeave row, boolean submit) {
        EmployeeLeave existing = getEmployeeLeaveById(id);
        if (Integer.valueOf(1).equals(existing.getStatus())) {
            throw new BadRequestException(
                    "Submitted application cannot be edited");
        }
        if (!canSeeDraft(existing)) {
            throw new ResourceNotFoundException(
                    "Employee Leave not found with id: " + id);
        }
        if (row.getEmployeeId() != null
                && !row.getEmployeeId().equals(existing.getEmployeeId())) {
            throw new BadRequestException("Employee cannot be changed");
        }
        validateAndApply(existing, row, id, submit);
        existing.setStatus(submit ? 1 : 0);
        return employeeLeaveRepository.save(existing);
    }

    @Override
    @Transactional
    public EmployeeLeave submitEmployeeLeave(Long id) {
        EmployeeLeave existing = getEmployeeLeaveById(id);
        if (Integer.valueOf(1).equals(existing.getStatus())) {
            throw new BadRequestException(
                    "Application is already submitted");
        }
        validateAndApply(existing, existing, id, true);
        existing.setStatus(1);
        return employeeLeaveRepository.save(existing);
    }

    @Override
    public void deleteEmployeeLeave(Long id) {
        EmployeeLeave existing = getEmployeeLeaveById(id);
        if (Integer.valueOf(1).equals(existing.getStatus())) {
            throw new BadRequestException(
                    "Submitted application cannot be deleted");
        }
        employeeLeaveRepository.delete(existing);
    }

    @Override
    public List<LeaveBalanceDto> getAvailableCategories(
            Long employeeId, Integer year) {
        Employee employee = loadEmployee(employeeId);
        int targetYear = year == null ? LocalDate.now().getYear() : year;
        return leaveAllocationRepository
                .findByBuIdAndLeaveYearOrderByIdDesc(
                        employee.getBuId(), targetYear)
                .stream()
                .filter(row -> Integer.valueOf(1).equals(row.getStatus()))
                .filter(row -> employee.getEmploymentTypeId()
                        .equals(row.getEmpTypeId()))
                .map(row -> toBalance(row, employeeId, targetYear))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public LeaveCalcDto calculate(
            Long employeeId, Long leaveCategoryId,
            LocalDate fromDate, LocalDate toDate) {
        Employee employee = loadEmployee(employeeId);
        if (fromDate == null) {
            throw new BadRequestException("From date cannot be empty");
        }
        LeaveAllocation allocation = findAllocation(
                employee, leaveCategoryId, fromDate.getYear());
        BigDecimal days = computeDays(
                employee, fromDate, toDate,
                Integer.valueOf(1).equals(
                        allocation.getSandwichLeaveApplicable()));
        LeaveCalcDto result = new LeaveCalcDto();
        result.setTotalLeaveDays(days);
        result.setRemainingLeaveDays(
                allocation.getTotalLeaveDays().subtract(
                        usedDays(employeeId, leaveCategoryId,
                                fromDate.getYear(), null)));
        result.setSandwichLeaveApplicable(
                allocation.getSandwichLeaveApplicable());
        return result;
    }

    private void validateAndApply(
            EmployeeLeave target, EmployeeLeave source,
            Long selfId, boolean submit) {
        if (source.getEmployeeId() == null && currentEmpId() != null) {
            source.setEmployeeId(currentEmpId());
        }
        Employee employee = loadEmployee(source.getEmployeeId());
        if (source.getFromDate() == null) {
            computeDays(employee, source.getFromDate(),
                    source.getToDate(), false);
        }
        int year = source.getFromDate() == null
                ? 0 : source.getFromDate().getYear();
        LeaveAllocation allocation = findAllocation(
                employee, source.getLeaveCategoryId(), year);
        BigDecimal days = computeDays(
                employee, source.getFromDate(), source.getToDate(),
                Integer.valueOf(1).equals(
                        allocation.getSandwichLeaveApplicable()));
        checkOverlap(employee.getId(), source.getFromDate(),
                source.getToDate(), selfId);

        if (submit) {
            BigDecimal remaining = allocation.getTotalLeaveDays().subtract(
                    usedDays(employee.getId(), source.getLeaveCategoryId(),
                            year, selfId));
            if (days.compareTo(remaining) > 0) {
                throw new BadRequestException(
                        "Insufficient leave balance. Available: "
                                + remaining.stripTrailingZeros()
                                .toPlainString()
                                + " day(s), requested: "
                                + days.stripTrailingZeros().toPlainString()
                                + " day(s)");
            }
        }

        String supportingDocument = cleanOptional(
                source.getSupportingDocument());
        if (supportingDocument != null
                && supportingDocument.length() > 255) {
            throw new BadRequestException(
                    "Supporting document reference cannot exceed 255 characters");
        }
        if (supportingDocument != null
                && supportingDocument.contains("..")) {
            throw new BadRequestException(
                    "Invalid supporting document reference");
        }
        String reason = cleanOptional(source.getReason());
        if (reason != null && reason.length() > 1000) {
            throw new BadRequestException(
                    "Reason cannot exceed 1000 characters");
        }

        target.setEmployeeId(employee.getId());
        target.setBuId(employee.getBuId());
        target.setDeptId(employee.getDeptId());
        target.setDesignationId(employee.getDesigId());
        target.setEmpTypeId(employee.getEmploymentTypeId());
        target.setFromDate(source.getFromDate());
        target.setToDate(source.getToDate());
        target.setLeaveCategoryId(source.getLeaveCategoryId());
        target.setTotalLeaveDays(days);
        target.setSupportingDocument(supportingDocument);
        target.setReason(reason);
    }

    private Employee loadEmployee(Long employeeId) {
        if (employeeId == null) {
            throw new BadRequestException("Employee cannot be empty");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));
        if (!Integer.valueOf(1).equals(employee.getStatus())) {
            throw new BadRequestException(
                    "Selected Employee is not active");
        }
        return employee;
    }

    private LeaveAllocation findAllocation(
            Employee employee, Long leaveCategoryId, int year) {
        if (leaveCategoryId == null) {
            throw new BadRequestException(
                    "Leave category cannot be empty");
        }
        LeaveAllocation allocation = leaveAllocationRepository
                .findByBuIdAndEmpTypeIdAndLeaveCategoryIdAndLeaveYear(
                        employee.getBuId(), employee.getEmploymentTypeId(),
                        leaveCategoryId, year)
                .stream()
                .filter(row -> Integer.valueOf(1).equals(row.getStatus()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "No active leave allocation for this Employee Type, "
                                + "Leave Category and Year"));
        LeaveType leaveType = leaveTypeRepository.findById(leaveCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave Type not found with id: " + leaveCategoryId));
        if (!Integer.valueOf(1).equals(leaveType.getStatus())) {
            throw new BadRequestException(
                    "Selected Leave Category is not active");
        }
        return allocation;
    }

    private BigDecimal usedDays(
            Long employeeId, Long leaveCategoryId,
            int year, Long excludeId) {
        BigDecimal total = BigDecimal.ZERO;
        for (EmployeeLeave leave : employeeLeaveRepository
                .findByEmployeeIdAndLeaveCategoryIdAndStatus(
                        employeeId, leaveCategoryId, 1)) {
            if (leave.getFromDate().getYear() == year
                    && (excludeId == null
                    || !leave.getId().equals(excludeId))) {
                total = total.add(leave.getTotalLeaveDays());
            }
        }
        return total;
    }

    private Set<LocalDate> nonWorkingDays(
            Employee employee, LocalDate from, LocalDate to) {
        Set<LocalDate> result = new HashSet<>();

        // Employee-te ar shiftId nai, tai Attendance Config theke
        Shift shift = findEmployeeShift(employee);

        Set<String> weekends = new HashSet<>();
        if (shift != null && shift.getWeekendDays() != null
                && !shift.getWeekendDays().isBlank()) {
            for (String code : shift.getWeekendDays().split(",")) {
                weekends.add(code.trim().toUpperCase(Locale.ROOT));
            }
        }
        for (LocalDate date = from;
             !date.isAfter(to); date = date.plusDays(1)) {
            if (weekends.contains(date.getDayOfWeek().name()
                    .substring(0, 3))) {
                result.add(date);
            }
        }

        Set<Integer> years = new HashSet<>();
        years.add(from.getYear());
        years.add(to.getYear());
        for (Integer year : years) {
            for (OfficeCalendar calendar : officeCalendarRepository
                    .findByBuIdAndYearOrderByIdDesc(
                            employee.getBuId(), year)) {
                if (!Integer.valueOf(1).equals(calendar.getStatus())) {
                    continue;
                }
                for (OfficeCalendarDtl detail : officeCalendarDtlRepository
                        .findByOfficeCalendarIdAndStatusOrderByCalendarDateAsc(
                                calendar.getId(), 1)) {
                    LocalDate date = detail.getCalendarDate();
                    if (date.isBefore(from) || date.isAfter(to)) {
                        continue;
                    }
                    HrLookupValue event = hrLookupValueRepository
                            .findById(detail.getEventTypeId()).orElse(null);
                    if (event != null && ("Holiday".equalsIgnoreCase(
                            event.getValue()) || "Weekend".equalsIgnoreCase(
                            event.getValue()))) {
                        result.add(date);
                    }
                }
            }
        }
        return result;
    }

    // Employee-r active Attendance Config-er prothom active Shift (na thakle null)
    private Shift findEmployeeShift(Employee employee) {
        return empAttConfigRepository.findByEmployeeId(employee.getId())
                .stream()
                .filter(c -> Integer.valueOf(1).equals(c.getStatus()))
                .findFirst()
                .flatMap(c -> empAttShiftRepository
                        .findByAttConfigIdOrderByIdAsc(c.getId()).stream()
                        .filter(r -> Integer.valueOf(1).equals(r.getStatus()))
                        .findFirst())
                .flatMap(r -> shiftRepository.findById(r.getShiftId()))
                .orElse(null);
    }

    private BigDecimal countDays(
            LocalDate from, LocalDate to,
            Set<LocalDate> nonWorking, boolean sandwich) {
        if (!sandwich) {
            long count = 0;
            for (LocalDate date = from;
                    !date.isAfter(to); date = date.plusDays(1)) {
                if (!nonWorking.contains(date)) {
                    count++;
                }
            }
            return BigDecimal.valueOf(count).setScale(2);
        }
        LocalDate firstWorking = null;
        LocalDate lastWorking = null;
        for (LocalDate date = from;
                !date.isAfter(to); date = date.plusDays(1)) {
            if (!nonWorking.contains(date)) {
                if (firstWorking == null) {
                    firstWorking = date;
                }
                lastWorking = date;
            }
        }
        if (firstWorking == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        long count = java.time.temporal.ChronoUnit.DAYS
                .between(firstWorking, lastWorking) + 1;
        return BigDecimal.valueOf(count).setScale(2);
    }

    private BigDecimal computeDays(
            Employee employee, LocalDate from, LocalDate to,
            boolean sandwich) {
        if (from == null) {
            throw new BadRequestException("From date cannot be empty");
        }
        if (to == null) {
            throw new BadRequestException("To date cannot be empty");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException(
                    "To date cannot be before from date");
        }
        if (from.getYear() != to.getYear()) {
            throw new BadRequestException(
                    "Leave period must be within a single year");
        }
        BigDecimal days = countDays(
                from, to, nonWorkingDays(employee, from, to), sandwich);
        if (days.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Selected period has no working days");
        }
        return days;
    }

    private void checkOverlap(
            Long employeeId, LocalDate from, LocalDate to, Long excludeId) {
        for (EmployeeLeave leave : employeeLeaveRepository
                .findByEmployeeIdAndStatus(employeeId, 1)) {
            if (excludeId != null && leave.getId().equals(excludeId)) {
                continue;
            }
            if (!from.isAfter(leave.getToDate())
                    && !to.isBefore(leave.getFromDate())) {
                throw new DuplicateResourceException(
                        "Leave period overlaps with an existing application: "
                                + leave.getFromDate() + " to "
                                + leave.getToDate());
            }
        }
    }

    private LeaveBalanceDto toBalance(
            LeaveAllocation allocation, Long employeeId, int year) {
        Optional<LeaveType> leaveType = leaveTypeRepository
                .findById(allocation.getLeaveCategoryId());
        if (leaveType.isEmpty()
                || !Integer.valueOf(1).equals(leaveType.get().getStatus())) {
            return null;
        }
        BigDecimal used = usedDays(
                employeeId, allocation.getLeaveCategoryId(), year, null);
        LeaveBalanceDto result = new LeaveBalanceDto();
        result.setLeaveCategoryId(allocation.getLeaveCategoryId());
        result.setLeaveCategoryName(leaveType.get().getTypeName());
        result.setTotalLeaveDays(allocation.getTotalLeaveDays());
        result.setUsedLeaveDays(used);
        result.setRemainingLeaveDays(
                allocation.getTotalLeaveDays().subtract(used));
        result.setSandwichLeaveApplicable(
                allocation.getSandwichLeaveApplicable());
        result.setWithPay(allocation.getWithPay());
        return result;
    }

    private Long currentEmpId() {
        return CurrentUser.get().map(AuthUser::empId).orElse(null);
    }

    private Long currentUserId() {
        return CurrentUser.get().map(AuthUser::userId).orElse(null);
    }

    private boolean canSeeDraft(EmployeeLeave leave) {
        return leave.getEmployeeId().equals(currentEmpId())
                || (currentUserId() != null
                && currentUserId().equals(leave.getCreatedUserId()));
    }

    private String cleanOptional(String value) {
        return value == null || value.trim().isEmpty()
                ? null : value.trim();
    }
}
