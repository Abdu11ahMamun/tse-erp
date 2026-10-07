package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_item_spec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetItemSpec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "item_name_id", nullable = false)
    private Long itemNameId;

    @Column(name = "spec_name", nullable = false, length = 20)
    private String specName;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
