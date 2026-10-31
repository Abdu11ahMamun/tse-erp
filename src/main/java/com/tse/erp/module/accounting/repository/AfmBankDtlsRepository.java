package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmBankDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmBankDtlsRepository extends JpaRepository<AfmBankDtls, Long> {

    List<AfmBankDtls> findByAfmBankIdOrderByIdAsc(Long afmBankId);

    List<AfmBankDtls> findByAfmBankIdAndStatusOrderByIdAsc(Long afmBankId, Integer status);

    List<AfmBankDtls> findByBuIdOrderByIdDesc(Long buId);

    List<AfmBankDtls> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmBankDtls> findByBuIdAndAccountNumber(Long buId, String accountNumber);

    boolean existsByAfmBankId(Long afmBankId);

    void deleteByAfmBankId(Long afmBankId);
}
