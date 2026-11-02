package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmCalendarMonthDto;
import com.tse.erp.module.accounting.dto.AfmCalendarRequestDto;
import com.tse.erp.module.accounting.dto.AfmCalendarResponseDto;
import com.tse.erp.module.accounting.entity.AfmCalendar;
import com.tse.erp.module.accounting.entity.AfmCalendarMonth;
import com.tse.erp.module.accounting.repository.AfmCalendarMonthRepository;
import com.tse.erp.module.accounting.repository.AfmCalendarRepository;
import com.tse.erp.module.accounting.service.AfmCalendarService;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AfmCalendarServiceImpl implements AfmCalendarService {

    private final AfmCalendarRepository calendarRepository;
    private final AfmCalendarMonthRepository calendarMonthRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmCalendarResponseDto> getAllCalendars(Long bgId, Long buId, Integer status) {
        List<AfmCalendar> calendars;

        if (bgId != null && buId != null && status != null) {
            calendars = calendarRepository.findByBgIdAndBuIdAndStatusOrderByIdDesc(bgId, buId, status);
        } else if (bgId != null && buId != null) {
            calendars = calendarRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null && status != null) {
            calendars = calendarRepository.findByBuIdAndStatusOrderByIdDesc(buId, status);
        } else if (buId != null) {
            calendars = calendarRepository.findByBuIdOrderByIdDesc(buId);
        } else if (bgId != null) {
            calendars = calendarRepository.findByBgIdOrderByIdDesc(bgId);
        } else {
            calendars = calendarRepository.findAllByOrderByIdDesc();
        }

        if (status != null && (bgId == null || buId == null)) {
            calendars = calendars.stream().filter(c -> status.equals(c.getStatus())).toList();
        }

        return toResponseList(calendars);
    }

    @Override
    public AfmCalendarResponseDto getCalendarById(Long id) {
        AfmCalendar calendar = findCalendar(id);
        AfmCalendarResponseDto response = toResponse(calendar);
        response.setCalendarMonths(getCalendarMonths(id));
        return response;
    }

    @Override
    public List<AfmCalendarMonthDto> getCalendarMonths(Long calendarId) {
        findCalendar(calendarId);
        return calendarMonthRepository.findByAfmCalendarIdOrderBySerialNoAsc(calendarId)
                .stream()
                .map(this::toMonthDto)
                .toList();
    }

    @Override
    @Transactional
    public AfmCalendarResponseDto createCalendar(AfmCalendarRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateCalendarFields(request);
        StatusUtil.validate(request.getStatus());

        String year = request.getCalendarYear().trim();
        boolean exists = !calendarRepository.findByBuIdAndCalendarYearIgnoreCase(request.getBuId(), year).isEmpty();
        if (exists) {
            throw new DuplicateResourceException("Accounting Calendar for year " + year + " already exists for this Business Unit");
        }

        AfmCalendar calendar = AfmCalendar.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .calendarYear(year)
                .calendarType(request.getCalendarType().trim())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .period(request.getPeriod() != null ? request.getPeriod().trim() : null)
                .build();

        calendar.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        calendar.setCreatedApp(AppConstants.APP_NAME);
        calendar = calendarRepository.save(calendar);

        List<AfmCalendarMonthDto> savedMonths = new ArrayList<>();
        if (request.getCalendarMonths() != null && !request.getCalendarMonths().isEmpty()) {
            final Long calId = calendar.getId();
            List<AfmCalendarMonth> months = request.getCalendarMonths().stream().map(dto -> {
                AfmCalendarMonth month = toMonthEntity(dto);
                month.setAfmCalendarId(calId);
                month.setCreatedApp(AppConstants.APP_NAME);
                return month;
            }).toList();
            savedMonths = calendarMonthRepository.saveAll(months).stream().map(this::toMonthDto).toList();
        }

        AfmCalendarResponseDto response = toResponse(calendar);
        response.setCalendarMonths(savedMonths);
        return response;
    }

    @Override
    @Transactional
    public AfmCalendarResponseDto updateCalendar(Long id, AfmCalendarRequestDto request) {
        AfmCalendar calendar = findCalendar(id);
        validateParents(request.getBgId(), request.getBuId());
        validateCalendarFields(request);
        StatusUtil.validate(request.getStatus());

        String year = request.getCalendarYear().trim();
        List<AfmCalendar> existing = calendarRepository.findByBuIdAndCalendarYearIgnoreCase(request.getBuId(), year);
        boolean duplicate = existing.stream().anyMatch(c -> !c.getId().equals(id));
        if (duplicate) {
            throw new DuplicateResourceException("Accounting Calendar for year " + year + " already exists for this Business Unit");
        }

        calendar.setBgId(request.getBgId());
        calendar.setBuId(request.getBuId());
        calendar.setCalendarYear(year);
        calendar.setCalendarType(request.getCalendarType().trim());
        calendar.setFromDate(request.getFromDate());
        calendar.setToDate(request.getToDate());
        if (request.getPeriod() != null) {
            calendar.setPeriod(request.getPeriod().trim());
        }
        if (request.getStatus() != null) {
            calendar.setStatus(request.getStatus());
        }
        calendar.setUpdatedApp(AppConstants.APP_NAME);
        calendar = calendarRepository.save(calendar);

        // Synchronize months if provided
        List<AfmCalendarMonthDto> updatedMonths = new ArrayList<>();
        if (request.getCalendarMonths() != null) {
            calendarMonthRepository.deleteByAfmCalendarId(id);
            final Long calId = calendar.getId();
            List<AfmCalendarMonth> months = request.getCalendarMonths().stream().map(dto -> {
                AfmCalendarMonth month = toMonthEntity(dto);
                month.setId(null);
                month.setAfmCalendarId(calId);
                month.setCreatedApp(AppConstants.APP_NAME);
                month.setUpdatedApp(AppConstants.APP_NAME);
                return month;
            }).toList();
            updatedMonths = calendarMonthRepository.saveAll(months).stream().map(this::toMonthDto).toList();
        } else {
            updatedMonths = getCalendarMonths(id);
        }

        AfmCalendarResponseDto response = toResponse(calendar);
        response.setCalendarMonths(updatedMonths);
        return response;
    }

    @Override
    @Transactional
    public void deleteCalendar(Long id) {
        findCalendar(id);
        calendarMonthRepository.deleteByAfmCalendarId(id);
        calendarRepository.deleteById(id);
    }

    @Override
    @Transactional
    public AfmCalendarMonthDto addCalendarMonth(Long calendarId, AfmCalendarMonthDto monthDto) {
        findCalendar(calendarId);
        AfmCalendarMonth month = toMonthEntity(monthDto);
        month.setId(null);
        month.setAfmCalendarId(calendarId);
        month.setCreatedApp(AppConstants.APP_NAME);
        AfmCalendarMonth saved = calendarMonthRepository.save(month);
        return toMonthDto(saved);
    }

    @Override
    @Transactional
    public AfmCalendarMonthDto updateCalendarMonth(Long calendarId, Long monthId, AfmCalendarMonthDto monthDto) {
        findCalendar(calendarId);
        AfmCalendarMonth month = calendarMonthRepository.findById(monthId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar Month with id " + monthId + " not found"));

        if (!month.getAfmCalendarId().equals(calendarId)) {
            throw new BadRequestException("Calendar Month " + monthId + " does not belong to Calendar " + calendarId);
        }

        month.setSerialNo(monthDto.getSerialNo());
        month.setCalendarYear(monthDto.getCalendarYear());
        month.setCalendarMonth(monthDto.getCalendarMonth());
        month.setCalendarQuater(monthDto.getCalendarQuater());
        month.setFromDate(monthDto.getFromDate());
        month.setToDate(monthDto.getToDate());
        month.setPeriodName(monthDto.getPeriodName());
        month.setIsAdjustmentPeriod(monthDto.getIsAdjustmentPeriod());
        month.setPeriodStatus(monthDto.getPeriodStatus());
        if (monthDto.getStatus() != null) {
            StatusUtil.validate(monthDto.getStatus());
            month.setStatus(monthDto.getStatus());
        }
        month.setUpdatedApp(AppConstants.APP_NAME);

        return toMonthDto(calendarMonthRepository.save(month));
    }

    @Override
    @Transactional
    public void deleteCalendarMonth(Long calendarId, Long monthId) {
        findCalendar(calendarId);
        AfmCalendarMonth month = calendarMonthRepository.findById(monthId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar Month with id " + monthId + " not found"));

        if (!month.getAfmCalendarId().equals(calendarId)) {
            throw new BadRequestException("Calendar Month " + monthId + " does not belong to Calendar " + calendarId);
        }

        calendarMonthRepository.delete(month);
    }

    private AfmCalendar findCalendar(Long id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accounting Calendar with id " + id + " not found"));
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException("Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }

        BusinessGroup bg = businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Group with id " + bgId + " not found"));
        if (!Integer.valueOf(1).equals(bg.getStatus())) {
            throw new BadRequestException("Business Group is inactive");
        }

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit with id " + buId + " not found"));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Business Unit is inactive");
        }
        if (!Objects.equals(bu.getBgId(), bgId)) {
            throw new BadRequestException("Business Unit " + buId + " does not belong to Business Group " + bgId);
        }
    }

    private void validateCalendarFields(AfmCalendarRequestDto request) {
        if (request.getCalendarYear() == null || request.getCalendarYear().trim().isEmpty()) {
            throw new BadRequestException("Account Year cannot be empty");
        }
        if (request.getCalendarType() == null || request.getCalendarType().trim().isEmpty()) {
            throw new BadRequestException("Calendar Type cannot be empty");
        }
        if (request.getFromDate() == null) {
            throw new BadRequestException("Date (From) cannot be empty");
        }
        if (request.getToDate() == null) {
            throw new BadRequestException("Date (To) cannot be empty");
        }
        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new BadRequestException("Date (To) must be after Date (From)");
        }
    }

    private AfmCalendarResponseDto toResponse(AfmCalendar entity) {
        return toResponseList(List.of(entity)).get(0);
    }

    private List<AfmCalendarResponseDto> toResponseList(List<AfmCalendar> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> bgIds = list.stream().map(AfmCalendar::getBgId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> buIds = list.stream().map(AfmCalendar::getBuId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BusinessGroup> bgMap = businessGroupRepository.findAllById(bgIds).stream()
                .collect(Collectors.toMap(BusinessGroup::getId, Function.identity()));
        Map<Long, BusinessUnit> buMap = businessUnitRepository.findAllById(buIds).stream()
                .collect(Collectors.toMap(BusinessUnit::getId, Function.identity()));

        return list.stream().map(item -> {
            BusinessGroup bg = bgMap.get(item.getBgId());
            BusinessUnit bu = buMap.get(item.getBuId());

            return AfmCalendarResponseDto.builder()
                    .id(item.getId())
                    .bgId(item.getBgId())
                    .groupName(bg != null ? bg.getGroupName() : null)
                    .buId(item.getBuId())
                    .businessUnitName(bu != null ? bu.getBusinessUnit() : null)
                    .calendarYear(item.getCalendarYear())
                    .calendarType(item.getCalendarType())
                    .fromDate(item.getFromDate())
                    .toDate(item.getToDate())
                    .period(item.getPeriod())
                    .status(item.getStatus())
                    .createdUserId(item.getCreatedUserId())
                    .createdEmpId(item.getCreatedEmpId())
                    .createdDatetime(item.getCreatedDatetime())
                    .updatedUserId(item.getUpdatedUserId())
                    .updatedEmpId(item.getUpdatedEmpId())
                    .updatedDatetime(item.getUpdatedDatetime())
                    .createdApp(item.getCreatedApp())
                    .updatedApp(item.getUpdatedApp())
                    .build();
        }).collect(Collectors.toList());
    }

    private AfmCalendarMonth toMonthEntity(AfmCalendarMonthDto dto) {
        AfmCalendarMonth entity = AfmCalendarMonth.builder()
                .id(dto.getId())
                .afmCalendarId(dto.getAfmCalendarId())
                .serialNo(dto.getSerialNo())
                .calendarYear(dto.getCalendarYear())
                .calendarMonth(dto.getCalendarMonth())
                .calendarQuater(dto.getCalendarQuater())
                .fromDate(dto.getFromDate())
                .toDate(dto.getToDate())
                .periodName(dto.getPeriodName())
                .isAdjustmentPeriod(dto.getIsAdjustmentPeriod())
                .periodStatus(dto.getPeriodStatus() != null ? dto.getPeriodStatus() : "Open")
                .build();
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return entity;
    }

    private AfmCalendarMonthDto toMonthDto(AfmCalendarMonth entity) {
        return AfmCalendarMonthDto.builder()
                .id(entity.getId())
                .afmCalendarId(entity.getAfmCalendarId())
                .serialNo(entity.getSerialNo())
                .calendarYear(entity.getCalendarYear())
                .calendarMonth(entity.getCalendarMonth())
                .calendarQuater(entity.getCalendarQuater())
                .fromDate(entity.getFromDate())
                .toDate(entity.getToDate())
                .periodName(entity.getPeriodName())
                .isAdjustmentPeriod(entity.getIsAdjustmentPeriod())
                .periodStatus(entity.getPeriodStatus())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdEmpId(entity.getCreatedEmpId())
                .createdDatetime(entity.getCreatedDatetime())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedDatetime(entity.getUpdatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedApp(entity.getUpdatedApp())
                .build();
    }
}

