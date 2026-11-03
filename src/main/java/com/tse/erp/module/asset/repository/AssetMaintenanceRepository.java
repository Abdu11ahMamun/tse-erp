package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetMaintenanceRepository
        extends JpaRepository<AssetMaintenance, Long> {

    List<AssetMaintenance> findAllByOrderByIdDesc();

    List<AssetMaintenance> findByBuIdOrderByIdDesc(Long buId);

    List<AssetMaintenance> findByItemIdOrderByIdDesc(Long itemId);

    List<AssetMaintenance> findByBuIdAndItemIdOrderByIdDesc(Long buId, Long itemId);

    List<AssetMaintenance> findByItemIdAndMaintenanceType(
            Long itemId, String maintenanceType);
}