package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetItemRepository
        extends JpaRepository<AssetItem, Long> {

    List<AssetItem> findAllByOrderByIdDesc();

    List<AssetItem> findByBuIdOrderByIdDesc(Long buId);

    List<AssetItem> findByItemGroupIdAndItemNameIgnoreCase(
            Long itemGroupId, String itemName);

    boolean existsByItemGroupId(Long itemGroupId);

    boolean existsByAssetDepriCatId(Long assetDepriCatId);

    boolean existsByUomId(Long uomId);
}
