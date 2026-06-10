package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Same module e same permission name duplicate check
    List<Permission> findByPermissionNameIgnoreCaseAndModuleId(
            String permissionName, Long moduleId);

    // Module id diye sob permission
    List<Permission> findByModuleId(Long moduleId);
}