package com.tse.erp.module.hr.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LeaveCalcDto {

    private BigDecimal totalLeaveDays;
    private BigDecimal remainingLeaveDays;
    private Integer sandwichLeaveApplicable;
}
