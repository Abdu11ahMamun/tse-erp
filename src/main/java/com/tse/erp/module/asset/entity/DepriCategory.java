package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "asset_depri_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepriCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "depri_category", nullable = false, length = 100)
    private String depriCategory;

    @Column(name = "depri_method_id", nullable = false)
    private Long depriMethodId;

    @Column(name = "asset_type_id", nullable = false)
    private Long assetTypeId;

    @Column(name = "depri_life", nullable = false)
    private Integer depriLife;

    @Column(name = "depri_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal depriPercentage;

    @Column(name = "asset_owner_id", nullable = false)
    private Long assetOwnerId;
}
