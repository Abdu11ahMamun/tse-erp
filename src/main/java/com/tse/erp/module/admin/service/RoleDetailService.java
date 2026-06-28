package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.dto.ApiResponse;
import com.tse.erp.module.admin.dto.AssignPermissionRequestDto;
import com.tse.erp.module.admin.dto.RoleDetailResponseDto;
import com.tse.erp.module.admin.entity.RoleDetail;
import com.tse.erp.module.admin.dto.PermissionDto;

import java.util.List;

public interface RoleDetailService {

    List<RoleDetail> getAllRoleDetails();
    List<RoleDetail> getRoleDetailsByRoleId(Long roleId);
    RoleDetail getRoleDetailById(Long id);
    RoleDetail createRoleDetail(RoleDetail roleDetail);
    RoleDetail updateRoleDetail(Long id, RoleDetail roleDetail);
    void deleteRoleDetail(Long id);

    RoleDetailResponseDto getRoleWithGroupedPermissions(Long roleId);

    List<PermissionDto> getAvailablePermissions(
            Long roleId, Long moduleId, Long menuId);

    RoleDetailResponseDto assignPermissions(
            Long roleId, AssignPermissionRequestDto request);

    ApiResponse<RoleDetailResponseDto> removePermissionFromRoleDetail(
            Long roleDetailId, Long permissionId);
}