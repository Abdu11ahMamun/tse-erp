package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "AFM_CALENDAR_MONTH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCalendarMonth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "AFM_CALENDAR_ID", nullable = false)
    private Long afmCalendarId;

    @Column(name = "SERIAL_NO")
    private Integer serialNo;

    @Column(name = "CALENDAR_YEAR", length = 50)
    private String calendarYear;

    @Column(name = "CALENDAR_MONTH", length = 50)
    private String calendarMonth;

    @Column(name = "CALENDAR_QUATER", length = 50)
    private String calendarQuater;

    @Column(name = "FROM_DATE")
    private LocalDate fromDate;

    @Column(name = "TO_DATE")
    private LocalDate toDate;

    @Column(name = "PERIOD_NAME", length = 100)
    private String periodName;

    @Column(name = "IS_ADJUSTMENT_PERIOD", length = 20)
    private String isAdjustmentPeriod;

    @Column(name = "PERIOD_STATUS", length = 20)
    private String periodStatus;
}

