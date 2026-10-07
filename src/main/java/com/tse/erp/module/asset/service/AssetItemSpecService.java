package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetItemSpec;

import java.util.List;

public interface AssetItemSpecService {

    List<AssetItemSpec> getAllAssetItemSpecs(
            Long buId, Long itemNameId);

    AssetItemSpec getAssetItemSpecById(Long id);

    AssetItemSpec createAssetItemSpec(AssetItemSpec assetItemSpec);

    AssetItemSpec updateAssetItemSpec(
            Long id, AssetItemSpec assetItemSpec);

    void deleteAssetItemSpec(Long id);
}
