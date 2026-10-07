package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetGroup;

import java.util.List;

public interface AssetGroupService {

    List<AssetGroup> getAllAssetGroups(Long buId);

    AssetGroup getAssetGroupById(Long id);

    AssetGroup createAssetGroup(AssetGroup assetGroup);

    AssetGroup updateAssetGroup(Long id, AssetGroup assetGroup);

    void deleteAssetGroup(Long id);
}
