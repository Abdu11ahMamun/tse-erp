package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemStoreRepository extends JpaRepository<InvItemStore, Long> {

    List<InvItemStore> findByBuIdOrderByStoreNameAsc(Long buId);

    List<InvItemStore> findByBuIdAndStatusOrderByStoreNameAsc(Long buId, Integer status);

    boolean existsByBuIdAndStoreCodeIgnoreCase(Long buId, String storeCode);

    boolean existsByBuIdAndStoreNameIgnoreCase(Long buId, String storeName);

    Optional<InvItemStore> findByBuIdAndStoreCodeIgnoreCase(Long buId, String storeCode);

    Optional<InvItemStore> findByBuIdAndStoreNameIgnoreCase(Long buId, String storeName);

    long countByBuId(Long buId);

    List<InvItemStore> findByBuIdAndIsDefault(Long buId, Integer isDefault);

    @Modifying
    @Query("UPDATE InvItemStore s SET s.isDefault = 0 WHERE s.buId = :buId AND (:excludeId IS NULL OR s.id <> :excludeId)")
    void resetOtherDefaults(@Param("buId") Long buId, @Param("excludeId") Long excludeId);

    @Query("SELECT s FROM InvItemStore s WHERE " +
           "(:bgId IS NULL OR s.bgId = :bgId) AND " +
           "(:buId IS NULL OR s.buId = :buId) AND " +
           "(:storeName IS NULL OR LOWER(s.storeName) LIKE LOWER(CONCAT('%', :storeName, '%'))) AND " +
           "(:storeCode IS NULL OR LOWER(s.storeCode) LIKE LOWER(CONCAT('%', :storeCode, '%'))) AND " +
           "(:isDefault IS NULL OR s.isDefault = :isDefault) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.id DESC")
    List<InvItemStore> searchItemStores(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("storeName") String storeName,
            @Param("storeCode") String storeCode,
            @Param("isDefault") Integer isDefault,
            @Param("status") Integer status);
}

