package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_leave_allocation")
@Getter
@Setter
@NoArgsConstructor
public class LeaveAllocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "emp_type_id", nullable = false)
    private Long empTypeId;

    @Column(name = "leave_category_id", nullable = false)
    private Long leaveCategoryId;

    @Column(name = "total_leave_days", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal totalLeaveDays;

    @Column(name = "leave_year", nullable = false)
    private Integer leaveYear;

    @Column(name = "sandwich_leave_applicable", nullable = false,
            columnDefinition = "TINYINT")
    private Integer sandwichLeaveApplicable;

    @Column(name = "with_pay", nullable = false,
            columnDefinition = "TINYINT")
    private Integer withPay;
}
