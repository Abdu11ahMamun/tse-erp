package com.tse.erp.module.hr.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LeaveBalanceDto {

    private Long leaveCategoryId;
    private String leaveCategoryName;
    private BigDecimal totalLeaveDays;
    private BigDecimal usedLeaveDays;
    private BigDecimal remainingLeaveDays;
    private Integer sandwichLeaveApplicable;
    private Integer withPay;
}
