package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmVTypePTypeMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmVTypePTypeMapResponseDto;

import java.util.List;

public interface AfmVTypePTypeMapService {

    List<AfmVTypePTypeMapResponseDto> getAllMappings(Long bgId, Long buId, Integer status);

    AfmVTypePTypeMapResponseDto getMappingById(Long id);

    List<AfmVTypePTypeMapResponseDto> getMappingsByVoucherType(Long buId, Long voucherTypeId, Integer status);

    AfmVTypePTypeMapResponseDto createMapping(AfmVTypePTypeMapRequestDto request);

    AfmVTypePTypeMapResponseDto updateMapping(Long id, AfmVTypePTypeMapRequestDto request);

    void deleteMapping(Long id);
}