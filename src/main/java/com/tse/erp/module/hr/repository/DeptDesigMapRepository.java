package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.DeptDesigMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeptDesigMapRepository
        extends JpaRepository<DeptDesigMap, Long> {

    List<DeptDesigMap> findAllByOrderByIdDesc();

    List<DeptDesigMap> findByBuIdOrderByIdDesc(Long buId);

    List<DeptDesigMap> findByBuIdAndDeptIdAndDesigId(
            Long buId, Long deptId, Long desigId);

    boolean existsByDeptId(Long deptId);

    boolean existsByDesigId(Long desigId);
}
