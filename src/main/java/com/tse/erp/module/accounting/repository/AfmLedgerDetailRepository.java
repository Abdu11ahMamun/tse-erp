package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmLedgerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmLedgerDetailRepository extends JpaRepository<AfmLedgerDetail, Long> {

    List<AfmLedgerDetail> findByVoucherIdOrderByIdAsc(Long voucherId);

    List<AfmLedgerDetail> findByVoucherIdAndStatusOrderByIdAsc(Long voucherId, Integer status);

    void deleteByVoucherId(Long voucherId);
}
