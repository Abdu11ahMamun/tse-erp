package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetLookupValue;
import java.util.List;

public interface AssetLookupValueService {

    List<AssetLookupValue> getAllLookupValues(Long buId, Long typeId);

    AssetLookupValue getLookupValueById(Long id);

    AssetLookupValue createLookupValue(AssetLookupValue lookupValue);

    AssetLookupValue updateLookupValue(Long id, AssetLookupValue lookupValue);

    void deleteLookupValue(Long id);
}
