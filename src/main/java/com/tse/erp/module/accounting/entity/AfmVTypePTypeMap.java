package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_v_type_p_type_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVTypePTypeMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "voucher_type_id", nullable = false)
    private Long voucherTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "voucher_type_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_VT_PT_MAP_VOUCHER_TYPE")
    )
    private AfmLookupValue voucherType;

    @Column(name = "payment_type_id", nullable = false)
    private Long paymentTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payment_type_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_VT_PT_MAP_PAYMENT_TYPE")
    )
    private AfmLookupValue paymentType;

    @Column(name = "bank_cheque_mode", length = 20)
    private String bankChequeMode;
}