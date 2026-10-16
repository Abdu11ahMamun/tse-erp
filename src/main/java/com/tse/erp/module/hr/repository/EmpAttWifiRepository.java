package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmpAttWifi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpAttWifiRepository
        extends JpaRepository<EmpAttWifi, Long> {

    List<EmpAttWifi> findByAttConfigIdOrderByIdAsc(Long attConfigId);

    boolean existsByWifiIdAndStatus(Long wifiId, Integer status);
}