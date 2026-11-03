package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetRepair;

import java.util.List;

public interface AssetRepairService {

    List<AssetRepair> getAll(Long buId, Long itemId, boolean openOnly);

    AssetRepair getById(Long id);

    AssetRepair create(AssetRepair r);

    AssetRepair update(Long id, AssetRepair r);

    void delete(Long id);
}