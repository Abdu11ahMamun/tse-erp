package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "AFM_CUST_FIN_AGREEMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCustFinAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Long customerId;

    @Column(name = "PAYMENT_TERMS", length = 50)
    private String paymentTerms;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CREDIT_LOWER_LIMIT", precision = 18, scale = 2)
    private BigDecimal creditLowerLimit;

    @Column(name = "CREDIT_UPPER_LIMIT", precision = 18, scale = 2)
    private BigDecimal creditUpperLimit;

    @Column(name = "CREDIT_DAYS")
    private Integer creditDays;

    @Column(name = "OPENING_BALANCE", precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "BALANCE_TYPE", length = 20)
    private String balanceType;

    @Column(name = "AGREEMENT_DATE_FROM")
    private LocalDate agreementDateFrom;

    @Column(name = "AGREEMENT_DATE_TO")
    private LocalDate agreementDateTo;

    @Column(name = "FINANCE_STATUS", length = 20)
    private String financeStatus;
}

