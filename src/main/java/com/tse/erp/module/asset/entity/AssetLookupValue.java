package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "asset_lookup_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetLookupValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "type_name_id", nullable = false)
    private Long typeId;

    @Column(name = "value", nullable = false, length = 150)
    private String value;

    @Column(name = "type_code", length = 50)
    private String typeCode;

    @Column(name = "value_code", length = 50)
    private String valueCode;

    @Column(name = "note", length = 200)
    private String note;

    @Column(name = "sort_order", precision = 12, scale = 4)
    private BigDecimal sortOrder;
}
