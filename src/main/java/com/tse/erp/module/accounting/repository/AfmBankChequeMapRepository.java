package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmBankChequeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AfmBankChequeMapRepository extends JpaRepository<AfmBankChequeMap, Long> {

    List<AfmBankChequeMap> findAllByOrderByIdDesc();

    List<AfmBankChequeMap> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmBankChequeMap> findByBuIdOrderByIdDesc(Long buId);

    List<AfmBankChequeMap> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmBankChequeMap> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmBankChequeMap> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmBankChequeMap> findByBankAccountIdOrderByIdDesc(Long bankAccountId);

    List<AfmBankChequeMap> findByBankIdAndBankAccountIdOrderByIdDesc(Long bankId, Long bankAccountId);

    boolean existsByBankAccountIdAndChequeNo(Long bankAccountId, String chequeNo);

    @Query("SELECT c.chequeNo FROM AfmBankChequeMap c WHERE c.bankAccountId = :bankAccountId AND c.chequeNo IN :chequeNumbers")
    List<String> findExistingChequeNumbers(@Param("bankAccountId") Long bankAccountId, @Param("chequeNumbers") Collection<String> chequeNumbers);
}

