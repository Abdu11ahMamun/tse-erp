package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemWarehouseResponseDto {

    private Long id;
    private Long bgId;
    private String groupName;

    private Long buId;
    private String businessUnitName;

    private String warehouseName;
    private String warehouseCode;
    private Integer isDefault;

    private String storeId;
    private List<Long> storeIds;
    private String storeName;
    private List<String> storeNames;
    private String storeCode;

    private Integer status;

    private Long createdUserId;
    private String createdUserName;
    private Long createdEmpId;
    private String createdEmpName;
    private LocalDateTime createdDatetime;
    private String createdApp;

    private Long updatedUserId;
    private String updatedUserName;
    private Long updatedEmpId;
    private String updatedEmpName;
    private LocalDateTime updatedDatetime;
    private String updatedApp;

    private List<InvItemWarehouseEmployeeMapDto> assignedEmployees;
}

