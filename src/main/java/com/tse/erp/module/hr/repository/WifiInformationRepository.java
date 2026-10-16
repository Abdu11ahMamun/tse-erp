package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.WifiInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WifiInformationRepository
        extends JpaRepository<WifiInformation, Long> {

    List<WifiInformation> findAllByOrderByIdDesc();

    List<WifiInformation> findByBuIdOrderByIdDesc(Long buId);

    List<WifiInformation> findByMacAddressIgnoreCase(String macAddress);
}
