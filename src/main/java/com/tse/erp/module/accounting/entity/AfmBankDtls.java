package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "AFM_BANK_DTLS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankDtls extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "AFM_BANK_ID", nullable = false)
    private Long afmBankId;

    @Column(name = "BRANCH_NAME", length = 150)
    private String branchName;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "ACCOUNT_TYPE", length = 50)
    private String accountType;

    @Column(name = "ACCOUNT_NAME", length = 150)
    private String accountName;

    @Column(name = "ACCOUNT_NUMBER", length = 50)
    private String accountNumber;

    @Column(name = "OPENING_DATE")
    private LocalDate openingDate;

    @Column(name = "OPENING_BALANCE", precision = 18, scale = 2)
    private BigDecimal openingBalance;
}
