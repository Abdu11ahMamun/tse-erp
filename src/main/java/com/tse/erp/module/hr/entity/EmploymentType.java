package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_employment_type")
@Getter
@Setter
@NoArgsConstructor
public class EmploymentType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id")
    private Long bgId;

    // ET / WS / PS / ES
    @Column(name = "category", length = 2)
    private String category;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(name = "sort_order", precision = 10, scale = 2)
    private BigDecimal sortOrder;

    // Boolean wrapper: JSON key "isDateFieldRequired" thik thake
    @Column(name = "is_date_field_required", nullable = false,
            columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean isDateFieldRequired;

    @Column(name = "date_field_label", length = 50)
    private String dateFieldLabel;
}