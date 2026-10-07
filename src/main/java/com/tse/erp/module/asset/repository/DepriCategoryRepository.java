package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.DepriCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepriCategoryRepository
        extends JpaRepository<DepriCategory, Long> {

    List<DepriCategory> findAllByOrderByIdDesc();

    List<DepriCategory> findByBuIdOrderByIdDesc(Long buId);

    List<DepriCategory>
    findByBuIdAndDepriCategoryIgnoreCaseAndDepriMethodIdAndAssetTypeIdAndAssetOwnerId(
            Long buId, String depriCategory, Long depriMethodId,
            Long assetTypeId, Long assetOwnerId);
}
