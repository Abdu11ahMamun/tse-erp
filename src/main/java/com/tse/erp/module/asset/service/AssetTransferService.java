package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetTransfer;

import java.util.List;

public interface AssetTransferService {

    List<AssetTransfer> getAll(Long buId, Long itemId);

    AssetTransfer getById(Long id);

    // Item-er current state (latest To), na thakle null
    AssetTransfer getCurrent(Long itemId);

    AssetTransfer create(AssetTransfer r);

    void delete(Long id);
}