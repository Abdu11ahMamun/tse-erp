package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmCustomerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfmCustomerInfoRepository extends JpaRepository<AfmCustomerInfo, Long> {

    List<AfmCustomerInfo> findAllByOrderByIdDesc();

    List<AfmCustomerInfo> findByBuIdOrderByIdDesc(Long buId);

    List<AfmCustomerInfo> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    boolean existsByBuIdAndCustomerCodeIgnoreCase(Long buId, String customerCode);

    Optional<AfmCustomerInfo> findFirstByBuIdOrderByIdDesc(Long buId);

    long countByBuId(Long buId);

    @Query("SELECT c FROM AfmCustomerInfo c WHERE " +
           "(:bgId IS NULL OR c.bgId = :bgId) AND " +
           "(:buId IS NULL OR c.buId = :buId) AND " +
           "(:customerType IS NULL OR LOWER(c.customerType) = LOWER(:customerType)) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.contactPerson) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY c.id DESC")
    List<AfmCustomerInfo> searchCustomers(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("query") String query,
            @Param("customerType") String customerType,
            @Param("status") Integer status
    );
}

