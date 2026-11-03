package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPeriodRequestDto {

    private Long bgId;
    private Long buId;
    private String periodYear;
    private String periodName;
    private String periodStatus;
    private Long afmCalendarMonthId;
    private Integer status;
}
