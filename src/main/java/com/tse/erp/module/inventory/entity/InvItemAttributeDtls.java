package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_item_attribute_dtls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemAttributeDtls extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;

    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "opening_stock", precision = 18, scale = 2)
    private BigDecimal openingStock;
}
