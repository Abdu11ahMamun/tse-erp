package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INV_ITEM_WAREHOUSE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemWarehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "WAREHOUSE_NAME", nullable = false, length = 150)
    private String warehouseName;

    @Column(name = "WAREHOUSE_CODE", nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "IS_DEFAULT", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDefault;

    @Column(name = "STORE_ID", length = 500)
    private String storeId;
}

