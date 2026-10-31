package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.accounting.entity.AfmChartOfAccounts;
import com.tse.erp.module.accounting.repository.AfmChartOfAccountsRepository;
import com.tse.erp.module.accounting.service.AfmChartOfAccountsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AfmChartOfAccountsServiceImpl implements AfmChartOfAccountsService {

    private final AfmChartOfAccountsRepository repository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    private static final List<String> ALLOWED_LEDGER_HEADS = List.of(
            "Title Account", "Control Account", "Active Account"
    );

    private static final List<String> ALLOWED_ACCOUNT_HEAD_TYPES = List.of(
            "Asset", "Expenditure", "Income", "Liability", "Equity"
    );

    private static final List<String> ALLOWED_ACCOUNT_USAGES = List.of(
            "Bank", "Cash", "Ledger", "Inter Company", "None"
    );

    private static final List<String> ALLOWED_ACCOUNTS_SOURCES = List.of(
            "Customer", "Supplier", "Employee", "Loan Master", "Farm Shed", "None"
    );

    @Override
    public List<AfmChartOfAccounts> getAllAccounts(Long bgId, Long buId) {
        if (bgId != null && buId != null) {
            return repository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        }
        if (buId != null) {
            return repository.findByBuIdOrderByIdDesc(buId);
        }
        if (bgId != null) {
            return repository.findByBgIdOrderByIdDesc(bgId);
        }
        return repository.findAllByOrderByIdDesc();
    }

    @Override
    public AfmChartOfAccounts getAccountById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chart of Accounts head not found with id: " + id));
    }

    @Override
    public List<AfmChartOfAccounts> getAccountsByParentId(Long parentId) {
        if (parentId == null) {
            return repository.findByParentAccountHead(null);
        }
        return repository.findByParentAccountHeadOrderByIdAsc(parentId);
    }

    @Override
    public AfmChartOfAccounts createAccount(AfmChartOfAccounts account) {
        validateParents(account.getBgId(), account.getBuId());
        validateAccountCode(account.getAccountCode());
        validateAccountHeadName(account.getAccountHeadName());
        validateLedgerHead(account.getLedgerHead());
        validateAccountHeadType(account.getAccountHeadType());
        validateAccountUsage(account.getAccountUsage());
        validateAccountsSource(account.getAccountsSource());
        validateBinaryFlag(account.getIsCostCenterMandatory(), "Cost Center Mandatory");
        validateBinaryFlag(account.getIsBudgetAccHead(), "Budget Account Head");
        validateSortOrder(account.getSortOrder());
        StatusUtil.validate(account.getStatus());

        String code = account.getAccountCode().trim();
        String headName = account.getAccountHeadName().trim();

        boolean codeExists = !repository
                .findByBuIdAndAccountCodeIgnoreCase(account.getBuId(), code)
                .isEmpty();
        if (codeExists) {
            throw new DuplicateResourceException(
                    "Account code already exists for this Business Unit: " + code);
        }

        boolean headExists = !repository
                .findByBuIdAndAccountHeadNameIgnoreCase(account.getBuId(), headName)
                .isEmpty();
        if (headExists) {
            throw new DuplicateResourceException(
                    "Account head name already exists for this Business Unit: " + headName);
        }

        applyParentAccountDetails(account, null);

        account.setId(null);
        account.setAccountCode(code);
        account.setAccountHeadName(headName);
        account.setLedgerHead(canonicalLedgerHead(account.getLedgerHead()));
        account.setAccountHeadType(canonicalAccountHeadType(account.getAccountHeadType()));
        account.setAccountUsage(canonicalAccountUsage(account.getAccountUsage()));
        account.setAccountsSource(canonicalAccountsSource(account.getAccountsSource()));
        if (account.getIsCostCenterMandatory() == null) {
            account.setIsCostCenterMandatory(0);
        }
        if (account.getIsBudgetAccHead() == null) {
            account.setIsBudgetAccHead(0);
        }
        if (account.getStatus() == null) {
            account.setStatus(1);
        }
        account.setCreatedApp(AppConstants.APP_NAME);
       
        return repository.save(account);
    }

    @Override
    public AfmChartOfAccounts updateAccount(Long id, AfmChartOfAccounts account) {
        AfmChartOfAccounts existing = getAccountById(id);

        validateParents(account.getBgId(), account.getBuId());
        validateAccountCode(account.getAccountCode());
        validateAccountHeadName(account.getAccountHeadName());
        validateLedgerHead(account.getLedgerHead());
        validateAccountHeadType(account.getAccountHeadType());
        validateAccountUsage(account.getAccountUsage());
        validateAccountsSource(account.getAccountsSource());
        validateBinaryFlag(account.getIsCostCenterMandatory(), "Cost Center Mandatory");
        validateBinaryFlag(account.getIsBudgetAccHead(), "Budget Account Head");
        validateSortOrder(account.getSortOrder());
        StatusUtil.validate(account.getStatus());

        String code = account.getAccountCode().trim();
        String headName = account.getAccountHeadName().trim();

        boolean codeDuplicate = repository
                .findByBuIdAndAccountCodeIgnoreCase(account.getBuId(), code)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (codeDuplicate) {
            throw new DuplicateResourceException(
                    "Account code already exists for this Business Unit: " + code);
        }

        boolean headDuplicate = repository
                .findByBuIdAndAccountHeadNameIgnoreCase(account.getBuId(), headName)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (headDuplicate) {
            throw new DuplicateResourceException(
                    "Account head name already exists for this Business Unit: " + headName);
        }

        applyParentAccountDetails(account, id);

        existing.setBgId(account.getBgId());
        existing.setBuId(account.getBuId());
        existing.setAccountCode(code);
        existing.setAccountHeadName(headName);
        existing.setLedgerHead(canonicalLedgerHead(account.getLedgerHead()));
        existing.setAccountHeadType(canonicalAccountHeadType(account.getAccountHeadType()));
        existing.setParentAccountHead(account.getParentAccountHead());
        existing.setParentAccountCode(account.getParentAccountCode());
        existing.setSortOrder(account.getSortOrder());
        existing.setAccountUsage(canonicalAccountUsage(account.getAccountUsage()));
        existing.setAccountsSource(canonicalAccountsSource(account.getAccountsSource()));
        existing.setIsCostCenterMandatory(account.getIsCostCenterMandatory() != null ? account.getIsCostCenterMandatory() : 0);
        existing.setIsBudgetAccHead(account.getIsBudgetAccHead() != null ? account.getIsBudgetAccHead() : 0);
        existing.setLevel(account.getLevel());

        if (account.getStatus() != null) {
            existing.setStatus(account.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return repository.save(existing);
    }

    @Override
    public void deleteAccount(Long id) {
        AfmChartOfAccounts existing = getAccountById(id);

        if (repository.existsByParentAccountHead(id)) {
            throw new BadRequestException(
                    "Cannot delete: Sub-accounts exist under this parent account");
        }

        repository.delete(existing);
    }

    // =========================================
    // VALIDATIONS
    // =========================================

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }
        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Selected Business Unit is not active");
        }

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }
    }

    private void validateAccountCode(String accountCode) {
        if (accountCode == null || accountCode.trim().isEmpty()) {
            throw new BadRequestException("Account code cannot be empty");
        }
        if (accountCode.trim().length() < 1) {
            throw new BadRequestException("Account code must be at least 1 character");
        }
        if (accountCode.trim().length() > 50) {
            throw new BadRequestException("Account code cannot exceed 50 characters");
        }
    }

    private void validateAccountHeadName(String accountHeadName) {
        if (accountHeadName == null || accountHeadName.trim().isEmpty()) {
            throw new BadRequestException("Account head name cannot be empty");
        }
        if (accountHeadName.trim().length() < 2) {
            throw new BadRequestException("Account head name must be at least 2 characters");
        }
        if (accountHeadName.trim().length() > 150) {
            throw new BadRequestException("Account head name cannot exceed 150 characters");
        }
    }

    private void validateLedgerHead(String ledgerHead) {
        if (ledgerHead == null || ledgerHead.trim().isEmpty()) {
            throw new BadRequestException("Ledger head cannot be empty");
        }
        String trimmed = ledgerHead.trim();
        boolean match = ALLOWED_LEDGER_HEADS.stream().anyMatch(h -> h.equalsIgnoreCase(trimmed));
        if (!match) {
            throw new BadRequestException(
                    "Ledger head must be one of: " + String.join(", ", ALLOWED_LEDGER_HEADS));
        }
    }

    private String canonicalLedgerHead(String ledgerHead) {
        if (ledgerHead == null) return null;
        for (String h : ALLOWED_LEDGER_HEADS) {
            if (h.equalsIgnoreCase(ledgerHead.trim())) {
                return h;
            }
        }
        return ledgerHead.trim();
    }

    private void validateAccountHeadType(String accountHeadType) {
        if (accountHeadType == null || accountHeadType.trim().isEmpty()) {
            throw new BadRequestException("Account head type cannot be empty");
        }
        String trimmed = accountHeadType.trim();
        boolean match = ALLOWED_ACCOUNT_HEAD_TYPES.stream().anyMatch(t -> t.equalsIgnoreCase(trimmed));
        if (!match) {
            throw new BadRequestException(
                    "Account head type must be one of: " + String.join(", ", ALLOWED_ACCOUNT_HEAD_TYPES));
        }
    }

    private String canonicalAccountHeadType(String accountHeadType) {
        if (accountHeadType == null) return null;
        for (String t : ALLOWED_ACCOUNT_HEAD_TYPES) {
            if (t.equalsIgnoreCase(accountHeadType.trim())) {
                return t;
            }
        }
        return accountHeadType.trim();
    }

    private void validateAccountUsage(String accountUsage) {
        if (accountUsage == null || accountUsage.trim().isEmpty()) {
            return;
        }
        String trimmed = accountUsage.trim();
        boolean match = ALLOWED_ACCOUNT_USAGES.stream().anyMatch(u -> u.equalsIgnoreCase(trimmed));
        if (!match) {
            throw new BadRequestException(
                    "Account usage must be one of: " + String.join(", ", ALLOWED_ACCOUNT_USAGES));
        }
    }

    private String canonicalAccountUsage(String accountUsage) {
        if (accountUsage == null || accountUsage.trim().isEmpty()) return "None";
        for (String u : ALLOWED_ACCOUNT_USAGES) {
            if (u.equalsIgnoreCase(accountUsage.trim())) {
                return u;
            }
        }
        return accountUsage.trim();
    }

    private void validateAccountsSource(String accountsSource) {
        if (accountsSource == null || accountsSource.trim().isEmpty()) {
            return;
        }
        String trimmed = accountsSource.trim();
        boolean match = ALLOWED_ACCOUNTS_SOURCES.stream().anyMatch(s -> s.equalsIgnoreCase(trimmed));
        if (!match) {
            throw new BadRequestException(
                    "Accounts source must be one of: " + String.join(", ", ALLOWED_ACCOUNTS_SOURCES));
        }
    }

    private String canonicalAccountsSource(String accountsSource) {
        if (accountsSource == null || accountsSource.trim().isEmpty()) return "None";
        for (String s : ALLOWED_ACCOUNTS_SOURCES) {
            if (s.equalsIgnoreCase(accountsSource.trim())) {
                return s;
            }
        }
        return accountsSource.trim();
    }

    private void applyParentAccountDetails(AfmChartOfAccounts target, Long selfId) {
        Long parentId = target.getParentAccountHead();
        if (parentId == null) {
            target.setParentAccountCode(null);
            if (target.getLevel() == null || target.getLevel() <= 0) {
                target.setLevel(1);
            }
            return;
        }

        if (selfId != null && parentId.equals(selfId)) {
            throw new BadRequestException("An account cannot be its own parent account");
        }

        AfmChartOfAccounts parent = repository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent account head not found with id: " + parentId));

        if (!target.getBgId().equals(parent.getBgId())) {
            throw new BadRequestException(
                    "Parent account does not belong to selected Business Group");
        }

        if (!target.getBuId().equals(parent.getBuId())) {
            throw new BadRequestException(
                    "Parent account does not belong to selected Business Unit");
        }

        if (!Integer.valueOf(1).equals(parent.getStatus())) {
            throw new BadRequestException("Selected parent account is not active");
        }

        if ("Active Account".equalsIgnoreCase(parent.getLedgerHead())) {
            throw new BadRequestException(
                    "Parent account cannot be an 'Active Account'. Must be 'Title Account' or 'Control Account'");
        }

        if (selfId != null) {
            Long currentParentId = parent.getParentAccountHead();
            while (currentParentId != null) {
                if (currentParentId.equals(selfId)) {
                    throw new BadRequestException(
                            "Circular hierarchy detected: An account cannot have its own descendant as parent");
                }
                AfmChartOfAccounts ancestor = repository.findById(currentParentId).orElse(null);
                if (ancestor == null) {
                    break;
                }
                currentParentId = ancestor.getParentAccountHead();
            }
        }

        target.setParentAccountCode(parent.getAccountCode());
        if (target.getLevel() == null || target.getLevel() <= 0) {
            int parentLevel = parent.getLevel() != null ? parent.getLevel() : 1;
            target.setLevel(parentLevel + 1);
        }
    }

    private void validateBinaryFlag(Integer flag, String fieldName) {
        if (flag != null && flag != 0 && flag != 1) {
            throw new BadRequestException(fieldName + " must be 0 (No) or 1 (Yes)");
        }
    }

    private void validateSortOrder(BigDecimal sortOrder) {
        if (sortOrder != null && sortOrder.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Sort order must be greater than 0");
        }
    }
}
