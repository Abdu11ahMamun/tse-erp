package com.tse.erp.module.accounting.service;

import com.tse.erp.common.AppConstants;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.accounting.entity.AfmChartOfAccounts;
import com.tse.erp.module.accounting.repository.AfmChartOfAccountsRepository;
import com.tse.erp.module.accounting.service.impl.AfmChartOfAccountsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmChartOfAccountsServiceTest {

    @Mock
    private AfmChartOfAccountsRepository repository;

    @Mock
    private BusinessGroupRepository businessGroupRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private AfmChartOfAccountsServiceImpl service;

    private BusinessGroup sampleBg;
    private BusinessUnit sampleBu;
    private AfmChartOfAccounts rootAccount;

    @BeforeEach
    void setUp() {
        sampleBg = BusinessGroup.builder()
                .id(1L)
                .groupName("Test Group")
                .build();
        sampleBg.setStatus(1);

        sampleBu = BusinessUnit.builder()
                .id(10L)
                .bgId(1L)
                .businessUnit("Test Unit")
                .build();
        sampleBu.setStatus(1);

        rootAccount = AfmChartOfAccounts.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .accountCode("1000")
                .accountHeadName("Assets")
                .ledgerHead("Title Account")
                .accountHeadType("Asset")
                .level(1)
                .sortOrder(new BigDecimal("1.00"))
                .accountUsage("None")
                .accountsSource("None")
                .isCostCenterMandatory(0)
                .isBudgetAccHead(0)
                .build();
        rootAccount.setStatus(1);
    }

    @Test
    void createAccount_Success_RootAccount() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(sampleBg));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(sampleBu));
        when(repository.findByBuIdAndAccountCodeIgnoreCase(10L, "1000")).thenReturn(Collections.emptyList());
        when(repository.findByBuIdAndAccountHeadNameIgnoreCase(10L, "Assets")).thenReturn(Collections.emptyList());
        when(repository.save(any(AfmChartOfAccounts.class))).thenAnswer(invocation -> {
            AfmChartOfAccounts saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        AfmChartOfAccounts toCreate = AfmChartOfAccounts.builder()
                .bgId(1L)
                .buId(10L)
                .accountCode("1000")
                .accountHeadName("Assets")
                .ledgerHead("Title Account")
                .accountHeadType("Asset")
                .sortOrder(new BigDecimal("1.00"))
                .build();

        AfmChartOfAccounts result = service.createAccount(toCreate);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("1000", result.getAccountCode());
        assertEquals("Assets", result.getAccountHeadName());
        assertEquals(1, result.getLevel());
        assertEquals(1, result.getStatus());
        assertEquals(0, result.getIsCostCenterMandatory());
        assertEquals(0, result.getIsBudgetAccHead());
        assertEquals(AppConstants.APP_NAME, result.getCreatedApp());
        assertNull(result.getUpdatedApp());
        assertNull(result.getUpdatedUserId());
        assertNull(result.getUpdatedEmpId());
        assertNull(result.getUpdatedDatetime());
        verify(repository).save(any(AfmChartOfAccounts.class));
    }

    @Test
    void createAccount_Success_ChildAccount() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(sampleBg));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(sampleBu));
        when(repository.findByBuIdAndAccountCodeIgnoreCase(10L, "1010")).thenReturn(Collections.emptyList());
        when(repository.findByBuIdAndAccountHeadNameIgnoreCase(10L, "Current Assets")).thenReturn(Collections.emptyList());
        when(repository.findById(100L)).thenReturn(Optional.of(rootAccount));
        when(repository.save(any(AfmChartOfAccounts.class))).thenAnswer(invocation -> {
            AfmChartOfAccounts saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        AfmChartOfAccounts child = AfmChartOfAccounts.builder()
                .bgId(1L)
                .buId(10L)
                .accountCode("1010")
                .accountHeadName("Current Assets")
                .ledgerHead("Control Account")
                .accountHeadType("Asset")
                .parentAccountHead(100L)
                .sortOrder(new BigDecimal("2.00"))
                .build();

        AfmChartOfAccounts result = service.createAccount(child);

        assertNotNull(result);
        assertEquals("1000", result.getParentAccountCode());
        assertEquals(2, result.getLevel());
        assertEquals(AppConstants.APP_NAME, result.getCreatedApp());
        assertNull(result.getUpdatedApp());
        assertNull(result.getUpdatedUserId());
        assertNull(result.getUpdatedEmpId());
        assertNull(result.getUpdatedDatetime());
        verify(repository).save(any(AfmChartOfAccounts.class));
    }

    @Test
    void createAccount_DuplicateCode_ThrowsDuplicateResourceException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(sampleBg));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(sampleBu));
        when(repository.findByBuIdAndAccountCodeIgnoreCase(10L, "1000")).thenReturn(List.of(rootAccount));

        AfmChartOfAccounts duplicate = AfmChartOfAccounts.builder()
                .bgId(1L)
                .buId(10L)
                .accountCode("1000")
                .accountHeadName("Unique Name")
                .ledgerHead("Title Account")
                .accountHeadType("Asset")
                .build();

        assertThrows(DuplicateResourceException.class, () -> service.createAccount(duplicate));
    }

    @Test
    void createAccount_ParentIsActiveAccount_ThrowsBadRequestException() {
        AfmChartOfAccounts activeParent = AfmChartOfAccounts.builder()
                .id(200L)
                .bgId(1L)
                .buId(10L)
                .accountCode("1001")
                .accountHeadName("Cash in Hand")
                .ledgerHead("Active Account")
                .accountHeadType("Asset")
                .build();
        activeParent.setStatus(1);

        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(sampleBg));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(sampleBu));
        when(repository.findByBuIdAndAccountCodeIgnoreCase(10L, "100101")).thenReturn(Collections.emptyList());
        when(repository.findByBuIdAndAccountHeadNameIgnoreCase(10L, "Petty Cash")).thenReturn(Collections.emptyList());
        when(repository.findById(200L)).thenReturn(Optional.of(activeParent));

        AfmChartOfAccounts child = AfmChartOfAccounts.builder()
                .bgId(1L)
                .buId(10L)
                .accountCode("100101")
                .accountHeadName("Petty Cash")
                .ledgerHead("Active Account")
                .accountHeadType("Asset")
                .parentAccountHead(200L)
                .build();

        assertThrows(BadRequestException.class, () -> service.createAccount(child));
    }

    @Test
    void deleteAccount_HasChildren_ThrowsBadRequestException() {
        when(repository.findById(100L)).thenReturn(Optional.of(rootAccount));
        when(repository.existsByParentAccountHead(100L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.deleteAccount(100L));
        verify(repository, never()).delete(any());
    }

    @Test
    void deleteAccount_NoChildren_Success() {
        when(repository.findById(100L)).thenReturn(Optional.of(rootAccount));
        when(repository.existsByParentAccountHead(100L)).thenReturn(false);

        service.deleteAccount(100L);

        verify(repository).delete(rootAccount);
    }

    @Test
    void updateAccount_Success_SetsUpdatedApp() {
        when(repository.findById(100L)).thenReturn(Optional.of(rootAccount));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(sampleBg));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(sampleBu));
        when(repository.findByBuIdAndAccountCodeIgnoreCase(10L, "1000")).thenReturn(List.of(rootAccount));
        when(repository.findByBuIdAndAccountHeadNameIgnoreCase(10L, "Assets Updated")).thenReturn(Collections.emptyList());
        when(repository.save(any(AfmChartOfAccounts.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AfmChartOfAccounts updateRequest = AfmChartOfAccounts.builder()
                .bgId(1L)
                .buId(10L)
                .accountCode("1000")
                .accountHeadName("Assets Updated")
                .ledgerHead("Title Account")
                .accountHeadType("Asset")
                .sortOrder(new BigDecimal("1.00"))
                .build();

        AfmChartOfAccounts updated = service.updateAccount(100L, updateRequest);

        assertNotNull(updated);
        assertEquals("Assets Updated", updated.getAccountHeadName());
        assertEquals(AppConstants.APP_NAME, updated.getUpdatedApp());
        verify(repository).save(rootAccount);
    }
}
