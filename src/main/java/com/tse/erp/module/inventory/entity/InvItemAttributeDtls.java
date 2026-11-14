package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "INV_ITEM_ATTRIBUTE_DTLS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemAttributeDtls extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "ITEM_ID", nullable = false)
    private Long itemId;

    @Column(name = "ATTRIBUTE_ID", nullable = false)
    private Long attributeId;

    @Column(name = "PRICE", precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "OPENING_STOCK", precision = 18, scale = 2)
    private BigDecimal openingStock;
}

