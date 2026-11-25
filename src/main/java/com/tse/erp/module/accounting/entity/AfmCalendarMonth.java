package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "afm_calendar_month")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCalendarMonth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "afm_calendar_id", nullable = false)
    private Long afmCalendarId;

    @Column(name = "serial_no")
    private Integer serialNo;

    @Column(name = "calendar_year", length = 50)
    private String calendarYear;

    @Column(name = "calendar_month", length = 50)
    private String calendarMonth;

    @Column(name = "calendar_quater", length = 50)
    private String calendarQuater;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "period_name", length = 100)
    private String periodName;

    @Column(name = "is_adjustment_period", length = 20)
    private String isAdjustmentPeriod;

    @Column(name = "period_status", length = 20)
    private String periodStatus;
}

