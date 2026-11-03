package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "AFM_CALENDAR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCalendar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "CALENDAR_YEAR", nullable = false, length = 50)
    private String calendarYear;

    @Column(name = "CALENDAR_TYPE", nullable = false, length = 50)
    private String calendarType;

    @Column(name = "FROM_DATE", nullable = false)
    private LocalDate fromDate;

    @Column(name = "TO_DATE", nullable = false)
    private LocalDate toDate;

    @Column(name = "PERIOD", length = 50)
    private String period;
}

