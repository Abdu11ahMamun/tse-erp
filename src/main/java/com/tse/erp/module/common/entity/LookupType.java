package com.tse.erp.module.common.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "common_lookup_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

    @Column(name = "type_code", length = 10)
    private String typeCode;
}