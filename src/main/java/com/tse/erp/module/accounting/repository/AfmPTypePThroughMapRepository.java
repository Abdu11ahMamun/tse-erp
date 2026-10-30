package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmPTypePThroughMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AfmPTypePThroughMapRepository extends JpaRepository<AfmPTypePThroughMap, Long> {

    List<AfmPTypePThroughMap> findAllByOrderByIdDesc();

    List<AfmPTypePThroughMap> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmPTypePThroughMap> findByBuIdOrderByIdDesc(Long buId);

    List<AfmPTypePThroughMap> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmPTypePThroughMap> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmPTypePThroughMap> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmPTypePThroughMap> findByBuIdAndPaymentTypeIdOrderByIdDesc(Long buId, Long paymentTypeId);

    List<AfmPTypePThroughMap> findByBuIdAndPaymentTypeIdAndStatusOrderByIdDesc(Long buId, Long paymentTypeId, Integer status);

    Optional<AfmPTypePThroughMap> findByBuIdAndPaymentTypeIdAndPaymentThroughId(Long buId, Long paymentTypeId, Long paymentThroughId);

    boolean existsByBuIdAndPaymentTypeIdAndPaymentThroughId(Long buId, Long paymentTypeId, Long paymentThroughId);

    List<AfmPTypePThroughMap> findByPaymentTypeId(Long paymentTypeId);

    List<AfmPTypePThroughMap> findByPaymentThroughId(Long paymentThroughId);

    boolean existsByPaymentTypeId(Long paymentTypeId);

    boolean existsByPaymentThroughId(Long paymentThroughId);

    boolean existsByPaymentTypeIdOrPaymentThroughId(Long paymentTypeId, Long paymentThroughId);

    @Query("SELECT DISTINCT m.paymentTypeId FROM AfmPTypePThroughMap m WHERE m.paymentTypeId IN :ids")
    Set<Long> findReferencedPaymentTypeIds(@Param("ids") Collection<Long> ids);

    @Query("SELECT DISTINCT m.paymentThroughId FROM AfmPTypePThroughMap m WHERE m.paymentThroughId IN :ids")
    Set<Long> findReferencedPaymentThroughIds(@Param("ids") Collection<Long> ids);
}
