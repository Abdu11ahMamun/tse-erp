package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemRepository extends JpaRepository<InvItem, Long> {

    List<InvItem> findByBuIdOrderByItemNameAsc(Long buId);

    List<InvItem> findByBuIdAndStatusOrderByItemNameAsc(Long buId, Integer status);

    boolean existsByBuIdAndItemCodeIgnoreCase(Long buId, String itemCode);

    boolean existsByBuIdAndItemNameIgnoreCase(Long buId, String itemName);

    Optional<InvItem> findByBuIdAndItemCodeIgnoreCase(Long buId, String itemCode);

    Optional<InvItem> findByBuIdAndItemNameIgnoreCase(Long buId, String itemName);

    long countByBuId(Long buId);

    @Query("SELECT i FROM InvItem i WHERE " +
           "(:bgId IS NULL OR i.bgId = :bgId) AND " +
           "(:buId IS NULL OR i.buId = :buId) AND " +
           "(:categoryId IS NULL OR i.categoryId = :categoryId) AND " +
           "(:brandId IS NULL OR i.brandId = :brandId) AND " +
           "(:uomId IS NULL OR i.uomId = :uomId) AND " +
           "(:storeId IS NULL OR :storeId = '' OR CONCAT(',', COALESCE(i.storeId, ''), ',') LIKE CONCAT('%,', :storeId, ',%')) AND " +
           "(:itemName IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND " +
           "(:itemCode IS NULL OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%'))) AND " +
           "(:status IS NULL OR i.status = :status) " +
           "ORDER BY i.id DESC")
    List<InvItem> searchItems(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("uomId") Long uomId,
            @Param("storeId") String storeId,
            @Param("itemName") String itemName,
            @Param("itemCode") String itemCode,
            @Param("status") Integer status);
}

