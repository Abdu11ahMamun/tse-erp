package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignStoreEmployeesRequestDto {

    private Long bgId;
    private Long buId;
    private Long storeId;
    private List<Long> employeeIds;
    private String employeeId;
    private Integer status;
}

