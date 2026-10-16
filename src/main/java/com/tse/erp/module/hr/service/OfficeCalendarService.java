package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.dto.OfficeCalendarDto;

import java.util.List;

public interface OfficeCalendarService {

    List<OfficeCalendarDto> getAllOfficeCalendars(
            Long buId, Integer year);

    OfficeCalendarDto getOfficeCalendarById(Long id);

    List<OfficeCalendarDto> createOfficeCalendars(OfficeCalendarDto dto);

    OfficeCalendarDto updateOfficeCalendar(
            Long id, OfficeCalendarDto dto);

    void deleteOfficeCalendar(Long id);
}
