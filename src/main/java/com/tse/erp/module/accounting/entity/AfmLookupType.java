package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_LOOKUP_TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmLookupType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "TYPE_NAME", nullable = false, length = 150)
    private String typeName;

    @Column(name = "TYPE_CODE", length = 50)
    private String typeCode;
}
