package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_USER_SETUP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserSetup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "VOUCHER_DATE", length = 250)
    private String voucherDate;

    @Column(name = "VOUCHER_TYPE", length = 500)
    private String voucherType;

    @Column(name = "VOUCHER_TYPE_DEFAULT", length = 100)
    private String voucherTypeDefault;

    @Column(name = "LOCATION", length = 500)
    private String location;

    @Column(name = "LOCATION_DEFAULT", length = 100)
    private String locationDefault;

    @Column(name = "BANK_ACCOUNT", length = 500)
    private String bankAccount;

    @Column(name = "BANK_ACCOUNT_DEFAULT", length = 100)
    private String bankAccountDefault;

    @Column(name = "COST_CENTER", length = 500)
    private String costCenter;

    @Column(name = "COST_CENTER_DEFAULT", length = 100)
    private String costCenterDefault;
}

