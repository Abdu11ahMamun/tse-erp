package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_employee_training")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeTraining extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "training_title", nullable = false, length = 200)
    private String trainingTitle;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "topics_covered", nullable = false, length = 500)
    private String topicsCovered;

    @Column(name = "training_year", nullable = false)
    private Integer trainingYear;

    @Column(name = "institute_name", nullable = false, length = 200)
    private String instituteName;

    @Column(name = "duration", nullable = false, precision = 5, scale = 2)
    private BigDecimal duration;

    @Column(name = "location", nullable = false, length = 150)
    private String location;
}
