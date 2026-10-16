package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "hr_office_calender_dtls")
@Getter
@Setter
@NoArgsConstructor
public class OfficeCalendarDtl extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hr_office_calender_id", nullable = false)
    private Long officeCalendarId;

    @Column(name = "calender_date", nullable = false)
    private LocalDate calendarDate;

    @Column(name = "calender_day", nullable = false, length = 20)
    private String calendarDay;

    @Column(name = "event_type", nullable = false)
    private Long eventTypeId;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
