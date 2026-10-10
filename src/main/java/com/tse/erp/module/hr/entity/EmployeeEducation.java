package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_employee_education")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEducation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "level_of_edu_id", nullable = false)
    private Long levelOfEduId;

    @Column(name = "degree_id", nullable = false)
    private Long degreeId;

    @Column(name = "major_group", length = 150)
    private String majorGroup;

    @Column(name = "institute_name", nullable = false, length = 200)
    private String instituteName;

    @Column(name = "result", nullable = false, length = 50)
    private String result;

    @Column(name = "cgpa_marks", precision = 5, scale = 2)
    private BigDecimal cgpaMarks;

    @Column(name = "scale_marks", precision = 5, scale = 2)
    private BigDecimal scaleMarks;

    @Column(name = "year_of_passing", nullable = false)
    private Integer yearOfPassing;

    @Column(name = "duration", precision = 4, scale = 2)
    private BigDecimal duration;
}
