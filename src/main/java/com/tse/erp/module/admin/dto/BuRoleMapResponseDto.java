package com.tse.erp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BuRoleMapResponseDto {
    private Long buId;
    private String businessUnitName;
    private List<AssignedRoleDto> assignedRoles;
}