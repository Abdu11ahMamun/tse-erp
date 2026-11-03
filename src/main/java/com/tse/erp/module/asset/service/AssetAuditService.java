package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetAudit;

import java.util.List;

public interface AssetAuditService {

    List<AssetAudit> getAll(Long buId, String disposalStatus);

    AssetAudit getById(Long id);

    AssetAudit create(AssetAudit r);

    AssetAudit update(Long id, AssetAudit r);

    AssetAudit submit(Long id);

    AssetAudit approve(Long id, Long approvedBy);

    AssetAudit reject(Long id);

    AssetAudit cancel(Long id);

    AssetAudit complete(Long id);

    void delete(Long id);
}