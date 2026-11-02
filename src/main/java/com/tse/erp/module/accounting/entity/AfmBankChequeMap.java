package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_BANK_CHEQUE_MAP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankChequeMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "BANK_ID", nullable = false)
    private Long bankId;

    @Column(name = "BANK_ACCOUNT_ID", nullable = false)
    private Long bankAccountId;

    @Column(name = "CHEQUE_NO_FROM", nullable = false, length = 50)
    private String chequeNoFrom;

    @Column(name = "CHEQUE_NO_TO", nullable = false, length = 50)
    private String chequeNoTo;

    @Column(name = "CHEQUE_NO", nullable = false, length = 50)
    private String chequeNo;

    @Column(name = "CHEQUE_STATUS", nullable = false, length = 20)
    private String chequeStatus;
}

