package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.DepartmentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentGroupRepository
        extends JpaRepository<DepartmentGroup, Long> {

    List<DepartmentGroup> findAllByOrderByIdDesc();

    List<DepartmentGroup> findByDeptIdOrderByIdDesc(Long deptId);

    List<DepartmentGroup> findByDeptIdAndBuId(Long deptId, Long buId);

    boolean existsByDeptId(Long deptId);
}
