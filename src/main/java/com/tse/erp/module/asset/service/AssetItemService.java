package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetItem;

import java.util.List;

public interface AssetItemService {

    List<AssetItem> getAllAssetItems(Long buId);

    AssetItem getAssetItemById(Long id);

    AssetItem createAssetItem(AssetItem assetItem);

    AssetItem updateAssetItem(Long id, AssetItem assetItem);

    void deleteAssetItem(Long id);
}
