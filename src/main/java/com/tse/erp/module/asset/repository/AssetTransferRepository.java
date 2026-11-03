package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetTransferRepository
        extends JpaRepository<AssetTransfer, Long> {

    List<AssetTransfer> findAllByOrderByIdDesc();

    List<AssetTransfer> findByBuIdOrderByIdDesc(Long buId);

    List<AssetTransfer> findByItemIdOrderByIdDesc(Long itemId);

    Optional<AssetTransfer> findFirstByItemIdAndStatusOrderByIdDesc(
            Long itemId, Integer status);
}