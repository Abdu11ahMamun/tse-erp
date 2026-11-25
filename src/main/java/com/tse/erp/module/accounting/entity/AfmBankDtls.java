package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "afm_bank_dtls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankDtls extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "afm_bank_id", nullable = false)
    private Long afmBankId;

    @Column(name = "branch_name", length = 150)
    private String branchName;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "account_type", length = 50)
    private String accountType;

    @Column(name = "account_name", length = 150)
    private String accountName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "opening_date")
    private LocalDate openingDate;

    @Column(name = "opening_balance", precision = 18, scale = 2)
    private BigDecimal openingBalance;
}
