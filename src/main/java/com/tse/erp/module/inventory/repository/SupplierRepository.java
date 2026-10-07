package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findAllByOrderByIdDesc();

    List<Supplier> findByBuIdOrderByIdDesc(Long buId);

    List<Supplier> findByBuIdAndSupplierNameIgnoreCase(
            Long buId, String supplierName);

    List<Supplier> findByBuIdAndSupplierCodeIgnoreCase(
            Long buId, String supplierCode);

    List<Supplier> findByBuIdAndTaxIdIgnoreCase(Long buId, String taxId);
}
