package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.entity.RoleDetail;
import java.util.List;

public interface RoleDetailService {

    List<RoleDetail> getAllRoleDetails();

    List<RoleDetail> getRoleDetailsByRoleId(Long roleId);

    RoleDetail getRoleDetailById(Long id);

    RoleDetail createRoleDetail(RoleDetail roleDetail);

    RoleDetail updateRoleDetail(Long id, RoleDetail roleDetail);

    void deleteRoleDetail(Long id);
}