package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetUom;

import java.util.List;

public interface AssetUomService {

    List<AssetUom> getAllAssetUoms(Long buId);

    AssetUom getAssetUomById(Long id);

    AssetUom createAssetUom(AssetUom assetUom);

    AssetUom updateAssetUom(Long id, AssetUom assetUom);

    void deleteAssetUom(Long id);
}
