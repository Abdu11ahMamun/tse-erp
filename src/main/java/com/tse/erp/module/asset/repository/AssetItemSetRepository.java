package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetItemSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetItemSetRepository extends JpaRepository<AssetItemSet, Long> {

    List<AssetItemSet> findByItemIdOrderBySortOrderAscIdAsc(Long itemId);

    void deleteByItemId(Long itemId);

    boolean existsByItemId(Long itemId);
}
