package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignWarehouseEmployeesRequestDto {

    private Long bgId;
    private Long buId;
    private Long warehouseId;
    private List<Long> employeeIds;
    private String employeeId;
    private Integer status;
}
