package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "AFM_VOUCHER_DETAIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID")
    private Long bgId;

    @Column(name = "BU_ID")
    private Long buId;

    @Column(name = "VOUCHER_ID", nullable = false)
    private Long voucherId;

    @Column(name = "COA_ID")
    private Long coaId;

    @Column(name = "COA_SOURCE_NAME", length = 100)
    private String coaSourceName;

    @Column(name = "COA_SOURCE_ID")
    private Long coaSourceId;

    @Column(name = "COST_CENTER_ID")
    private Long costCenterId;

    @Column(name = "DR_AMOUNT", precision = 18, scale = 2)
    private BigDecimal drAmount;

    @Column(name = "CR_AMOUNT", precision = 18, scale = 2)
    private BigDecimal crAmount;
}
