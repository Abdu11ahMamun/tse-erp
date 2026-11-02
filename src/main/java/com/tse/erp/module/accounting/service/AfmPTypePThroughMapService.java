package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmPTypePThroughMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmPTypePThroughMapResponseDto;

import java.util.List;

public interface AfmPTypePThroughMapService {

    List<AfmPTypePThroughMapResponseDto> getAllMappings(Long bgId, Long buId, Integer status);

    AfmPTypePThroughMapResponseDto getMappingById(Long id);

    List<AfmPTypePThroughMapResponseDto> getMappingsByPaymentType(Long buId, Long paymentTypeId, Integer status);

    AfmPTypePThroughMapResponseDto createMapping(AfmPTypePThroughMapRequestDto request);

    AfmPTypePThroughMapResponseDto updateMapping(Long id, AfmPTypePThroughMapRequestDto request);

    void deleteMapping(Long id);
}
