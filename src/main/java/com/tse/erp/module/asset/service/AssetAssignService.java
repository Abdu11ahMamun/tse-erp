package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.AssetAssign;

import java.time.LocalDate;
import java.util.List;

public interface AssetAssignService {

    List<AssetAssign> getAll(Long buId, Long emplId, boolean openOnly);

    AssetAssign getById(Long id);

    AssetAssign create(AssetAssign r);

    AssetAssign update(Long id, AssetAssign r);

    AssetAssign markReturned(Long id, LocalDate returnDate);

    void delete(Long id);
}