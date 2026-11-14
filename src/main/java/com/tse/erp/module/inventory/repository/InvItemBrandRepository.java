package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemBrandRepository extends JpaRepository<InvItemBrand, Long> {

    List<InvItemBrand> findByBuIdOrderByBrandNameAsc(Long buId);

    List<InvItemBrand> findByBuIdAndStatusOrderByBrandNameAsc(Long buId, Integer status);

    boolean existsByBuIdAndBrandCodeIgnoreCase(Long buId, String brandCode);

    boolean existsByBuIdAndBrandNameIgnoreCase(Long buId, String brandName);

    Optional<InvItemBrand> findByBuIdAndBrandCodeIgnoreCase(Long buId, String brandCode);

    Optional<InvItemBrand> findByBuIdAndBrandNameIgnoreCase(Long buId, String brandName);

    long countByBuId(Long buId);

    @Query("SELECT b FROM InvItemBrand b WHERE " +
           "(:bgId IS NULL OR b.bgId = :bgId) AND " +
           "(:buId IS NULL OR b.buId = :buId) AND " +
           "(:brandName IS NULL OR LOWER(b.brandName) LIKE LOWER(CONCAT('%', :brandName, '%'))) AND " +
           "(:brandCode IS NULL OR LOWER(b.brandCode) LIKE LOWER(CONCAT('%', :brandCode, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) " +
           "ORDER BY b.id DESC")
    List<InvItemBrand> searchItemBrands(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("brandName") String brandName,
            @Param("brandCode") String brandCode,
            @Param("status") Integer status);
}

