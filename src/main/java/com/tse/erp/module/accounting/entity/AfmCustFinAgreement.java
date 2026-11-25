package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "afm_cust_fin_agreement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCustFinAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "currency_code", length = 20)
    private String currencyCode;

    @Column(name = "credit_lower_limit", precision = 18, scale = 2)
    private BigDecimal creditLowerLimit;

    @Column(name = "credit_upper_limit", precision = 18, scale = 2)
    private BigDecimal creditUpperLimit;

    @Column(name = "credit_days")
    private Integer creditDays;

    @Column(name = "opening_balance", precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "balance_type", length = 20)
    private String balanceType;

    @Column(name = "agreement_date_from")
    private LocalDate agreementDateFrom;

    @Column(name = "agreement_date_to")
    private LocalDate agreementDateTo;

    @Column(name = "finance_status", length = 20)
    private String financeStatus;
}

