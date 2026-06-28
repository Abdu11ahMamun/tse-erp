package com.tse.erp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AssignedPermissionGroupDto {
    private Long id;           // role_detail id
    private Long moduleId;
    private String moduleName;
    private Long menuId;
    private String menuName;
    private List<PermissionDto> permissions;
}