package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.entity.AfmChartOfAccounts;

import java.util.List;

public interface AfmChartOfAccountsService {

    List<AfmChartOfAccounts> getAllAccounts(Long bgId, Long buId);

    AfmChartOfAccounts getAccountById(Long id);

    List<AfmChartOfAccounts> getAccountsByParentId(Long parentId);

    AfmChartOfAccounts createAccount(AfmChartOfAccounts account);

    AfmChartOfAccounts updateAccount(Long id, AfmChartOfAccounts account);

    void deleteAccount(Long id);
}
