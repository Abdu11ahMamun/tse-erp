package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.BuRoleMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuRoleMapRepository
        extends JpaRepository<BuRoleMap, Long> {

    List<BuRoleMap> findByBuIdOrderByIdDesc(Long buId);

    Optional<BuRoleMap> findByRoleIdAndBuId(Long roleId, Long buId);

    List<BuRoleMap> findAllByOrderByIdDesc();
}