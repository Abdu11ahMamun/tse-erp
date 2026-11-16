package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AfmLedgerRepository extends JpaRepository<AfmLedger, Long> {

    List<AfmLedger> findAllByOrderByIdDesc();

    List<AfmLedger> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmLedger> findByBuIdOrderByIdDesc(Long buId);

    List<AfmLedger> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmLedger> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmLedger> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmLedger> findByBuIdAndVoucherTypeOrderByIdDesc(Long buId, String voucherType);

    Optional<AfmLedger> findByBuIdAndVoucherNo(Long buId, String voucherNo);

    Optional<AfmLedger> findByBuIdAndVoucherNoIgnoreCase(Long buId, String voucherNo);

    boolean existsByBuIdAndVoucherNoIgnoreCase(Long buId, String voucherNo);

    Optional<AfmLedger> findByVoucherSourceAndVoucherSourceLink(String voucherSource, String voucherSourceLink);

    Optional<AfmLedger> findByBuIdAndVoucherSourceAndVoucherSourceLink(Long buId, String voucherSource, String voucherSourceLink);

    @Query("SELECT l FROM AfmLedger l " +
           "WHERE (:bgId IS NULL OR l.bgId = :bgId) " +
           "  AND (:buId IS NULL OR l.buId = :buId) " +
           "  AND (:voucherType IS NULL OR LOWER(l.voucherType) = LOWER(:voucherType)) " +
           "  AND (:status IS NULL OR l.status = :status) " +
           "  AND (:fromDate IS NULL OR l.voucherDate >= :fromDate) " +
           "  AND (:toDate IS NULL OR l.voucherDate <= :toDate) " +
           "ORDER BY l.id DESC")
    List<AfmLedger> searchLedgers(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("voucherType") String voucherType,
            @Param("status") Integer status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
