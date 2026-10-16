package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "hr_emp_att_config")
@Getter
@Setter
@NoArgsConstructor
public class EmpAttConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    // Comma separated: WIFI,GPS,MACHINE
    @Column(name = "attendance_methods", nullable = false, length = 50)
    private String attendanceMethods;

    @Column(name = "wifi_flexible", nullable = false, columnDefinition = "TINYINT")
    private Integer wifiFlexible;

    @Column(name = "wifi_in_flexible", nullable = false, columnDefinition = "TINYINT")
    private Integer wifiInFlexible;

    @Column(name = "wifi_out_flexible", nullable = false, columnDefinition = "TINYINT")
    private Integer wifiOutFlexible;

    @Column(name = "gps_flexible", nullable = false, columnDefinition = "TINYINT")
    private Integer gpsFlexible;

    @Column(name = "gps_in_flexible", nullable = false, columnDefinition = "TINYINT")
    private Integer gpsInFlexible;

    @Column(name = "gps_out_flexible", nullable = false, columnDefinition = "TINYINT")
    private Integer gpsOutFlexible;

    @Column(name = "gps_tracking", nullable = false, columnDefinition = "TINYINT")
    private Integer gpsTracking;

    @Column(name = "tracking_from_time")
    private LocalTime trackingFromTime;

    @Column(name = "tracking_to_time")
    private LocalTime trackingToTime;

    @Column(name = "shift_mode", nullable = false, length = 20)
    private String shiftMode;
}