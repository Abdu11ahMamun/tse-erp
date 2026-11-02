package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmBankRepository extends JpaRepository<AfmBank, Long> {

    List<AfmBank> findAllByOrderByIdDesc();

    List<AfmBank> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmBank> findByBuIdOrderByIdDesc(Long buId);

    List<AfmBank> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmBank> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmBank> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmBank> findByBuIdAndBankNameIgnoreCase(Long buId, String bankName);

    List<AfmBank> findByBuIdAndShortNameIgnoreCase(Long buId, String shortName);

    boolean existsByBuIdAndBankNameIgnoreCase(Long buId, String bankName);
}
