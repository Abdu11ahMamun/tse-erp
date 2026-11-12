package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCustFinAgreementRequestDto {

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
}

