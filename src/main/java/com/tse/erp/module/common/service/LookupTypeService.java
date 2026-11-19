package com.tse.erp.module.common.service;

import com.tse.erp.module.common.dto.LookupTypeRequestDto;
import com.tse.erp.module.common.dto.LookupTypeResponseDto;
import com.tse.erp.module.common.dto.LookupValueRowDto;
import com.tse.erp.module.common.entity.LookupType;

import java.util.List;

public interface LookupTypeService {

    List<LookupTypeResponseDto> getAllLookupTypes(Long buId);

    LookupTypeResponseDto getLookupTypeById(Long id);

    LookupTypeResponseDto createLookupType(LookupTypeRequestDto request);

    LookupTypeResponseDto updateLookupType(Long id, LookupTypeRequestDto request);

    void deleteLookupType(Long id);

    List<LookupValueRowDto> getLookupValues(Long typeId);

    LookupType createLookupType(LookupType lookupType);

    LookupType updateLookupType(Long id, LookupType lookupType);
}
