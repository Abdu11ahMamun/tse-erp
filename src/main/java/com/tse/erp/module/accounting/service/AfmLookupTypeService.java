package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmLookupTypeRequestDto;
import com.tse.erp.module.accounting.dto.AfmLookupTypeResponseDto;
import com.tse.erp.module.accounting.dto.AfmLookupValueDto;

import java.util.List;

public interface AfmLookupTypeService {

    List<AfmLookupTypeResponseDto> getAllLookupTypes(Long bgId, Long buId, Integer status);

    AfmLookupTypeResponseDto getLookupTypeById(Long id);

    AfmLookupTypeResponseDto getLookupTypeByCode(Long buId, String typeCode);

    AfmLookupTypeResponseDto getLookupTypeByCode(String typeCode, Long buId);

    List<AfmLookupValueDto> getLookupValues(Long typeId);

    List<AfmLookupValueDto> getLookupValuesByCode(Long buId, String typeCode, Integer status);

    List<AfmLookupValueDto> getLookupValuesByCode(String typeCode, Long buId, Integer status);

    List<AfmLookupValueDto> getLookupValuesByCode(String typeCode);

    AfmLookupTypeResponseDto createLookupType(AfmLookupTypeRequestDto request);

    AfmLookupTypeResponseDto updateLookupType(Long id, AfmLookupTypeRequestDto request);

    void deleteLookupType(Long id);

    AfmLookupValueDto addLookupValue(Long typeId, AfmLookupValueDto detailDto);

    AfmLookupValueDto updateLookupValue(Long typeId, Long valueId, AfmLookupValueDto detailDto);

    void deleteLookupValue(Long typeId, Long valueId);
}
