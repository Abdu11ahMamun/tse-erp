package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmChartOfAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmChartOfAccountsRepository
        extends JpaRepository<AfmChartOfAccounts, Long> {

    List<AfmChartOfAccounts> findAllByOrderByIdDesc();

    List<AfmChartOfAccounts> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmChartOfAccounts> findByBuIdOrderByIdDesc(Long buId);

    List<AfmChartOfAccounts> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmChartOfAccounts> findByBuIdAndAccountCodeIgnoreCase(
            Long buId, String accountCode);

    List<AfmChartOfAccounts> findByBuIdAndAccountHeadNameIgnoreCase(
            Long buId, String accountHeadName);

    List<AfmChartOfAccounts> findByParentAccountHead(Long parentAccountHead);

    List<AfmChartOfAccounts> findByParentAccountHeadOrderByIdAsc(Long parentAccountHead);

    boolean existsByParentAccountHead(Long parentAccountHead);

    List<AfmChartOfAccounts> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmChartOfAccounts> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);
}
