package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INV_ITEM_STORE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemStore extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "STORE_NAME", nullable = false, length = 150)
    private String storeName;

    @Column(name = "STORE_CODE", nullable = false, length = 50)
    private String storeCode;

    @Column(name = "IS_DEFAULT", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDefault;
}

