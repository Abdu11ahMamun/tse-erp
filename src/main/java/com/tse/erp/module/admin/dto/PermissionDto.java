package com.tse.erp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PermissionDto {
    private Long id;
    private String permissionName;
}