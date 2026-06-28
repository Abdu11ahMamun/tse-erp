package com.tse.erp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RoleDetailResponseDto {
    private Long roleId;
    private String roleName;
    private Boolean status;
    private String createdAt;

    private int assignedMenuCount;
    private int assignedPermissionCount;

    private List<AssignedPermissionGroupDto> assignedPermissions;
}