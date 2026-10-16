package com.tse.erp.module.hr.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

// Request AND response
@Getter
@Setter
@NoArgsConstructor
public class EmpAttConfigDto {

    private Long id;

    private Long buId;
    private Long employeeId;

    // Response only (Employee Master theke)
    private String employeeName;
    private String employeeCode;
    private Long empTypeId;
    private Long deptId;
    private Long desigId;

    private List<String> attendanceMethods;

    private Integer wifiFlexible;
    private Integer wifiInFlexible;
    private Integer wifiOutFlexible;
    private Integer gpsFlexible;
    private Integer gpsInFlexible;
    private Integer gpsOutFlexible;
    private Integer gpsTracking;
    private LocalTime trackingFromTime;
    private LocalTime trackingToTime;

    private String shiftMode;
    private Integer status;

    // Request: id list
    private List<Long> shiftIds;
    private List<Long> wifiIds;
    private List<Long> gpsIds;

    // Response: detail
    private List<ShiftItem> shifts;
    private List<WifiItem> wifis;
    private List<GpsItem> gpsLocations;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ShiftItem {
        private Long shiftId;
        private String shiftTitle;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WifiItem {
        private Long wifiId;
        private String wifiName;
        private String macAddress;
        private Integer status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GpsItem {
        private Long locationId;
        private String locationName;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Integer status;
    }
}