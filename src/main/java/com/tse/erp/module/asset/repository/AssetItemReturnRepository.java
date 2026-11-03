package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetItemReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetItemReturnRepository
        extends JpaRepository<AssetItemReturn, Long> {

    List<AssetItemReturn> findAllByOrderByIdDesc();

    List<AssetItemReturn> findByBuIdOrderByIdDesc(Long buId);

    List<AssetItemReturn> findByEmplIdOrderByIdDesc(Long emplId);

    List<AssetItemReturn> findByBuIdAndEmplIdOrderByIdDesc(Long buId, Long emplId);

    List<AssetItemReturn> findByAssignId(Long assignId);

    boolean existsByAssignId(Long assignId);
}