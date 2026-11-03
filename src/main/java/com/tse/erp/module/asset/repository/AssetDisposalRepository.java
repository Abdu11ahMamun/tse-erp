package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetDisposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetDisposalRepository
        extends JpaRepository<AssetDisposal, Long> {

    List<AssetDisposal> findAllByOrderByIdDesc();

    List<AssetDisposal> findByBuIdOrderByIdDesc(Long buId);

    List<AssetDisposal> findByDisposalStatusOrderByIdDesc(String disposalStatus);

    List<AssetDisposal> findByBuIdAndDisposalStatusOrderByIdDesc(
            Long buId, String disposalStatus);

    List<AssetDisposal> findByItemId(Long itemId);
}