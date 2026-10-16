package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "hr_shift")
@Getter
@Setter
@NoArgsConstructor
public class Shift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "shift_title", nullable = false, length = 100)
    private String shiftTitle;

    @Column(name = "shift_start_time", nullable = false)
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time", nullable = false)
    private LocalTime shiftEndTime;

    @Column(name = "apply_late_in", nullable = false,
            columnDefinition = "TINYINT")
    private Integer applyLateIn;

    @Column(name = "late_in_days")
    private Integer lateInDays;

    @Column(name = "late_in_deduction", precision = 5, scale = 2)
    private BigDecimal lateInDeduction;

    @Column(name = "apply_early_out", nullable = false,
            columnDefinition = "TINYINT")
    private Integer applyEarlyOut;

    @Column(name = "early_out_days")
    private Integer earlyOutDays;

    @Column(name = "early_out_deduction", precision = 5, scale = 2)
    private BigDecimal earlyOutDeduction;

    @Column(name = "weekend_days", nullable = false, length = 50)
    private String weekendDays;

    @Column(name = "grace_time_minutes", nullable = false)
    private Integer graceTimeMinutes;

    @Column(name = "late_in_message", length = 500)
    private String lateInMessage;

    @Column(name = "early_out_message", length = 500)
    private String earlyOutMessage;

    @Column(name = "allow_late_in_msg", nullable = false,
            columnDefinition = "TINYINT")
    private Integer allowLateInMsg;

    @Column(name = "allow_early_out_msg", nullable = false,
            columnDefinition = "TINYINT")
    private Integer allowEarlyOutMsg;

    @Column(name = "attn_in_out_policy", nullable = false,
            columnDefinition = "TINYINT")
    private Integer attnInOutPolicy;

    @Column(name = "start_before")
    private Integer startBefore;

    @Column(name = "close_after")
    private Integer closeAfter;

    @Column(name = "not_allow_attn", nullable = false,
            columnDefinition = "TINYINT")
    private Integer notAllowAttn;

    @Column(name = "in_time_start")
    private LocalTime inTimeStart;

    @Column(name = "in_time_close")
    private LocalTime inTimeClose;

    @Column(name = "out_time_start")
    private LocalTime outTimeStart;

    @Column(name = "out_time_close")
    private LocalTime outTimeClose;

    @Column(name = "allow_next_day_attn", nullable = false,
            columnDefinition = "TINYINT")
    private Integer allowNextDayAttn;

    @Column(name = "allow_overtime", nullable = false,
            columnDefinition = "TINYINT")
    private Integer allowOvertime;

    @Column(name = "max_ot_hour", precision = 5, scale = 2)
    private BigDecimal maxOtHour;

    @Column(name = "extra_ot_hour", precision = 5, scale = 2)
    private BigDecimal extraOtHour;
}
