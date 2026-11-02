package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_V_TYPE_P_TYPE_MAP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVTypePTypeMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "VOUCHER_TYPE_ID", nullable = false)
    private Long voucherTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "VOUCHER_TYPE_ID",
            referencedColumnName = "ID",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_VT_PT_MAP_VOUCHER_TYPE")
    )
    private AfmLookupValue voucherType;

    @Column(name = "PAYMENT_TYPE_ID", nullable = false)
    private Long paymentTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "PAYMENT_TYPE_ID",
            referencedColumnName = "ID",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_VT_PT_MAP_PAYMENT_TYPE")
    )
    private AfmLookupValue paymentType;
}