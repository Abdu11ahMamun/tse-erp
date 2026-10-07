package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.entity.Supplier;

import java.util.List;

public interface SupplierService {

    List<Supplier> getAllSuppliers(Long buId);

    Supplier getSupplierById(Long id);

    Supplier createSupplier(Supplier supplier);

    Supplier updateSupplier(Long id, Supplier supplier);

    void deleteSupplier(Long id);
}
