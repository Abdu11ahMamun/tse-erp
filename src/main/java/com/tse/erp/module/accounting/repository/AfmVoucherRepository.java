package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AfmVoucherRepository extends JpaRepository<AfmVoucher, Long> {

    List<AfmVoucher> findAllByOrderByIdDesc();

    List<AfmVoucher> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmVoucher> findByBuIdOrderByIdDesc(Long buId);

    List<AfmVoucher> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmVoucher> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmVoucher> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmVoucher> findByBuIdAndVoucherTypeOrderByIdDesc(Long buId, String voucherType);

    Optional<AfmVoucher> findByBuIdAndVoucherNo(Long buId, String voucherNo);

    Optional<AfmVoucher> findByBuIdAndVoucherNoIgnoreCase(Long buId, String voucherNo);

    boolean existsByBuIdAndVoucherNoIgnoreCase(Long buId, String voucherNo);

    @Query("SELECT v FROM AfmVoucher v " +
           "WHERE (:bgId IS NULL OR v.bgId = :bgId) " +
           "  AND (:buId IS NULL OR v.buId = :buId) " +
           "  AND (:voucherType IS NULL OR LOWER(v.voucherType) = LOWER(:voucherType)) " +
           "  AND (:status IS NULL OR v.status = :status) " +
           "  AND (:fromDate IS NULL OR v.voucherDate >= :fromDate) " +
           "  AND (:toDate IS NULL OR v.voucherDate <= :toDate) " +
           "ORDER BY v.id DESC")
    List<AfmVoucher> searchVouchers(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("voucherType") String voucherType,
            @Param("status") Integer status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
