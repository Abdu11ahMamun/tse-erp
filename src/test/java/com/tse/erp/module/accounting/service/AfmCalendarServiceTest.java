package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.module.accounting.dto.AfmCalendarMonthDto;
import com.tse.erp.module.accounting.dto.AfmCalendarRequestDto;
import com.tse.erp.module.accounting.dto.AfmCalendarResponseDto;
import com.tse.erp.module.accounting.entity.AfmCalendar;
import com.tse.erp.module.accounting.entity.AfmCalendarMonth;
import com.tse.erp.module.accounting.repository.AfmCalendarMonthRepository;
import com.tse.erp.module.accounting.repository.AfmCalendarRepository;
import com.tse.erp.module.accounting.service.impl.AfmCalendarServiceImpl;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmCalendarServiceTest {

    @Mock
    private AfmCalendarRepository calendarRepository;
    @Mock
    private AfmCalendarMonthRepository calendarMonthRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private AfmCalendarServiceImpl calendarService;

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
    void createCalendar_Success_WithMonths() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(calendarRepository.findByBuIdAndCalendarYearIgnoreCase(10L, "2026")).thenReturn(Collections.emptyList());

        when(calendarRepository.save(any(AfmCalendar.class))).thenAnswer(invocation -> {
            AfmCalendar c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        when(calendarMonthRepository.saveAll(any())).thenAnswer(invocation -> {
            List<AfmCalendarMonth> list = invocation.getArgument(0);
            for (long i = 0; i < list.size(); i++) {
                list.get((int) i).setId(i + 1);
            }
            return list;
        });

        when(businessGroupRepository.findAllById(any())).thenReturn(List.of(businessGroup));
        when(businessUnitRepository.findAllById(any())).thenReturn(List.of(businessUnit));

        AfmCalendarMonthDto m1 = AfmCalendarMonthDto.builder()
                .serialNo(1)
                .calendarYear("2026")
                .calendarMonth("January")
                .calendarQuater("1st Quarter")
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 1, 31))
                .periodName("Jan-2026")
                .periodStatus("Open")
                .status(1)
                .build();

        AfmCalendarRequestDto request = AfmCalendarRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .calendarYear("2026")
                .calendarType("Calendar Year")
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 12, 31))
                .period("12")
                .status(1)
                .calendarMonths(List.of(m1))
                .build();

        AfmCalendarResponseDto response = calendarService.createCalendar(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCalendarYear()).isEqualTo("2026");
        assertThat(response.getCalendarType()).isEqualTo("Calendar Year");
        assertThat(response.getCalendarMonths()).hasSize(1);
        assertThat(response.getCalendarMonths().get(0).getCalendarMonth()).isEqualTo("January");
    }

    @Test
    void createCalendar_DuplicateYear_ThrowsDuplicateException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));

        AfmCalendar existing = AfmCalendar.builder().id(50L).buId(10L).calendarYear("2026").build();
        when(calendarRepository.findByBuIdAndCalendarYearIgnoreCase(10L, "2026")).thenReturn(List.of(existing));

        AfmCalendarRequestDto request = AfmCalendarRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .calendarYear("2026")
                .calendarType("Calendar Year")
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 12, 31))
                .period("12")
                .build();

        assertThatThrownBy(() -> calendarService.createCalendar(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createCalendar_InvalidDates_ThrowsBadRequest() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));

        AfmCalendarRequestDto request = AfmCalendarRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .calendarYear("2026")
                .calendarType("Calendar Year")
                .fromDate(LocalDate.of(2026, 12, 31))
                .toDate(LocalDate.of(2026, 1, 1))
                .build();

        assertThatThrownBy(() -> calendarService.createCalendar(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be after");
    }

    @Test
    void deleteCalendar_Success_CascadesDetailDelete() {
        AfmCalendar existing = AfmCalendar.builder().id(50L).buId(10L).calendarYear("2026").build();
        when(calendarRepository.findById(50L)).thenReturn(Optional.of(existing));

        calendarService.deleteCalendar(50L);

        verify(calendarMonthRepository).deleteByAfmCalendarId(50L);
        verify(calendarRepository).deleteById(50L);
    }
}

