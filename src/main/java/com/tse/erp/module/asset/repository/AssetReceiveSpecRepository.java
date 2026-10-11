package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetReceiveSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetReceiveSpecRepository
        extends JpaRepository<AssetReceiveSpec, Long> {

    List<AssetReceiveSpec> findByAssetReceiveIdAndStatusOrderByIdAsc(
            Long assetReceiveId, Integer status);

    List<AssetReceiveSpec> findByAssetReceiveIdOrderByIdAsc(
            Long assetReceiveId);
}
