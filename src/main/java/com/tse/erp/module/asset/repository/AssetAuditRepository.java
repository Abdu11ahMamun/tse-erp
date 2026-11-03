package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.AssetAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAuditRepository
        extends JpaRepository<AssetAudit, Long> {

    List<AssetAudit> findAllByOrderByIdDesc();

    List<AssetAudit> findByBuIdOrderByIdDesc(Long buId);

    List<AssetAudit> findByDisposalStatusOrderByIdDesc(String disposalStatus);

    List<AssetAudit> findByBuIdAndDisposalStatusOrderByIdDesc(
            Long buId, String disposalStatus);

    List<AssetAudit> findByItemId(Long itemId);
}