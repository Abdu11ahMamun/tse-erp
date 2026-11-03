package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetDisposal;

import java.util.List;

public interface AssetDisposalService {

    List<AssetDisposal> getAll(Long buId, String disposalStatus);

    AssetDisposal getById(Long id);

    AssetDisposal create(AssetDisposal r);

    AssetDisposal update(Long id, AssetDisposal r);

    AssetDisposal submit(Long id);

    AssetDisposal approve(Long id, Long approvedBy);

    AssetDisposal reject(Long id);

    AssetDisposal cancel(Long id);

    AssetDisposal complete(Long id);

    void delete(Long id);
}