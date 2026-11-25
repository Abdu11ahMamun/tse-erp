package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_bank_cheque_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankChequeMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "bank_id", nullable = false)
    private Long bankId;

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "cheque_no_from", nullable = false, length = 50)
    private String chequeNoFrom;

    @Column(name = "cheque_no_to", nullable = false, length = 50)
    private String chequeNoTo;

    @Column(name = "cheque_no", nullable = false, length = 50)
    private String chequeNo;

    @Column(name = "cheque_status", nullable = false, length = 20)
    private String chequeStatus;
}

