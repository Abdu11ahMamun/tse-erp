package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_P_TYPE_P_THROUGH_MAP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPTypePThroughMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "PAYMENT_TYPE_ID", nullable = false)
    private Long paymentTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "PAYMENT_TYPE_ID",
            referencedColumnName = "ID",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_PT_PTH_MAP_PAYMENT_TYPE")
    )
    private AfmLookupValue paymentType;

    @Column(name = "PAYMENT_THROUGH_ID", nullable = false)
    private Long paymentThroughId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "PAYMENT_THROUGH_ID",
            referencedColumnName = "ID",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_PT_PTH_MAP_PAYMENT_THROUGH")
    )
    private AfmLookupValue paymentThrough;
}
