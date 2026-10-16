package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hr_office_calender")
@Getter
@Setter
@NoArgsConstructor
public class OfficeCalendar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month_id", nullable = false)
    private Long monthId;
}
