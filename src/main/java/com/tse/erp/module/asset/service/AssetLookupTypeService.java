package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.dto.AssetLookupTypeRequestDto;
import com.tse.erp.module.asset.dto.AssetLookupTypeResponseDto;
import com.tse.erp.module.asset.dto.AssetLookupValueRowDto;
import com.tse.erp.module.asset.entity.AssetLookupType;

import java.util.List;

public interface AssetLookupTypeService {

    List<AssetLookupTypeResponseDto> getAllLookupTypes(Long buId);

    AssetLookupTypeResponseDto getLookupTypeById(Long id);

    AssetLookupTypeResponseDto createLookupType(AssetLookupTypeRequestDto request);

    AssetLookupTypeResponseDto updateLookupType(Long id, AssetLookupTypeRequestDto request);

    void deleteLookupType(Long id);

    List<AssetLookupValueRowDto> getLookupValues(Long typeId);

    AssetLookupType createLookupType(AssetLookupType lookupType);

    AssetLookupType updateLookupType(Long id, AssetLookupType lookupType);
}
