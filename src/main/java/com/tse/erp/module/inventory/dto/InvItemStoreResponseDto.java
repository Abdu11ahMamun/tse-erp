package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemStoreResponseDto {

    private Long id;
    private Long bgId;
    private String groupName;

    private Long buId;
    private String businessUnitName;

    private String storeName;
    private String storeCode;
    private Integer isDefault;
    private Integer status;

    private List<InvItemStoreEmployeeMapDto> assignedEmployees;

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
}

