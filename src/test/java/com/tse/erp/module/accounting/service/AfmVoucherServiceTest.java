package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.*;
import com.tse.erp.module.accounting.entity.*;
import com.tse.erp.module.accounting.repository.*;
import com.tse.erp.module.accounting.service.impl.AfmVoucherServiceImpl;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmVoucherServiceTest {

    @Mock
    private AfmVoucherRepository voucherRepository;
    @Mock
    private AfmVoucherDetailRepository voucherDetailRepository;
    @Mock
    private AfmVoucherAttachRepository voucherAttachRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private AfmLookupValueRepository lookupValueRepository;
    @Mock
    private AfmChartOfAccountsRepository chartOfAccountsRepository;
    @Mock
    private AfmCostCenterRepository costCenterRepository;

    @InjectMocks
    private AfmVoucherServiceImpl voucherService;

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
    void createVoucher_success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(voucherRepository.existsByBuIdAndVoucherNoIgnoreCase(10L, "V-2026-001")).thenReturn(false);

        AfmLookupValue voucherTypeLookup = AfmLookupValue.builder().id(100L).value("Bank Payment").build();
        when(lookupValueRepository.findById(100L)).thenReturn(Optional.of(voucherTypeLookup));

        AfmVoucher saved = AfmVoucher.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .voucherDate(LocalDate.of(2026, 1, 15))
                .voucherTypeId(100L)
                .voucherType("Bank Payment")
                .voucherNo("V-2026-001")
                .voucherAmount(new BigDecimal("5000.00"))
                .build();
        saved.setStatus(1);

        when(voucherRepository.save(any(AfmVoucher.class))).thenReturn(saved);
        when(voucherDetailRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());
        when(voucherAttachRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());

        AfmVoucherRequestDto request = AfmVoucherRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .voucherDate(LocalDate.of(2026, 1, 15))
                .voucherTypeId(100L)
                .voucherNo("V-2026-001")
                .voucherAmount(new BigDecimal("5000.00"))
                .status(1)
                .voucherDetails(List.of(
                        AfmVoucherDetailDto.builder()
                                .coaId(200L)
                                .drAmount(new BigDecimal("5000.00"))
                                .crAmount(BigDecimal.ZERO)
                                .build()
                ))
                .voucherAttachments(List.of(
                        AfmVoucherAttachDto.builder()
                                .attachmentFileName("invoice.pdf")
                                .attachmentFile("data:application/pdf;base64,...")
                                .build()
                ))
                .build();

        AfmVoucherResponseDto response = voucherService.createVoucher(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getVoucherNo()).isEqualTo("V-2026-001");
        assertThat(response.getBusinessUnitName()).isEqualTo("TSE Textiles");
        verify(voucherRepository, times(1)).save(any(AfmVoucher.class));
        verify(voucherDetailRepository, times(1)).saveAll(anyList());
        verify(voucherAttachRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createVoucher_duplicateNo_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(voucherRepository.existsByBuIdAndVoucherNoIgnoreCase(10L, "V-2026-001")).thenReturn(true);

        AfmVoucherRequestDto request = AfmVoucherRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-2026-001")
                .status(1)
                .build();

        assertThatThrownBy(() -> voucherService.createVoucher(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Voucher No already exists");
    }

    @Test
    void getVoucherById_success() {
        AfmVoucher voucher = AfmVoucher.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-2026-001")
                .voucherType("Bank Payment")
                .build();
        voucher.setStatus(1);

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(voucherDetailRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());
        when(voucherAttachRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());

        AfmVoucherResponseDto response = voucherService.getVoucherById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getVoucherNo()).isEqualTo("V-2026-001");
    }

    @Test
    void getVoucherById_notFound_throwsException() {
        when(voucherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherService.getVoucherById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateVoucher_success() {
        AfmVoucher existing = AfmVoucher.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-2026-001")
                .build();
        existing.setStatus(1);

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(voucherRepository.findByBuIdAndVoucherNoIgnoreCase(10L, "V-2026-002")).thenReturn(Optional.empty());
        when(voucherRepository.save(any(AfmVoucher.class))).thenReturn(existing);
        when(voucherDetailRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());
        when(voucherAttachRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());

        AfmVoucherRequestDto request = AfmVoucherRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .voucherNo("V-2026-002")
                .status(1)
                .build();

        AfmVoucherResponseDto updated = voucherService.updateVoucher(1L, request);

        assertThat(updated).isNotNull();
        verify(voucherRepository, times(1)).save(existing);
    }

    @Test
    void deleteVoucher_success() {
        AfmVoucher existing = AfmVoucher.builder().id(1L).bgId(1L).buId(10L).build();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(voucherDetailRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());
        when(voucherAttachRepository.findByVoucherIdOrderByIdAsc(1L)).thenReturn(Collections.emptyList());

        voucherService.deleteVoucher(1L);

        verify(voucherRepository, times(1)).delete(existing);
    }
}
