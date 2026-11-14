package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INV_ITEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "ITEM_NAME", nullable = false, length = 150)
    private String itemName;

    @Column(name = "ITEM_CODE", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "CATEGORY_ID")
    private Long categoryId;

    @Column(name = "BRAND_ID")
    private Long brandId;

    @Column(name = "UOM_ID")
    private Long uomId;

    @Column(name = "STORE_ID", length = 500)
    private String storeId;

    @Column(name = "ITEM_IMAGE", columnDefinition = "LONGTEXT")
    private String itemImage;

    @Column(name = "IS_WARRANTY_APPLICABLE", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isWarrantyApplicable;

    @Column(name = "WARRANTY_DAY")
    private Integer warrantyDay;

    @Column(name = "WARRANTY_CRITERIA", length = 1000)
    private String warrantyCriteria;
}

