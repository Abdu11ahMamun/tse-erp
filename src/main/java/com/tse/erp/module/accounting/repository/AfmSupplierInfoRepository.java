package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmSupplierInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfmSupplierInfoRepository extends JpaRepository<AfmSupplierInfo, Long> {

    List<AfmSupplierInfo> findAllByOrderByIdDesc();

    List<AfmSupplierInfo> findByBuIdOrderByIdDesc(Long buId);

    List<AfmSupplierInfo> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    boolean existsByBuIdAndSupplierCodeIgnoreCase(Long buId, String supplierCode);

    Optional<AfmSupplierInfo> findFirstByBuIdOrderByIdDesc(Long buId);

    long countByBuId(Long buId);

    @Query("SELECT s FROM AfmSupplierInfo s WHERE " +
           "(:bgId IS NULL OR s.bgId = :bgId) AND " +
           "(:buId IS NULL OR s.buId = :buId) AND " +
           "(:supplierType IS NULL OR LOWER(s.supplierType) = LOWER(:supplierType)) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(s.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY s.id DESC")
    List<AfmSupplierInfo> searchSuppliers(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("query") String query,
            @Param("supplierType") String supplierType,
            @Param("status") Integer status
    );
}

