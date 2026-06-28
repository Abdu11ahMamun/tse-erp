package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.RoleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleDetailRepository
        extends JpaRepository<RoleDetail, Long> {

    List<RoleDetail> findByRoleId(Long roleId);

    List<RoleDetail> findByRoleIdAndModuleIdAndMenuId(
            Long roleId, Long moduleId, Long menuId);

    // ✅ Single result — merge logic er jonno
    Optional<RoleDetail> findFirstByRoleIdAndModuleIdAndMenuId(
            Long roleId, Long moduleId, Long menuId);

    List<RoleDetail> findAllByOrderByIdDesc();
}