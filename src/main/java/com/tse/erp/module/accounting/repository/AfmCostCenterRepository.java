package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmCostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmCostCenterRepository extends JpaRepository<AfmCostCenter, Long> {

    List<AfmCostCenter> findAllByOrderByIdDesc();

    List<AfmCostCenter> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmCostCenter> findByBuIdOrderByIdDesc(Long buId);

    List<AfmCostCenter> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmCostCenter> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmCostCenter> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmCostCenter> findByBuIdAndProjectIdOrderByIdDesc(Long buId, Long projectId);

    List<AfmCostCenter> findByBuIdAndDepartmentIdOrderByIdDesc(Long buId, Long departmentId);

    List<AfmCostCenter> findByBuIdAndCostCenterIgnoreCase(Long buId, String costCenter);

    List<AfmCostCenter> findByBuIdAndShortNameIgnoreCase(Long buId, String shortName);

    boolean existsByBuIdAndCostCenterIgnoreCase(Long buId, String costCenter);
}

