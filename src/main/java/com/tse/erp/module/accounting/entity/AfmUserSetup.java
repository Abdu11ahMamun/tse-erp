package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_user_setup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserSetup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "voucher_date", length = 250)
    private String voucherDate;

    @Column(name = "voucher_type", length = 500)
    private String voucherType;

    @Column(name = "voucher_type_default", length = 100)
    private String voucherTypeDefault;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "location_default", length = 100)
    private String locationDefault;

    @Column(name = "bank_account", length = 500)
    private String bankAccount;

    @Column(name = "bank_account_default", length = 100)
    private String bankAccountDefault;

    @Column(name = "cost_center", length = 500)
    private String costCenter;

    @Column(name = "cost_center_default", length = 100)
    private String costCenterDefault;

    @Column(name = "is_all_acc_head_allowed", columnDefinition = "TINYINT")
    private Integer isAllAccHeadAllowed;
}

