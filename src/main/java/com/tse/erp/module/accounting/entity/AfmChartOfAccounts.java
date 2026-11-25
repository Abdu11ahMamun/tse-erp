package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "afm_chart_of_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmChartOfAccounts extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "account_head_name", nullable = false, length = 150)
    private String accountHeadName;

    @Column(name = "ledger_head", nullable = false, length = 50)
    private String ledgerHead;

    @Column(name = "account_head_type", nullable = false, length = 50)
    private String accountHeadType;

    @Column(name = "parent_account_head")
    private Long parentAccountHead;

    @Column(name = "parent_account_code", length = 50)
    private String parentAccountCode;

    @Column(name = "sort_order", precision = 10, scale = 2)
    private BigDecimal sortOrder;

    @Column(name = "account_usage", length = 50)
    private String accountUsage;

    @Column(name = "accounts_source", length = 50)
    private String accountsSource;

    @Column(name = "is_cost_center_mandatory")
    private Integer isCostCenterMandatory;

    @Column(name = "is_budget_acc_head")
    private Integer isBudgetAccHead;

    @Column(name = "level")
    private Integer level;
}
