package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetGroupRepository
        extends JpaRepository<AssetGroup, Long> {

    List<AssetGroup> findAllByOrderByIdDesc();

    List<AssetGroup> findByBuIdOrderByIdDesc(Long buId);

    List<AssetGroup> findByBuIdAndAssetGroupIgnoreCaseAndAssetOwnerId(
            Long buId, String assetGroup, Long assetOwnerId);

    boolean existsByAssetOwnerId(Long assetOwnerId);
}
