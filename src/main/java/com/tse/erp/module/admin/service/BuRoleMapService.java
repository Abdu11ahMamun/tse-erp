package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.dto.BuRoleMapResponseDto;

public interface BuRoleMapService {

    BuRoleMapResponseDto getRolesByBusinessUnit(Long buId);

    BuRoleMapResponseDto assignRole(Long buId, Long roleId);

    BuRoleMapResponseDto removeRole(Long buId, Long mappingId);
}