package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_lookup_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrLookupValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "type_name_id", nullable = false)
    private Long typeId;

    @Column(name = "value", nullable = false, length = 20)
    private String value;

    @Column(name = "sort_order", precision = 10, scale = 2)
    private BigDecimal sortOrder;
}
