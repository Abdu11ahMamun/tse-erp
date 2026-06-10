package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.entity.Permission;
import java.util.List;

public interface PermissionService {

    List<Permission> getAllPermissions();

    List<Permission> getPermissionsByModuleId(Long moduleId);

    Permission getPermissionById(Long id);

    Permission createPermission(Permission permission);

    Permission updatePermission(Long id, Permission permission);

    void deletePermission(Long id);
}