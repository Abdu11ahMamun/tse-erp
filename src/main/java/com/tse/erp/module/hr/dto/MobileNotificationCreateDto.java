package com.tse.erp.module.hr.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MobileNotificationCreateDto {

    private List<Long> buIds;
    private List<Long> deptIds;
    private List<Long> employeeIds;
    private Long notificationTypeId;
    private String title;
    private String body;
    private Integer status;
}
