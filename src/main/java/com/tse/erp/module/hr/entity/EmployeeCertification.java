package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_employee_certification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCertification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "certification", nullable = false, length = 200)
    private String certification;

    @Column(name = "institute_name", nullable = false, length = 200)
    private String instituteName;

    @Column(name = "location", nullable = false, length = 150)
    private String location;

    @Column(name = "duration", nullable = false, precision = 5, scale = 2)
    private BigDecimal duration;
}
