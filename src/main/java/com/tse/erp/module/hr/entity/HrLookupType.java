package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hr_lookup_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrLookupType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

    @Column(name = "code", length = 10)
    private String code;
}
