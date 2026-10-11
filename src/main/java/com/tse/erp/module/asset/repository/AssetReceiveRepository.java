package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetReceive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetReceiveRepository
        extends JpaRepository<AssetReceive, Long> {

    List<AssetReceive> findAllByOrderByIdDesc();

    List<AssetReceive> findByBuIdOrderByIdDesc(Long buId);

    List<AssetReceive> findByBuIdAndAssetNoIgnoreCase(
            Long buId, String assetNo);
}
