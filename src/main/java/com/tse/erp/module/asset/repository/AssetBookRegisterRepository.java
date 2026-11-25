package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetBookRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetBookRegisterRepository extends JpaRepository<AssetBookRegister, Long> {

    List<AssetBookRegister> findAllByOrderByIdDesc();

    List<AssetBookRegister> findByBuIdOrderByIdDesc(Long buId);

    List<AssetBookRegister> findByBuIdAndAssetNoIgnoreCase(Long buId, String assetNo);

    boolean existsByAssetItemId(Long assetItemId);

    boolean existsByAssetMajorCategoryId(Long assetMajorCategoryId);

    boolean existsByAssetOwnerId(Long assetOwnerId);

    boolean existsByAllocationRefId(Long allocationRefId);

    boolean existsBySupplierId(Long supplierId);

    boolean existsByDepriciationMethodId(Long depriciationMethodId);
}
