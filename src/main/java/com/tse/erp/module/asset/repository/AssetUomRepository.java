package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetUom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetUomRepository
        extends JpaRepository<AssetUom, Long> {

    List<AssetUom> findAllByOrderByIdDesc();

    List<AssetUom> findByBuIdOrderByIdDesc(Long buId);

    List<AssetUom> findByBuIdAndUnitNameIgnoreCase(
            Long buId, String unitName);
}
