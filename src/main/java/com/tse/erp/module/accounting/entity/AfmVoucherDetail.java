package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "afm_voucher_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id")
    private Long bgId;

    @Column(name = "bu_id")
    private Long buId;

    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "coa_id")
    private Long coaId;

    @Column(name = "coa_source_type", length = 100)
    private String coaSourceType;

    @Column(name = "coa_source_name", length = 100)
    private String coaSourceName;

    @Column(name = "coa_source_id")
    private Long coaSourceId;

    @Column(name = "cost_center_id")
    private Long costCenterId;

    @Column(name = "dr_amount", precision = 18, scale = 2)
    private BigDecimal drAmount;

    @Column(name = "cr_amount", precision = 18, scale = 2)
    private BigDecimal crAmount;
}
