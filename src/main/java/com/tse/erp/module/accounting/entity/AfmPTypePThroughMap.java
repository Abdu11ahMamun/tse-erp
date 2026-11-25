package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_p_type_p_through_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPTypePThroughMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "payment_type_id", nullable = false)
    private Long paymentTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payment_type_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_PT_PTH_MAP_PAYMENT_TYPE")
    )
    private AfmLookupValue paymentType;

    @Column(name = "payment_through_id", nullable = false)
    private Long paymentThroughId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payment_through_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_AFM_PT_PTH_MAP_PAYMENT_THROUGH")
    )
    private AfmLookupValue paymentThrough;
}
