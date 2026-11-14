package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmCalendarMonthDto;
import com.tse.erp.module.accounting.dto.AfmPeriodRequestDto;
import com.tse.erp.module.accounting.dto.AfmPeriodResponseDto;

import java.util.List;

public interface AfmPeriodService {

    List<AfmPeriodResponseDto> getAllPeriods(
            Long bgId,
            Long buId,
            String periodYear,
            String periodStatus,
            Integer status);

    AfmPeriodResponseDto getPeriodById(Long id);

    AfmPeriodResponseDto createPeriod(AfmPeriodRequestDto request);

    AfmPeriodResponseDto updatePeriod(Long id, AfmPeriodRequestDto request);

    void deletePeriod(Long id);

    List<String> getOpenPeriodNames(Long buId, String year);

    List<String> getOpenPeriodNames(Long bgId, Long buId, String year);

    List<String> getAvailableYears(Long buId);

    List<AfmCalendarMonthDto> getCandidateMonths(Long buId, String year, String monthPeriodStatus);

    List<AfmCalendarMonthDto> getCandidateMonths(Long bgId, Long buId, String year, String monthPeriodStatus);
}
