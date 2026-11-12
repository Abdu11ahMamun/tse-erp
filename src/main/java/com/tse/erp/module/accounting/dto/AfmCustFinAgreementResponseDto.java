package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCustFinAgreementResponseDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private Long customerId;

    private String paymentTerms;
    private String currencyCode;
    private BigDecimal creditLowerLimit;
    private BigDecimal creditUpperLimit;
    private Integer creditDays;
    private BigDecimal openingBalance;
    private String balanceType;
    private LocalDate agreementDateFrom;
    private LocalDate agreementDateTo;
    private String financeStatus;

    private Long createdUserId;
    private String createdUserName;
    private Long createdEmpId;
    private LocalDateTime createdDatetime;
    private Long updatedUserId;
    private Long updatedEmpId;
    private LocalDateTime updatedDatetime;
    private String createdApp;
    private String updatedApp;
    private Integer status;
}

