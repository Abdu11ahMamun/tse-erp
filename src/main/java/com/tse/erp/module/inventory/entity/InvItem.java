package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inv_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "uom_id")
    private Long uomId;

    @Column(name = "store_id", length = 500)
    private String storeId;

    @Column(name = "item_image", columnDefinition = "LONGTEXT")
    private String itemImage;

    @Column(name = "is_warranty_applicable", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isWarrantyApplicable;

    @Column(name = "warranty_day")
    private Integer warrantyDay;

    @Column(name = "warranty_criteria", length = 1000)
    private String warrantyCriteria;
}
