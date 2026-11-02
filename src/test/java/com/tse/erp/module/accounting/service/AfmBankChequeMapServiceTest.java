package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapResponseDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapUpdateDto;
import com.tse.erp.module.accounting.entity.AfmBank;
import com.tse.erp.module.accounting.entity.AfmBankDtls;
import com.tse.erp.module.accounting.entity.AfmBankChequeMap;
import com.tse.erp.module.accounting.repository.AfmBankChequeMapRepository;
import com.tse.erp.module.accounting.repository.AfmBankDtlsRepository;
import com.tse.erp.module.accounting.repository.AfmBankRepository;
import com.tse.erp.module.accounting.service.impl.AfmBankChequeMapServiceImpl;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmBankChequeMapServiceTest {

    @Mock
    private AfmBankChequeMapRepository chequeMapRepository;
    @Mock
    private AfmBankRepository bankRepository;
    @Mock
    private AfmBankDtlsRepository bankDtlsRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private AfmBankChequeMapServiceImpl chequeMapService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;
    private AfmBank bank;
    private AfmBankDtls bankDtls;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("Test Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("Test BU").build();
        businessUnit.setStatus(1);

        bank = AfmBank.builder().id(100L).bgId(1L).buId(10L).bankName("Sonali Bank").build();
        bank.setStatus(1);

        bankDtls = AfmBankDtls.builder().id(200L).bgId(1L).buId(10L).afmBankId(100L).accountNumber("123456789").build();
        bankDtls.setStatus(1);
    }

    @Test
    void createChequeBatch_Success_InsertsRange() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(bankRepository.findById(100L)).thenReturn(Optional.of(bank));
        when(bankDtlsRepository.findById(200L)).thenReturn(Optional.of(bankDtls));
        when(chequeMapRepository.findExistingChequeNumbers(eq(200L), anyCollection())).thenReturn(Collections.emptyList());

        when(chequeMapRepository.saveAll(any())).thenAnswer(invocation -> {
            List<AfmBankChequeMap> items = invocation.getArgument(0);
            for (long idx = 0; idx < items.size(); idx++) {
                items.get((int) idx).setId(idx + 1);
            }
            return items;
        });

        when(businessGroupRepository.findAllById(any())).thenReturn(List.of(businessGroup));
        when(businessUnitRepository.findAllById(any())).thenReturn(List.of(businessUnit));
        when(bankRepository.findAllById(any())).thenReturn(List.of(bank));
        when(bankDtlsRepository.findAllById(any())).thenReturn(List.of(bankDtls));

        AfmBankChequeMapRequestDto request = AfmBankChequeMapRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .bankId(100L)
                .bankAccountId(200L)
                .chequeNoFrom("1000")
                .chequeNoTo("1005")
                .chequeStatus("Not Use")
                .status(1)
                .build();

        List<AfmBankChequeMapResponseDto> result = chequeMapService.createChequeBatch(request);

        assertThat(result).hasSize(5);
        assertThat(result.stream().map(AfmBankChequeMapResponseDto::getChequeNo))
                .containsExactly("1000", "1001", "1002", "1003", "1004");
        assertThat(result.get(0).getChequeNoFrom()).isEqualTo("1000");
        assertThat(result.get(0).getChequeNoTo()).isEqualTo("1005");
        assertThat(result.get(0).getChequeStatus()).isEqualTo("Not Use");
        assertThat(result.get(0).getBankName()).isEqualTo("Sonali Bank");
        assertThat(result.get(0).getAccountNumber()).isEqualTo("123456789");

        ArgumentCaptor<List<AfmBankChequeMap>> captor = ArgumentCaptor.forClass(List.class);
        verify(chequeMapRepository).saveAll(captor.capture());
        List<AfmBankChequeMap> saved = captor.getValue();
        assertThat(saved).hasSize(5);
        assertThat(saved.get(0).getChequeNo()).isEqualTo("1000");
        assertThat(saved.get(4).getChequeNo()).isEqualTo("1004");
    }

    @Test
    void createChequeBatch_InvalidRange_ThrowsBadRequest() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(bankRepository.findById(100L)).thenReturn(Optional.of(bank));
        when(bankDtlsRepository.findById(200L)).thenReturn(Optional.of(bankDtls));

        AfmBankChequeMapRequestDto request = AfmBankChequeMapRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .bankId(100L)
                .bankAccountId(200L)
                .chequeNoFrom("1005")
                .chequeNoTo("1000")
                .chequeStatus("Not Use")
                .status(1)
                .build();

        assertThatThrownBy(() -> chequeMapService.createChequeBatch(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be greater than");
    }

    @Test
    void createChequeBatch_DuplicateCheque_ThrowsDuplicateException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(bankRepository.findById(100L)).thenReturn(Optional.of(bank));
        when(bankDtlsRepository.findById(200L)).thenReturn(Optional.of(bankDtls));
        when(chequeMapRepository.findExistingChequeNumbers(eq(200L), anyCollection()))
                .thenReturn(List.of("1002"));

        AfmBankChequeMapRequestDto request = AfmBankChequeMapRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .bankId(100L)
                .bankAccountId(200L)
                .chequeNoFrom("1000")
                .chequeNoTo("1005")
                .chequeStatus("Not Use")
                .status(1)
                .build();

        assertThatThrownBy(() -> chequeMapService.createChequeBatch(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("1002");
    }

    @Test
    void deleteChequeMap_UsedStatus_ThrowsBadRequest() {
        AfmBankChequeMap usedCheque = AfmBankChequeMap.builder()
                .id(50L)
                .chequeStatus("Used")
                .build();
        when(chequeMapRepository.findById(50L)).thenReturn(Optional.of(usedCheque));

        assertThatThrownBy(() -> chequeMapService.deleteChequeMap(50L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot delete cheque that has already been marked as Used");
    }

    @Test
    void updateChequeMap_Success() {
        AfmBankChequeMap cheque = AfmBankChequeMap.builder()
                .id(50L)
                .bgId(1L)
                .buId(10L)
                .bankId(100L)
                .bankAccountId(200L)
                .chequeNoFrom("1000")
                .chequeNoTo("1005")
                .chequeNo("1000")
                .chequeStatus("Not Use")
                .build();
        cheque.setStatus(1);

        when(chequeMapRepository.findById(50L)).thenReturn(Optional.of(cheque));
        when(chequeMapRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(businessGroupRepository.findAllById(any())).thenReturn(List.of(businessGroup));
        when(businessUnitRepository.findAllById(any())).thenReturn(List.of(businessUnit));
        when(bankRepository.findAllById(any())).thenReturn(List.of(bank));
        when(bankDtlsRepository.findAllById(any())).thenReturn(List.of(bankDtls));

        AfmBankChequeMapUpdateDto updateDto = AfmBankChequeMapUpdateDto.builder()
                .chequeStatus("Void")
                .status(1)
                .build();

        AfmBankChequeMapResponseDto response = chequeMapService.updateChequeMap(50L, updateDto);

        assertThat(response.getChequeStatus()).isEqualTo("Void");
        verify(chequeMapRepository).save(cheque);
    }
}

