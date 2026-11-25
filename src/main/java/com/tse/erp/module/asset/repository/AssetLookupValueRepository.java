package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetLookupValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetLookupValueRepository
        extends JpaRepository<AssetLookupValue, Long> {

    List<AssetLookupValue> findAllByOrderByIdDesc();

    List<AssetLookupValue> findByBuIdOrderByIdDesc(Long buId);

    List<AssetLookupValue> findByTypeIdOrderBySortOrderAscIdAsc(Long typeId);

    List<AssetLookupValue> findByBuIdAndTypeIdOrderBySortOrderAscIdAsc(
            Long buId, Long typeId);

    // Duplicate check (Type-er moddhe unique)
    List<AssetLookupValue> findByTypeIdAndValueIgnoreCase(
            Long typeId, String value);

    List<AssetLookupValue> findByTypeIdAndValueCodeIgnoreCase(
            Long typeId, String valueCode);

    List<AssetLookupValue> findByTypeIdOrderByIdAsc(Long typeId);

    boolean existsByTypeId(Long typeId);

    void deleteByTypeId(Long typeId);
}
