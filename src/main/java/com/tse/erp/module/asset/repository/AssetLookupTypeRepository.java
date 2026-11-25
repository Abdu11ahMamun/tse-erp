package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetLookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetLookupTypeRepository
        extends JpaRepository<AssetLookupType, Long> {

    List<AssetLookupType> findAllByOrderByIdDesc();

    // BU diye filter
    List<AssetLookupType> findByBuIdOrderByIdDesc(Long buId);

    // Duplicate check (BU-er moddhe unique)
    List<AssetLookupType> findByBuIdAndTypeNameIgnoreCase(
            Long buId, String typeName);

    List<AssetLookupType> findByBuIdAndTypeCodeIgnoreCase(
            Long buId, String typeCode);
}
