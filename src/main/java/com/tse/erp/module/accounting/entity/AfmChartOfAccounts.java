package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "AFM_CHART_OF_ACCOUNTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmChartOfAccounts extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "ACCOUNT_CODE", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "ACCOUNT_HEAD_NAME", nullable = false, length = 150)
    private String accountHeadName;

    @Column(name = "LEDGER_HEAD", nullable = false, length = 50)
    private String ledgerHead;

    @Column(name = "ACCOUNT_HEAD_TYPE", nullable = false, length = 50)
    private String accountHeadType;

    @Column(name = "PARENT_ACCOUNT_HEAD")
    private Long parentAccountHead;

    @Column(name = "PARENT_ACCOUNT_CODE", length = 50)
    private String parentAccountCode;

    @Column(name = "SORT_ORDER", precision = 10, scale = 2)
    private BigDecimal sortOrder;

    @Column(name = "ACCOUNT_USAGE", length = 50)
    private String accountUsage;

    @Column(name = "ACCOUNTS_SOURCE", length = 50)
    private String accountsSource;

    @Column(name = "IS_COST_CENTER_MANDATORY")
    private Integer isCostCenterMandatory;

    @Column(name = "IS_BUDGET_ACC_HEAD")
    private Integer isBudgetAccHead;

    @Column(name = "LEVEL")
    private Integer level;
}
