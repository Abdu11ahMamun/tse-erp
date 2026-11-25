package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_period")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPeriod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "period_year", nullable = false, length = 50)
    private String periodYear;

    @Column(name = "period_name", nullable = false, length = 100)
    private String periodName;

    @Column(name = "period_status", nullable = false, length = 20)
    private String periodStatus;

    @Column(name = "afm_calendar_month_id")
    private Long afmCalendarMonthId;
}
