package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemCategoryRepository extends JpaRepository<InvItemCategory, Long> {

    List<InvItemCategory> findByBuIdOrderByCategoryNameAsc(Long buId);

    List<InvItemCategory> findByBuIdAndStatusOrderByCategoryNameAsc(Long buId, Integer status);

    boolean existsByBuIdAndCategoryCodeIgnoreCase(Long buId, String categoryCode);

    boolean existsByBuIdAndCategoryNameIgnoreCase(Long buId, String categoryName);

    Optional<InvItemCategory> findByBuIdAndCategoryCodeIgnoreCase(Long buId, String categoryCode);

    Optional<InvItemCategory> findByBuIdAndCategoryNameIgnoreCase(Long buId, String categoryName);

    long countByBuId(Long buId);

    @Query("SELECT c FROM InvItemCategory c WHERE " +
           "(:bgId IS NULL OR c.bgId = :bgId) AND " +
           "(:buId IS NULL OR c.buId = :buId) AND " +
           "(:categoryName IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :categoryName, '%'))) AND " +
           "(:categoryCode IS NULL OR LOWER(c.categoryCode) LIKE LOWER(CONCAT('%', :categoryCode, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) " +
           "ORDER BY c.id DESC")
    List<InvItemCategory> searchItemCategories(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("categoryName") String categoryName,
            @Param("categoryCode") String categoryCode,
            @Param("status") Integer status);
}

