package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetItemSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetItemSpecRepository
        extends JpaRepository<AssetItemSpec, Long> {

    List<AssetItemSpec> findAllByOrderByIdDesc();

    List<AssetItemSpec> findByBuIdOrderByIdDesc(Long buId);

    List<AssetItemSpec> findByItemNameIdOrderBySortOrderAscIdAsc(
            Long itemNameId);

    List<AssetItemSpec> findByItemNameIdAndSpecNameIgnoreCase(
            Long itemNameId, String specName);

    boolean existsByItemNameId(Long itemNameId);
}
