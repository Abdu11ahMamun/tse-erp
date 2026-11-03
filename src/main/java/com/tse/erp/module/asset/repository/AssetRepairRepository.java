package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepairRepository
        extends JpaRepository<AssetRepair, Long> {

    List<AssetRepair> findAllByOrderByIdDesc();

    List<AssetRepair> findByBuIdOrderByIdDesc(Long buId);

    List<AssetRepair> findByItemIdOrderByIdDesc(Long itemId);

    List<AssetRepair> findByBuIdAndItemIdOrderByIdDesc(Long buId, Long itemId);

    List<AssetRepair> findByItemIdAndDeliveryDateIsNull(Long itemId);
}