package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hr_emp_leave")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeLeave extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "designation_id", nullable = false)
    private Long designationId;

    @Column(name = "emp_type_id", nullable = false)
    private Long empTypeId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "leave_category_id", nullable = false)
    private Long leaveCategoryId;

    @Column(name = "total_leave_days", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal totalLeaveDays;

    @Column(name = "supporting_document", length = 255)
    private String supportingDocument;

    @Column(name = "reason", length = 1000)
    private String reason;
}
