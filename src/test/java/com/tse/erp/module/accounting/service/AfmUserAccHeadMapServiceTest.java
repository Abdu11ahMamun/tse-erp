package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.module.accounting.dto.AfmUserAccHeadMapDto;
import com.tse.erp.module.accounting.entity.AfmChartOfAccounts;
import com.tse.erp.module.accounting.entity.AfmUserAccHeadMap;
import com.tse.erp.module.accounting.repository.AfmChartOfAccountsRepository;
import com.tse.erp.module.accounting.repository.AfmUserAccHeadMapRepository;
import com.tse.erp.module.accounting.service.impl.AfmUserAccHeadMapServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmUserAccHeadMapServiceTest {

    @Mock
    private AfmUserAccHeadMapRepository userAccHeadMapRepository;

    @Mock
    private AfmChartOfAccountsRepository chartOfAccountsRepository;

    @InjectMocks
    private AfmUserAccHeadMapServiceImpl service;

    @Test
    void saveAllForUser_duplicateAccountsInBatch_throwsBadRequestException() {
        AfmUserAccHeadMapDto item1 = AfmUserAccHeadMapDto.builder().accountHeadId(10L).status(1).build();
        AfmUserAccHeadMapDto item2 = AfmUserAccHeadMapDto.builder().accountHeadId(10L).status(1).build();

        assertThatThrownBy(() -> service.saveAllForUser(1L, 2L, 5L, List.of(item1, item2)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Same Accounts Name is not allowed to be submitted against one user");

        verify(userAccHeadMapRepository, never()).save(any());
    }

    @Test
    void create_duplicateAccountForUser_throwsBadRequestException() {
        when(userAccHeadMapRepository.existsByBuIdAndUserIdAndAccountHeadId(2L, 5L, 10L)).thenReturn(true);

        AfmUserAccHeadMapDto dto = AfmUserAccHeadMapDto.builder()
                .bgId(1L)
                .buId(2L)
                .userId(5L)
                .accountHeadId(10L)
                .status(1)
                .build();

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Same Accounts Name is not allowed to be submitted against one user");

        verify(userAccHeadMapRepository, never()).save(any());
    }

    @Test
    void update_duplicateAccountForUser_throwsBadRequestException() {
        AfmUserAccHeadMap existing = AfmUserAccHeadMap.builder()
                .id(1L)
                .buId(2L)
                .userId(5L)
                .accountHeadId(10L)
                .build();

        AfmUserAccHeadMap other = AfmUserAccHeadMap.builder()
                .id(2L)
                .buId(2L)
                .userId(5L)
                .accountHeadId(20L)
                .build();

        when(userAccHeadMapRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userAccHeadMapRepository.findByBuIdAndUserIdAndAccountHeadId(2L, 5L, 20L)).thenReturn(Optional.of(other));

        AfmUserAccHeadMapDto updateDto = AfmUserAccHeadMapDto.builder().accountHeadId(20L).build();

        assertThatThrownBy(() -> service.update(1L, updateDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Same Accounts Name is not allowed to be submitted against one user");

        verify(userAccHeadMapRepository, never()).save(any());
    }

    @Test
    void saveAllForUser_success() {
        AfmUserAccHeadMapDto item1 = AfmUserAccHeadMapDto.builder().accountHeadId(10L).status(1).build();
        AfmUserAccHeadMapDto item2 = AfmUserAccHeadMapDto.builder().accountHeadId(20L).status(1).build();

        when(userAccHeadMapRepository.findByBuIdAndUserIdAndAccountHeadId(2L, 5L, 10L)).thenReturn(Optional.empty());
        when(userAccHeadMapRepository.findByBuIdAndUserIdAndAccountHeadId(2L, 5L, 20L)).thenReturn(Optional.empty());

        AfmChartOfAccounts coa1 = AfmChartOfAccounts.builder().id(10L).accountCode("1001").accountHeadName("Cash").build();
        AfmChartOfAccounts coa2 = AfmChartOfAccounts.builder().id(20L).accountCode("1002").accountHeadName("Bank").build();

        when(chartOfAccountsRepository.findById(10L)).thenReturn(Optional.of(coa1));
        when(chartOfAccountsRepository.findById(20L)).thenReturn(Optional.of(coa2));

        when(userAccHeadMapRepository.save(any(AfmUserAccHeadMap.class))).thenAnswer(invocation -> {
            AfmUserAccHeadMap saved = invocation.getArgument(0);
            saved.setId(saved.getAccountHeadId() == 10L ? 101L : 102L);
            return saved;
        });

        List<AfmUserAccHeadMapDto> savedList = service.saveAllForUser(1L, 2L, 5L, List.of(item1, item2));

        assertThat(savedList).hasSize(2);
        assertThat(savedList.get(0).getAccountHeadId()).isEqualTo(10L);
        assertThat(savedList.get(1).getAccountHeadId()).isEqualTo(20L);
        verify(userAccHeadMapRepository, times(2)).save(any(AfmUserAccHeadMap.class));
    }
}
