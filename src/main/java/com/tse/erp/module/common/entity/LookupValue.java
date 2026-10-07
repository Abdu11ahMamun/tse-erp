package com.tse.erp.module.common.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "common_lookup_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "type_name_id", nullable = false)
    private Long typeId;

    @Column(name = "value", nullable = false, length = 20)
    private String value;

    @Column(name = "sort_order")
    private Integer sortOrder;
}