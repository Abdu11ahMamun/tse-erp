package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmPeriodRepository extends JpaRepository<AfmPeriod, Long> {

    List<AfmPeriod> findAllByOrderByIdDesc();

    List<AfmPeriod> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmPeriod> findByBuIdOrderByIdDesc(Long buId);

    List<AfmPeriod> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmPeriod> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmPeriod> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmPeriod> findByBuIdAndPeriodYearOrderByIdDesc(Long buId, String periodYear);

    List<AfmPeriod> findByBuIdAndPeriodYearAndPeriodStatusOrderByIdDesc(Long buId, String periodYear, String periodStatus);

    List<AfmPeriod> findByBuIdAndPeriodYearAndPeriodNameIgnoreCase(Long buId, String periodYear, String periodName);

    boolean existsByBuIdAndPeriodYearAndPeriodNameIgnoreCase(Long buId, String periodYear, String periodName);
}

