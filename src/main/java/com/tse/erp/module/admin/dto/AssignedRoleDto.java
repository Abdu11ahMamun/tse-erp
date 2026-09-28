package com.tse.erp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AssignedRoleDto {
    private Long id;         // mapping id (for remove)
    private Long roleId;
    private String roleName;
}