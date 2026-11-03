package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetAssign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAssignRepository
        extends JpaRepository<AssetAssign, Long> {

    List<AssetAssign> findAllByOrderByIdDesc();

    List<AssetAssign> findByBuIdOrderByIdDesc(Long buId);

    List<AssetAssign> findByEmplIdOrderByIdDesc(Long emplId);

    List<AssetAssign> findByBuIdAndEmplIdOrderByIdDesc(Long buId, Long emplId);

    // Open (return hoy nai) assignment
    List<AssetAssign> findByItemIdAndActualReturnDateIsNull(Long itemId);
}