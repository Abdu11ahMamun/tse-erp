package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmVTypePTypeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AfmVTypePTypeMapRepository extends JpaRepository<AfmVTypePTypeMap, Long> {

    List<AfmVTypePTypeMap> findAllByOrderByIdDesc();

    List<AfmVTypePTypeMap> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmVTypePTypeMap> findByBuIdOrderByIdDesc(Long buId);

    List<AfmVTypePTypeMap> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmVTypePTypeMap> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmVTypePTypeMap> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmVTypePTypeMap> findByBuIdAndVoucherTypeIdOrderByIdDesc(Long buId, Long voucherTypeId);

    List<AfmVTypePTypeMap> findByBuIdAndVoucherTypeIdAndStatusOrderByIdDesc(Long buId, Long voucherTypeId, Integer status);

    Optional<AfmVTypePTypeMap> findByBuIdAndVoucherTypeIdAndPaymentTypeId(Long buId, Long voucherTypeId, Long paymentTypeId);

    boolean existsByBuIdAndVoucherTypeIdAndPaymentTypeId(Long buId, Long voucherTypeId, Long paymentTypeId);

    List<AfmVTypePTypeMap> findByVoucherTypeId(Long voucherTypeId);

    List<AfmVTypePTypeMap> findByPaymentTypeId(Long paymentTypeId);

    boolean existsByVoucherTypeId(Long voucherTypeId);

    boolean existsByPaymentTypeId(Long paymentTypeId);

    boolean existsByVoucherTypeIdOrPaymentTypeId(Long voucherTypeId, Long paymentTypeId);

    @Query("SELECT DISTINCT m.voucherTypeId FROM AfmVTypePTypeMap m WHERE m.voucherTypeId IN :ids")
    Set<Long> findReferencedVoucherTypeIds(@Param("ids") Collection<Long> ids);

    @Query("SELECT DISTINCT m.paymentTypeId FROM AfmVTypePTypeMap m WHERE m.paymentTypeId IN :ids")
    Set<Long> findReferencedPaymentTypeIds(@Param("ids") Collection<Long> ids);
}