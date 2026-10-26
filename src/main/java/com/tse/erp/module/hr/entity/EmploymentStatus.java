package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_employment_status")
@Getter
@Setter
@NoArgsConstructor
public class EmploymentStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "status_name", nullable = false, length = 50)
    private String statusName;

    @Column(name = "short_name", length = 5)
    private String shortName;

    @Column(name = "sort_order", precision = 10, scale = 2)
    private BigDecimal sortOrder;
}