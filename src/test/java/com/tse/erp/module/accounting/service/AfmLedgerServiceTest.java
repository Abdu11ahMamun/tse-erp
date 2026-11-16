package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmLedgerDetailDto;
import com.tse.erp.module.accounting.dto.AfmLedgerRequestDto;
import com.tse.erp.module.accounting.dto.AfmLedgerResponseDto;
import com.tse.erp.module.accounting.entity.*;
import com.tse.erp.module.accounting.repository.*;
import com.tse.erp.module.accounting.service.impl.AfmLedgerServiceImpl;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmLedgerServiceTest {

    @Mock
    private AfmLedgerRepository ledgerRepository;
    @Mock
    private AfmLedgerDetailRepository ledgerDetailRepository;
    @Mock
    private AfmLedgerAttachRepository ledgerAttachRepository;
    @Mock
    private AfmVoucherRepository voucherRepository;
    @Mock
    private AfmVoucherDetailRepository voucherDetailRepository;
    @Mock
    private AfmVoucherAttachRepository voucherAttachRepository;
    @Mock
    private AfmLookupValueRepository lookupValueRepository;
    @Mock
    private AfmChartOfAccountsRepository chartOfAccountsRepository;
    @Mock
    private AfmCostCenterRepository costCenterRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private AfmLedgerServiceImpl ledgerService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("TSE Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("TSE Textiles").build();
        businessUnit.setStatus(1);
    }

    @Test
    void createLedger_success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(ledgerRepository.existsByBuIdAndVoucherNoIgnoreCase(10L, "V-001")).thenReturn(false);

        AfmLedger saved = AfmLedger.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-001")
                .voucherAmount(new BigDecimal("5000.00"))
                .build();
        saved.setStatus(1);

        when(ledgerRepository.save(any(AfmLedger.class))).thenReturn(saved);

        AfmLedgerRequestDto req = AfmLedgerRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-001")
                .voucherAmount(new BigDecimal("5000.00"))
                .status(1)
                .build();

        AfmLedgerResponseDto response = ledgerService.createLedger(req);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getVoucherNo()).isEqualTo("V-001");
        verify(ledgerRepository).save(any(AfmLedger.class));
    }

    @Test
    void syncFromVoucherCreate_savesLedgerAndDetails() {
        AfmVoucher voucher = AfmVoucher.builder()
                .id(200L)
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-200")
                .voucherAmount(new BigDecimal("1200.00"))
                .voucherDate(LocalDate.now())
                .build();
        voucher.setStatus(1);

        AfmLedger savedLedger = AfmLedger.builder()
                .id(300L)
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-200")
                .voucherSource("AFM_VOUCHER")
                .voucherSourceLink("200")
                .build();
        when(ledgerRepository.save(any(AfmLedger.class))).thenReturn(savedLedger);

        AfmVoucherDetail vd = AfmVoucherDetail.builder()
                .id(1L)
                .voucherId(200L)
                .coaId(50L)
                .drAmount(new BigDecimal("1200.00"))
                .crAmount(BigDecimal.ZERO)
                .build();
        vd.setStatus(1);

        AfmVoucherAttach va = AfmVoucherAttach.builder()
                .id(1L)
                .voucherId(200L)
                .attachmentFileName("doc.pdf")
                .isActive(1)
                .build();
        va.setStatus(1);

        ledgerService.syncFromVoucherCreate(voucher, List.of(vd), List.of(va));

        verify(ledgerRepository).save(any(AfmLedger.class));
        verify(ledgerDetailRepository).saveAll(anyList());
        verify(ledgerAttachRepository).saveAll(anyList());
    }

    @Test
    void syncFromVoucherDelete_removesLedgerAndChildren() {
        AfmLedger ledger = AfmLedger.builder()
                .id(300L)
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-200")
                .build();

        when(ledgerRepository.findByVoucherSourceAndVoucherSourceLink("AFM_VOUCHER", "200"))
                .thenReturn(Optional.of(ledger));

        ledgerService.syncFromVoucherDelete(200L, 10L, "V-200");

        verify(ledgerDetailRepository).deleteByVoucherId(300L);
        verify(ledgerAttachRepository).deleteByVoucherId(300L);
        verify(ledgerRepository).delete(ledger);
    }
}
