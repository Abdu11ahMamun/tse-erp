package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "item_group_id", nullable = false)
    private Long itemGroupId;

    @Column(name = "asset_depri_cat_id", nullable = false)
    private Long assetDepriCatId;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "item_piece_or_set", length = 20)
    private String itemPieceOrSet;

    @Transient
    private java.util.List<AssetItemSet> itemSets;
}
