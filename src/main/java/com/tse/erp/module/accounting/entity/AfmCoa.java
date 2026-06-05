package com.tse.erp.module.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "afm_coas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCoa {

    // =========================================
    // IDENTITY
    // =========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sl_no")
    private Integer slNo;

    @Column(name = "company_id")
    private Integer companyId;

    // =========================================
    // ACCOUNT INFO
    // =========================================

    @Column(name = "account_code")
    private String accountCode;

    @Column(name = "account_head")
    private String accountHead;

    @Column(name = "ledger_head_type")
    private String ledgerHeadType;

    @Column(name = "account_type")
    private String accountType;

    // =========================================
    // HIERARCHY — Self Reference
    // =========================================

    @Column(name = "parent_account_head_id")
    private Integer parentAccountHeadId;

    // =========================================
    // FEATURES
    // =========================================

    @Column(name = "account_usage")
    private String accountUsage;

    @Column(name = "is_cost_center_mandatory")
    private Integer isCostCenterMandatory;

    @Column(name = "is_budget_head")
    private Integer isBudgetHead;

    // =========================================
    // BALANCE
    // =========================================

    @Column(name = "opening_date")
    private LocalDate openingDate;

    @Column(name = "opening_balance")
    private Double openingBalance;

    @Column(name = "balance_dr")
    private Double balanceDr;

    @Column(name = "balance_cr")
    private Double balanceCr;

    @Column(name = "current_dr")
    private Double currentDr;

    @Column(name = "current_cr")
    private Double currentCr;

    // =========================================
    // AUDIT
    // =========================================

    @Column(name = "status", columnDefinition = "TINYINT")
    private Integer status;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}