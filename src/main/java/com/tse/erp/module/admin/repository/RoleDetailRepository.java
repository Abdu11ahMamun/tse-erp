package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.RoleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleDetailRepository extends JpaRepository<RoleDetail, Long> {

    // Role id diye sob details
    List<RoleDetail> findByRoleId(Long roleId);

    // Duplicate check — same role + module + menu
    List<RoleDetail> findByRoleIdAndModuleIdAndMenuId(
            Long roleId, Long moduleId, Long menuId);

    List<RoleDetail> findAllByOrderByIdDesc();
}