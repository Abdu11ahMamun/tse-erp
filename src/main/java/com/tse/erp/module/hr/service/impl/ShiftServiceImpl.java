package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.Shift;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.hr.repository.ShiftRepository;
import com.tse.erp.module.hr.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<Shift> getAllShifts(Long buId) {
        if (buId == null) {
            return shiftRepository.findAllByOrderByIdDesc();
        }
        return shiftRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shift not found with id: " + id));
    }

    @Override
    public Shift createShift(Shift shift) {
        prepare(shift);
        StatusUtil.validate(shift.getStatus());
        if (!shiftRepository.findByBuIdAndShiftTitleIgnoreCase(
                shift.getBuId(), shift.getShiftTitle()).isEmpty()) {
            throw new DuplicateResourceException(
                    "Shift title already exists: " + shift.getShiftTitle());
        }
        shift.setId(null);
        return shiftRepository.save(shift);
    }

    @Override
    public Shift updateShift(Long id, Shift shift) {
        Shift existing = getShiftById(id);
        prepare(shift);
        StatusUtil.validate(shift.getStatus());
        boolean duplicateExists = shiftRepository
                .findByBuIdAndShiftTitleIgnoreCase(
                        shift.getBuId(), shift.getShiftTitle())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Shift title already exists: " + shift.getShiftTitle());
        }

        existing.setBuId(shift.getBuId());
        existing.setShiftTitle(shift.getShiftTitle());
        existing.setShiftStartTime(shift.getShiftStartTime());
        existing.setShiftEndTime(shift.getShiftEndTime());
        existing.setApplyLateIn(shift.getApplyLateIn());
        existing.setLateInDays(shift.getLateInDays());
        existing.setLateInDeduction(shift.getLateInDeduction());
        existing.setApplyEarlyOut(shift.getApplyEarlyOut());
        existing.setEarlyOutDays(shift.getEarlyOutDays());
        existing.setEarlyOutDeduction(shift.getEarlyOutDeduction());
        existing.setWeekendDays(shift.getWeekendDays());
        existing.setGraceTimeMinutes(shift.getGraceTimeMinutes());
        existing.setLateInMessage(shift.getLateInMessage());
        existing.setEarlyOutMessage(shift.getEarlyOutMessage());
        existing.setAllowLateInMsg(shift.getAllowLateInMsg());
        existing.setAllowEarlyOutMsg(shift.getAllowEarlyOutMsg());
        existing.setAttnInOutPolicy(shift.getAttnInOutPolicy());
        existing.setStartBefore(shift.getStartBefore());
        existing.setCloseAfter(shift.getCloseAfter());
        existing.setNotAllowAttn(shift.getNotAllowAttn());
        existing.setInTimeStart(shift.getInTimeStart());
        existing.setInTimeClose(shift.getInTimeClose());
        existing.setOutTimeStart(shift.getOutTimeStart());
        existing.setOutTimeClose(shift.getOutTimeClose());
        existing.setAllowNextDayAttn(shift.getAllowNextDayAttn());
        existing.setAllowOvertime(shift.getAllowOvertime());
        existing.setMaxOtHour(shift.getMaxOtHour());
        existing.setExtraOtHour(shift.getExtraOtHour());
        if (shift.getStatus() != null) {
            existing.setStatus(shift.getStatus());
        }
        return shiftRepository.save(existing);
    }

    @Override
    public void deleteShift(Long id) {
        Shift existing = getShiftById(id);
        if (employeeRepository.existsByShiftId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Employees are assigned to this Shift");
        }
        shiftRepository.delete(existing);
    }

    private void prepare(Shift shift) {
        validateBasic(shift);
        validatePolicies(shift);
        validateAttendance(shift);
        validateOvertime(shift);
    }

    private void validateBasic(Shift shift) {
        if (shift.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository.findById(shift.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: "
                                + shift.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }

        if (shift.getShiftTitle() == null
                || shift.getShiftTitle().trim().isEmpty()) {
            throw new BadRequestException("Shift title cannot be empty");
        }
        String title = shift.getShiftTitle().trim();
        if (title.length() < 2) {
            throw new BadRequestException(
                    "Shift title must be at least 2 characters");
        }
        if (title.length() > 100) {
            throw new BadRequestException(
                    "Shift title cannot exceed 100 characters");
        }
        shift.setShiftTitle(title);

        if (shift.getShiftStartTime() == null) {
            throw new BadRequestException(
                    "Shift start time cannot be empty");
        }
        if (shift.getShiftEndTime() == null) {
            throw new BadRequestException(
                    "Shift end time cannot be empty");
        }
        if (shift.getShiftStartTime().equals(shift.getShiftEndTime())) {
            throw new BadRequestException(
                    "Shift start time and end time must be different");
        }

        shift.setApplyLateIn(flag(
                shift.getApplyLateIn(), "Apply Late In"));
        shift.setApplyEarlyOut(flag(
                shift.getApplyEarlyOut(), "Apply Early Out"));
        shift.setAllowLateInMsg(flag(
                shift.getAllowLateInMsg(), "Allow Late In Message"));
        shift.setAllowEarlyOutMsg(flag(
                shift.getAllowEarlyOutMsg(), "Allow Early Out Message"));
        shift.setAttnInOutPolicy(flag(
                shift.getAttnInOutPolicy(), "Attendance In Out Policy"));
        shift.setNotAllowAttn(flag(
                shift.getNotAllowAttn(), "Not Allow Attendance"));
        shift.setAllowNextDayAttn(flag(
                shift.getAllowNextDayAttn(), "Allow Next Day Attendance"));
        shift.setAllowOvertime(flag(
                shift.getAllowOvertime(), "Allow Overtime"));

        if (shift.getShiftEndTime().isBefore(shift.getShiftStartTime())
                && shift.getAllowNextDayAttn() != 1) {
            throw new BadRequestException(
                    "End time earlier than start time (overnight shift) "
                            + "requires Next Day Attendance");
        }

        if (shift.getGraceTimeMinutes() == null) {
            throw new BadRequestException(
                    "Grace time cannot be empty");
        }
        if (shift.getGraceTimeMinutes() < 0
                || shift.getGraceTimeMinutes() > 999) {
            throw new BadRequestException(
                    "Grace time must be between 0 and 999");
        }

        normalizeWeekendDays(shift);
    }

    private void validatePolicies(Shift shift) {
        if (shift.getApplyLateIn() == 1) {
            if (shift.getLateInDays() == null) {
                throw new BadRequestException(
                        "Late in days cannot be empty");
            }
            if (shift.getLateInDays() < 1
                    || shift.getLateInDays() > 31) {
                throw new BadRequestException(
                        "Late in days must be between 1 and 31");
            }
            BigDecimal deduction = shift.getLateInDeduction();
            if (deduction == null) {
                throw new BadRequestException(
                        "Late in deduction cannot be empty");
            }
            if (deduction.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(
                        "Late in deduction must be greater than 0");
            }
            if (deduction.compareTo(BigDecimal.ONE) > 0) {
                throw new BadRequestException(
                        "Late in deduction cannot exceed 1 day");
            }
            shift.setLateInDeduction(
                    deduction.setScale(2, RoundingMode.HALF_UP));
        } else {
            shift.setLateInDays(null);
            shift.setLateInDeduction(null);
        }

        if (shift.getApplyEarlyOut() == 1) {
            if (shift.getEarlyOutDays() == null) {
                throw new BadRequestException(
                        "Early out days cannot be empty");
            }
            if (shift.getEarlyOutDays() < 1
                    || shift.getEarlyOutDays() > 31) {
                throw new BadRequestException(
                        "Early out days must be between 1 and 31");
            }
            BigDecimal deduction = shift.getEarlyOutDeduction();
            if (deduction == null) {
                throw new BadRequestException(
                        "Early out deduction cannot be empty");
            }
            if (deduction.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(
                        "Early out deduction must be greater than 0");
            }
            if (deduction.compareTo(BigDecimal.ONE) > 0) {
                throw new BadRequestException(
                        "Early out deduction cannot exceed 1 day");
            }
            shift.setEarlyOutDeduction(
                    deduction.setScale(2, RoundingMode.HALF_UP));
        } else {
            shift.setEarlyOutDays(null);
            shift.setEarlyOutDeduction(null);
        }

        shift.setLateInMessage(cleanMessage(
                shift.getLateInMessage(), "Late in message"));
        shift.setEarlyOutMessage(cleanMessage(
                shift.getEarlyOutMessage(), "Early out message"));
        if (shift.getAllowLateInMsg() == 1
                && shift.getLateInMessage() == null) {
            throw new BadRequestException(
                    "Late in message is required when Allow Late In Message is enabled");
        }
        if (shift.getAllowEarlyOutMsg() == 1
                && shift.getEarlyOutMessage() == null) {
            throw new BadRequestException(
                    "Early out message is required when Allow Early Out Message is enabled");
        }
    }

    private void validateAttendance(Shift shift) {
        if (shift.getAttnInOutPolicy() == 1) {
            if (shift.getStartBefore() == null) {
                throw new BadRequestException(
                        "Start before cannot be empty");
            }
            if (shift.getCloseAfter() == null) {
                throw new BadRequestException(
                        "Close after cannot be empty");
            }
            validateMinutes(shift.getStartBefore(), "Start before");
            validateMinutes(shift.getCloseAfter(), "Close after");

            requireTime(shift.getInTimeStart(), "In time start");
            requireTime(shift.getInTimeClose(), "In time close");
            requireTime(shift.getOutTimeStart(), "Out time start");
            requireTime(shift.getOutTimeClose(), "Out time close");

            validateTimePair(
                    shift.getInTimeStart(), shift.getInTimeClose(),
                    "In time start and close must be different",
                    "In time start must be before In time close",
                    shift.getAllowNextDayAttn() == 1);
            validateTimePair(
                    shift.getOutTimeStart(), shift.getOutTimeClose(),
                    "Out time start and close must be different",
                    "Out time start must be before Out time close",
                    shift.getAllowNextDayAttn() == 1);
        } else {
            shift.setStartBefore(null);
            shift.setCloseAfter(null);
            shift.setInTimeStart(null);
            shift.setInTimeClose(null);
            shift.setOutTimeStart(null);
            shift.setOutTimeClose(null);
        }
    }

    private void validateOvertime(Shift shift) {
        if (shift.getAllowOvertime() == 1) {
            BigDecimal maxOtHour = shift.getMaxOtHour();
            if (maxOtHour == null) {
                throw new BadRequestException(
                        "Maximum OT hour cannot be empty");
            }
            if (maxOtHour.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(
                        "Maximum OT hour must be greater than 0");
            }
            if (maxOtHour.compareTo(new BigDecimal("24")) > 0) {
                throw new BadRequestException(
                        "Maximum OT hour cannot exceed 24");
            }
            BigDecimal extraOtHour = shift.getExtraOtHour();
            if (extraOtHour == null) {
                throw new BadRequestException(
                        "Extra OT hour cannot be empty");
            }
            if (extraOtHour.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException(
                        "Extra OT hour cannot be negative");
            }
            if (extraOtHour.compareTo(maxOtHour) > 0) {
                throw new BadRequestException(
                        "Extra OT hour cannot exceed Maximum OT hour");
            }
            shift.setMaxOtHour(
                    maxOtHour.setScale(2, RoundingMode.HALF_UP));
            shift.setExtraOtHour(
                    extraOtHour.setScale(2, RoundingMode.HALF_UP));
        } else {
            shift.setMaxOtHour(null);
            shift.setExtraOtHour(null);
        }
    }

    private int flag(Integer value, String label) {
        if (value == null) {
            return 0;
        }
        if (value != 0 && value != 1) {
            throw new BadRequestException(label + " must be 0 or 1");
        }
        return value;
    }

    private void normalizeWeekendDays(Shift shift) {
        if (shift.getWeekendDays() == null
                || shift.getWeekendDays().trim().isEmpty()) {
            throw new BadRequestException("Weekend cannot be empty");
        }
        List<String> codes = new ArrayList<>();
        for (String raw : shift.getWeekendDays().split(",")) {
            String token = raw.trim();
            String code = token.length() >= 3
                    ? token.substring(0, 3).toUpperCase()
                    : token.toUpperCase();
            if (!List.of("MON", "TUE", "WED", "THU",
                    "FRI", "SAT", "SUN").contains(code)) {
                throw new BadRequestException(
                        "Invalid weekend day: " + token);
            }
            if (codes.contains(code)) {
                throw new BadRequestException(
                        "Duplicate weekend day: " + code);
            }
            codes.add(code);
        }
        codes.sort(Comparator.comparingInt(this::weekendOrder));
        shift.setWeekendDays(String.join(",", codes));
    }

    private int weekendOrder(String code) {
        return List.of("MON", "TUE", "WED", "THU",
                "FRI", "SAT", "SUN").indexOf(code);
    }

    private String cleanMessage(String message, String label) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        String trimmed = message.trim();
        if (trimmed.length() > 500) {
            throw new BadRequestException(
                    label + " cannot exceed 500 characters");
        }
        if (trimmed.contains("<") || trimmed.contains(">")) {
            throw new BadRequestException(
                    label + " contains unsupported characters");
        }
        return trimmed;
    }

    private void validateMinutes(Integer value, String label) {
        if (value < 0 || value > 1440) {
            throw new BadRequestException(
                    label + " must be between 0 and 1440 minutes");
        }
    }

    private void requireTime(LocalTime value, String label) {
        if (value == null) {
            throw new BadRequestException(label + " cannot be empty");
        }
    }

    private void validateTimePair(
            LocalTime start, LocalTime close, String equalMessage,
            String orderMessage, boolean allowNextDay) {
        if (start.equals(close)) {
            throw new BadRequestException(equalMessage);
        }
        if (!allowNextDay && !start.isBefore(close)) {
            throw new BadRequestException(orderMessage);
        }
    }
}
