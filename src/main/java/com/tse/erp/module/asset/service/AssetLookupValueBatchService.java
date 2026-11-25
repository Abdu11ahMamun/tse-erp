package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.dto.AssetLookupValueBatchDto;

public interface AssetLookupValueBatchService {

    AssetLookupValueBatchDto getByType(Long typeId);

    AssetLookupValueBatchDto saveByType(Long typeId, AssetLookupValueBatchDto dto);
}
