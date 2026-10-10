package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "hr_employee_job_experience")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeJobExperience extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "company_business", nullable = false, length = 200)
    private String companyBusiness;

    @Column(name = "designation", nullable = false, length = 150)
    private String designation;

    @Column(name = "department", nullable = false, length = 200)
    private String department;

    @Column(name = "employment_period_start_dt", nullable = false)
    private LocalDate employmentPeriodStartDt;

    @Column(name = "employment_period_end_dt")
    private LocalDate employmentPeriodEndDt;

    @Column(name = "currently_working", nullable = false)
    private Integer currentlyWorking;

    @Column(name = "responsibilities", length = 500)
    private String responsibilities;
}
