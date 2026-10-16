package com.tse.erp.module.hr.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OfficeCalendarDayDto {

    private Long id;
    private LocalDate calendarDate;
    private String calendarDay;
    private Long eventTypeId;
    private String remarks;
    private Integer status;
}
