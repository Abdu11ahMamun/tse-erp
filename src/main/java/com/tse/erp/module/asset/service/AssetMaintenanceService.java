package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetMaintenance;

import java.util.List;

public interface AssetMaintenanceService {

    List<AssetMaintenance> getAll(Long buId, Long itemId);

    AssetMaintenance getById(Long id);

    AssetMaintenance create(AssetMaintenance r);

    AssetMaintenance update(Long id, AssetMaintenance r);

    void delete(Long id);
}