package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemWarehouseRepository extends JpaRepository<InvItemWarehouse, Long> {

    List<InvItemWarehouse> findByBuIdOrderByWarehouseNameAsc(Long buId);

    List<InvItemWarehouse> findByBuIdAndStatusOrderByWarehouseNameAsc(Long buId, Integer status);

    List<InvItemWarehouse> findByStoreIdContaining(String storeId);

    boolean existsByBuIdAndWarehouseCodeIgnoreCase(Long buId, String warehouseCode);

    boolean existsByBuIdAndWarehouseNameIgnoreCase(Long buId, String warehouseName);

    Optional<InvItemWarehouse> findByBuIdAndWarehouseCodeIgnoreCase(Long buId, String warehouseCode);

    Optional<InvItemWarehouse> findByBuIdAndWarehouseNameIgnoreCase(Long buId, String warehouseName);

    long countByBuId(Long buId);

    @Modifying
    @Query("UPDATE InvItemWarehouse w SET w.isDefault = 0 WHERE w.buId = :buId AND (:excludeId IS NULL OR w.id <> :excludeId)")
    void resetOtherDefaults(@Param("buId") Long buId, @Param("excludeId") Long excludeId);

    @Query("SELECT w FROM InvItemWarehouse w WHERE " +
           "(:bgId IS NULL OR w.bgId = :bgId) AND " +
           "(:buId IS NULL OR w.buId = :buId) AND " +
           "(:warehouseName IS NULL OR LOWER(w.warehouseName) LIKE LOWER(CONCAT('%', :warehouseName, '%'))) AND " +
           "(:warehouseCode IS NULL OR LOWER(w.warehouseCode) LIKE LOWER(CONCAT('%', :warehouseCode, '%'))) AND " +
           "(:storeId IS NULL OR :storeId = '' OR CONCAT(',', COALESCE(w.storeId, ''), ',') LIKE CONCAT('%,', :storeId, ',%')) AND " +
           "(:isDefault IS NULL OR w.isDefault = :isDefault) AND " +
           "(:status IS NULL OR w.status = :status) " +
           "ORDER BY w.id DESC")
    List<InvItemWarehouse> searchItemWarehouses(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("warehouseName") String warehouseName,
            @Param("warehouseCode") String warehouseCode,
            @Param("storeId") String storeId,
            @Param("isDefault") Integer isDefault,
            @Param("status") Integer status);
}

