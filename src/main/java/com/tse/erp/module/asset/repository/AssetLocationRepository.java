package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetLocationRepository
        extends JpaRepository<AssetLocation, Long> {

    List<AssetLocation> findAllByOrderByIdDesc();

    List<AssetLocation> findByBuIdOrderByIdDesc(Long buId);

    List<AssetLocation>
    findByCustodianIdAndSubCustodianIdAndLocationNameIgnoreCase(
            Long custodianId, Long subCustodianId, String locationName);

    List<AssetLocation> findByBuIdAndLocationCodeIgnoreCase(
            Long buId, String locationCode);

    boolean existsByCustodianId(Long custodianId);

    boolean existsByDeptId(Long deptId);
}
