package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.dto.OfficeCalendarDayDto;
import com.tse.erp.module.hr.dto.OfficeCalendarDto;
import com.tse.erp.module.hr.entity.HrLookupValue;
import com.tse.erp.module.hr.entity.OfficeCalendar;
import com.tse.erp.module.hr.entity.OfficeCalendarDtl;
import com.tse.erp.module.hr.repository.HrLookupValueRepository;
import com.tse.erp.module.hr.repository.OfficeCalendarDtlRepository;
import com.tse.erp.module.hr.repository.OfficeCalendarRepository;
import com.tse.erp.module.hr.service.OfficeCalendarService;
import com.tse.erp.module.hr.validation.HrLookupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OfficeCalendarServiceImpl
        implements OfficeCalendarService {

    private final OfficeCalendarRepository officeCalendarRepository;
    private final OfficeCalendarDtlRepository officeCalendarDtlRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final HrLookupValueRepository hrLookupValueRepository;
    private final HrLookupValidator hrLookupValidator;

    @Override
    public List<OfficeCalendarDto> getAllOfficeCalendars(
            Long buId, Integer year) {
        List<OfficeCalendar> calendars;
        if (buId != null && year != null) {
            calendars = officeCalendarRepository
                    .findByBuIdAndYearOrderByIdDesc(buId, year);
        } else if (buId != null) {
            calendars = officeCalendarRepository
                    .findByBuIdOrderByIdDesc(buId);
        } else if (year != null) {
            calendars = officeCalendarRepository
                    .findByYearOrderByIdDesc(year);
        } else {
            calendars = officeCalendarRepository.findAllByOrderByIdDesc();
        }
        return calendars.stream()
                .map(calendar -> toDto(calendar, false))
                .toList();
    }

    @Override
    public OfficeCalendarDto getOfficeCalendarById(Long id) {
        return toDto(findCalendar(id), true);
    }

    @Override
    @Transactional
    public List<OfficeCalendarDto> createOfficeCalendars(
            OfficeCalendarDto dto) {
        List<Long> buIds = new ArrayList<>();
        if (dto.getBuIds() != null && !dto.getBuIds().isEmpty()) {
            buIds.addAll(dto.getBuIds());
        } else {
            if (dto.getBuId() == null) {
                throw new BadRequestException(
                        "Business Unit cannot be empty");
            }
            buIds.add(dto.getBuId());
        }
        buIds = new ArrayList<>(new LinkedHashSet<>(buIds));

        StatusUtil.validate(dto.getStatus());
        int year = validateYear(dto.getYear());
        Map<Long, Integer> months = new HashMap<>();

        for (Long buId : buIds) {
            BusinessUnit bu = loadBu(buId);
            int month = validateMonth(dto.getMonthId(), bu.getBgId());
            months.put(buId, month);
            validateDays(dto.getDays(), year, month, bu.getBgId());
            if (!Integer.valueOf(0).equals(dto.getStatus())
                    && officeCalendarRepository
                    .findByBuIdAndYearAndMonthId(
                            buId, year, dto.getMonthId())
                    .stream()
                    .anyMatch(c -> Integer.valueOf(1).equals(c.getStatus()))) {
                throw new DuplicateResourceException(
                        "An active calendar already exists for Business Unit "
                                + buId + ", this Year and Month");
            }
        }

        List<OfficeCalendarDto> result = new ArrayList<>();
        for (Long buId : buIds) {
            OfficeCalendar calendar = new OfficeCalendar();
            calendar.setBuId(buId);
            calendar.setYear(year);
            calendar.setMonthId(dto.getMonthId());
            if (dto.getStatus() != null) {
                calendar.setStatus(dto.getStatus());
            }
            OfficeCalendar saved = officeCalendarRepository.save(calendar);
            List<OfficeCalendarDtl> details = new ArrayList<>();
            if (dto.getDays() != null) {
                for (OfficeCalendarDayDto row : dto.getDays()) {
                    OfficeCalendarDtl detail = new OfficeCalendarDtl();
                    detail.setOfficeCalendarId(saved.getId());
                    detail.setCalendarDate(row.getCalendarDate());
                    detail.setCalendarDay(row.getCalendarDay());
                    detail.setEventTypeId(row.getEventTypeId());
                    detail.setRemarks(row.getRemarks());
                    if (row.getStatus() != null) {
                        detail.setStatus(row.getStatus());
                    }
                    details.add(detail);
                }
            }
            officeCalendarDtlRepository.saveAll(details);
            result.add(toDto(saved, true));
        }
        return result;
    }

    @Override
    @Transactional
    public OfficeCalendarDto updateOfficeCalendar(
            Long id, OfficeCalendarDto dto) {
        OfficeCalendar calendar = findCalendar(id);
        StatusUtil.validate(dto.getStatus());
        BusinessUnit bu = loadBu(calendar.getBuId());
        int year = validateYear(dto.getYear());
        int month = validateMonth(dto.getMonthId(), bu.getBgId());
        validateDays(dto.getDays(), year, month, bu.getBgId());

        int effectiveStatus = dto.getStatus() != null
                ? dto.getStatus()
                : calendar.getStatus();
        if (Integer.valueOf(1).equals(effectiveStatus)
                && officeCalendarRepository
                .findByBuIdAndYearAndMonthId(
                        calendar.getBuId(), year, dto.getMonthId())
                .stream()
                .anyMatch(c -> Integer.valueOf(1).equals(c.getStatus())
                        && !c.getId().equals(id))) {
            throw new DuplicateResourceException(
                    "An active calendar already exists for Business Unit "
                            + calendar.getBuId() + ", this Year and Month");
        }

        calendar.setYear(year);
        calendar.setMonthId(dto.getMonthId());
        if (dto.getStatus() != null) {
            calendar.setStatus(dto.getStatus());
        }
        officeCalendarRepository.save(calendar);

        List<OfficeCalendarDtl> current =
                officeCalendarDtlRepository
                        .findByOfficeCalendarIdOrderByCalendarDateAsc(id);
        Map<Long, OfficeCalendarDtl> byId = new HashMap<>();
        current.forEach(row -> byId.put(row.getId(), row));
        Set<Long> handled = new HashSet<>();
        List<OfficeCalendarDtl> newRows = new ArrayList<>();
        List<OfficeCalendarDayDto> requestedRows = dto.getDays() == null
                ? Collections.emptyList() : dto.getDays();

        for (OfficeCalendarDayDto row : requestedRows) {
            if (row.getId() != null) {
                OfficeCalendarDtl detail = byId.get(row.getId());
                if (detail == null) {
                    throw new BadRequestException(
                            "Day id " + row.getId()
                                    + " does not belong to this Calendar");
                }
                detail.setCalendarDate(row.getCalendarDate());
                detail.setCalendarDay(row.getCalendarDay());
                detail.setEventTypeId(row.getEventTypeId());
                detail.setRemarks(row.getRemarks());
                if (row.getStatus() != null) {
                    detail.setStatus(row.getStatus());
                }
                handled.add(row.getId());
            } else {
                OfficeCalendarDtl detail = new OfficeCalendarDtl();
                detail.setOfficeCalendarId(id);
                detail.setCalendarDate(row.getCalendarDate());
                detail.setCalendarDay(row.getCalendarDay());
                detail.setEventTypeId(row.getEventTypeId());
                detail.setRemarks(row.getRemarks());
                if (row.getStatus() != null) {
                    detail.setStatus(row.getStatus());
                }
                newRows.add(detail);
            }
        }

        for (OfficeCalendarDtl detail : current) {
            if (!handled.contains(detail.getId())) {
                detail.setStatus(0);
            }
        }

        List<OfficeCalendarDtl> allRows = new ArrayList<>(current);
        allRows.addAll(newRows);
        Set<LocalDate> dates = new HashSet<>();
        for (OfficeCalendarDtl detail : allRows) {
            if (Integer.valueOf(1).equals(detail.getStatus())
                    && !dates.add(detail.getCalendarDate())) {
                throw new DuplicateResourceException(
                        "Date already exists in this Calendar: "
                                + detail.getCalendarDate());
            }
        }
        officeCalendarDtlRepository.saveAll(allRows);
        return toDto(calendar, true);
    }

    @Override
    @Transactional
    public void deleteOfficeCalendar(Long id) {
        OfficeCalendar calendar = findCalendar(id);
        List<OfficeCalendarDtl> details = officeCalendarDtlRepository
                .findByOfficeCalendarIdOrderByCalendarDateAsc(id);
        officeCalendarDtlRepository.deleteAll(details);
        // TODO: block delete when attendance uses this calendar.
        officeCalendarRepository.delete(calendar);
    }

    private OfficeCalendar findCalendar(Long id) {
        return officeCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office Calendar not found with id: " + id));
    }

    private OfficeCalendarDto toDto(
            OfficeCalendar calendar, boolean withDays) {
        OfficeCalendarDto dto = new OfficeCalendarDto();
        dto.setId(calendar.getId());
        dto.setBuId(calendar.getBuId());
        dto.setYear(calendar.getYear());
        dto.setMonthId(calendar.getMonthId());
        dto.setStatus(calendar.getStatus());
        if (withDays) {
            List<OfficeCalendarDayDto> days = officeCalendarDtlRepository
                    .findByOfficeCalendarIdAndStatusOrderByCalendarDateAsc(
                            calendar.getId(), 1)
                    .stream()
                    .map(this::toDayDto)
                    .toList();
            dto.setDays(days);
        }
        return dto;
    }

    private OfficeCalendarDayDto toDayDto(OfficeCalendarDtl detail) {
        OfficeCalendarDayDto row = new OfficeCalendarDayDto();
        row.setId(detail.getId());
        row.setCalendarDate(detail.getCalendarDate());
        row.setCalendarDay(detail.getCalendarDay());
        row.setEventTypeId(detail.getEventTypeId());
        row.setRemarks(detail.getRemarks());
        row.setStatus(detail.getStatus());
        return row;
    }

    private BusinessUnit loadBu(Long buId) {
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }
        return bu;
    }

    private int validateYear(Integer year) {
        if (year == null) {
            throw new BadRequestException("Year cannot be empty");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException(
                    "Year must be between 2000 and 2100");
        }
        return year;
    }

    private int validateMonth(Long monthId, Long bgId) {
        hrLookupValidator.validate(monthId, bgId, "Month", "Month");
        HrLookupValue monthValue = hrLookupValueRepository
                .findById(monthId).get();
        return monthNumber(monthValue.getValue());
    }

    private int monthNumber(String value) {
        String trimmed = value.trim();
        if (trimmed.matches("\\d{1,2}")) {
            int number = Integer.parseInt(trimmed);
            if (number >= 1 && number <= 12) {
                return number;
            }
        }
        for (Month month : Month.values()) {
            if (trimmed.equalsIgnoreCase(month.name())
                    || trimmed.equalsIgnoreCase(
                            month.name().substring(0, 3))) {
                return month.getValue();
            }
        }
        throw new BadRequestException(
                "Month value must be a month name (January to December) "
                        + "or number 1 to 12");
    }

    private void validateDays(
            List<OfficeCalendarDayDto> days, int year, int month, Long bgId) {
        if (days == null) {
            return;
        }
        for (OfficeCalendarDayDto row : days) {
            StatusUtil.validate(row.getStatus());
            LocalDate date = row.getCalendarDate();
            if (date == null) {
                throw new BadRequestException("Date cannot be empty");
            }
            if (date.getYear() != year || date.getMonthValue() != month) {
                throw new BadRequestException(
                        "Date " + date
                                + " is not in the selected Year and Month");
            }
            hrLookupValidator.validate(
                    row.getEventTypeId(), bgId, "Event Type", "Event type");
            String remarks = row.getRemarks();
            remarks = remarks == null || remarks.trim().isEmpty()
                    ? null : remarks.trim();
            if (remarks != null && remarks.length() > 500) {
                throw new BadRequestException(
                        "Remarks cannot exceed 500 characters");
            }
            row.setRemarks(remarks);
            row.setCalendarDay(date.getDayOfWeek().getDisplayName(
                    TextStyle.FULL, Locale.ENGLISH));
        }
        Set<LocalDate> dates = new HashSet<>();
        for (OfficeCalendarDayDto row : days) {
            if ((row.getStatus() == null
                    || Integer.valueOf(1).equals(row.getStatus()))
                    && !dates.add(row.getCalendarDate())) {
                throw new DuplicateResourceException(
                        "Duplicate date in request: "
                                + row.getCalendarDate());
            }
        }
    }
}
