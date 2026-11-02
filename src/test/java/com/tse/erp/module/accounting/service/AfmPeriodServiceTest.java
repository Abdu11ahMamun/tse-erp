package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.module.accounting.dto.AfmPeriodRequestDto;
import com.tse.erp.module.accounting.dto.AfmPeriodResponseDto;
import com.tse.erp.module.accounting.entity.AfmPeriod;
import com.tse.erp.module.accounting.repository.AfmCalendarMonthRepository;
import com.tse.erp.module.accounting.repository.AfmCalendarRepository;
import com.tse.erp.module.accounting.repository.AfmPeriodRepository;
import com.tse.erp.module.accounting.service.impl.AfmPeriodServiceImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmPeriodServiceTest {

    @Mock
    private AfmPeriodRepository periodRepository;
    @Mock
    private AfmCalendarRepository calendarRepository;
    @Mock
    private AfmCalendarMonthRepository calendarMonthRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private AfmPeriodServiceImpl periodService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("Test Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("Test BU").build();
        businessUnit.setStatus(1);
    }

    @Test
    void createPeriod_Success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(periodRepository.existsByBuIdAndPeriodYearAndPeriodNameIgnoreCase(10L, "2026", "Jan-2026"))
                .thenReturn(false);

        when(periodRepository.save(any(AfmPeriod.class))).thenAnswer(invocation -> {
            AfmPeriod p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        when(businessGroupRepository.findAllById(any())).thenReturn(List.of(businessGroup));
        when(businessUnitRepository.findAllById(any())).thenReturn(List.of(businessUnit));

        AfmPeriodRequestDto request = AfmPeriodRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .periodYear("2026")
                .periodName("Jan-2026")
                .periodStatus("Open")
                .status(1)
                .build();

        AfmPeriodResponseDto response = periodService.createPeriod(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getPeriodYear()).isEqualTo("2026");
        assertThat(response.getPeriodName()).isEqualTo("Jan-2026");
        assertThat(response.getPeriodStatus()).isEqualTo("Open");
    }

    @Test
    void createPeriod_Duplicate_ThrowsDuplicateException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(periodRepository.existsByBuIdAndPeriodYearAndPeriodNameIgnoreCase(10L, "2026", "Jan-2026"))
                .thenReturn(true);

        AfmPeriodRequestDto request = AfmPeriodRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .periodYear("2026")
                .periodName("Jan-2026")
                .periodStatus("Open")
                .build();

        assertThatThrownBy(() -> periodService.createPeriod(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getOpenPeriodNames_QueriesCalendarMonth() {
        when(calendarMonthRepository.findOpenPeriodNamesByBuIdAndYear(10L, "2026"))
                .thenReturn(List.of("Jan-2026", "Feb-2026"));

        List<String> result = periodService.getOpenPeriodNames(10L, "2026");

        assertThat(result).containsExactly("Jan-2026", "Feb-2026");
    }

    @Test
    void deletePeriod_Success() {
        AfmPeriod period = AfmPeriod.builder().id(50L).build();
        when(periodRepository.findById(50L)).thenReturn(Optional.of(period));

        periodService.deletePeriod(50L);

        verify(periodRepository).deleteById(50L);
    }
}

