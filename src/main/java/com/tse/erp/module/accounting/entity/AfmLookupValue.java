package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "AFM_LOOKUP_VALUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmLookupValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "TYPE_NAME_ID", nullable = false)
    private Long typeNameId;

    @Column(name = "TYPE_CODE", length = 50)
    private String typeCode;

    @Column(name = "VALUE", nullable = false, length = 150)
    private String value;

    @Column(name = "VALUE_CODE", length = 50)
    private String valueCode;

    @Column(name = "NOTE", length = 200)
    private String note;

    @Column(name = "SORT_ORDER", precision = 12, scale = 4)
    private BigDecimal sortOrder;
}
