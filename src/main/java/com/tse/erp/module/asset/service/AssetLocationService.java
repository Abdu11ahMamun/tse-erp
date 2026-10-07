package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetLocation;

import java.util.List;

public interface AssetLocationService {

    List<AssetLocation> getAllAssetLocations(Long buId);

    AssetLocation getAssetLocationById(Long id);

    AssetLocation createAssetLocation(AssetLocation assetLocation);

    AssetLocation updateAssetLocation(Long id, AssetLocation assetLocation);

    void deleteAssetLocation(Long id);
}
