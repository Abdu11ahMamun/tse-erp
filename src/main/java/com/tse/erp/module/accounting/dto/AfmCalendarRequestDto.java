package com.tse.erp.module.accounting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCalendarRequestDto {

    private Long bgId;
    private Long buId;
    private String calendarYear;
    private String calendarType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private String period;
    private Integer status;

    private List<AfmCalendarMonthDto> calendarMonths;
}

