package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetItemReturn;

import java.util.List;

public interface AssetItemReturnService {

    List<AssetItemReturn> getAll(Long buId, Long emplId);

    AssetItemReturn getById(Long id);

    AssetItemReturn create(AssetItemReturn r);

    AssetItemReturn update(Long id, AssetItemReturn r);

    void delete(Long id);
}