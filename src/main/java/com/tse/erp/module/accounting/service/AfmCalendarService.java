package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmCalendarMonthDto;
import com.tse.erp.module.accounting.dto.AfmCalendarRequestDto;
import com.tse.erp.module.accounting.dto.AfmCalendarResponseDto;

import java.util.List;

public interface AfmCalendarService {

    List<AfmCalendarResponseDto> getAllCalendars(Long bgId, Long buId, Integer status);

    AfmCalendarResponseDto getCalendarById(Long id);

    List<AfmCalendarMonthDto> getCalendarMonths(Long calendarId);

    AfmCalendarResponseDto createCalendar(AfmCalendarRequestDto request);

    AfmCalendarResponseDto updateCalendar(Long id, AfmCalendarRequestDto request);

    void deleteCalendar(Long id);

    AfmCalendarMonthDto addCalendarMonth(Long calendarId, AfmCalendarMonthDto monthDto);

    AfmCalendarMonthDto updateCalendarMonth(Long calendarId, Long monthId, AfmCalendarMonthDto monthDto);

    void deleteCalendarMonth(Long calendarId, Long monthId);
}

