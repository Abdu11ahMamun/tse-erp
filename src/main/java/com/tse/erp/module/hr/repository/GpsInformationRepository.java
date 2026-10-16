package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.GpsInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpsInformationRepository
        extends JpaRepository<GpsInformation, Long> {

    List<GpsInformation> findAllByOrderByIdDesc();

    List<GpsInformation> findByBuIdOrderByIdDesc(Long buId);

    List<GpsInformation> findByBuIdAndLocationNameIgnoreCase(
            Long buId, String locationName);
}
