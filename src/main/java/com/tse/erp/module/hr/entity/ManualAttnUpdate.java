package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "hr_manual_attn_update")
@Getter
@Setter
@NoArgsConstructor
public class ManualAttnUpdate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "desig_id", nullable = false)
    private Long desigId;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "atten_date", nullable = false)
    private LocalDate attenDate;

    @Column(name = "in_time", nullable = false)
    private LocalTime inTime;

    @Column(name = "out_time")
    private LocalTime outTime;

    @Column(name = "note", length = 500)
    private String note;
}