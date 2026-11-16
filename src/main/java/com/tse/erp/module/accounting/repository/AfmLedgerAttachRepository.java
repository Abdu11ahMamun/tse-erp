package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmLedgerAttach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmLedgerAttachRepository extends JpaRepository<AfmLedgerAttach, Long> {

    List<AfmLedgerAttach> findByVoucherIdOrderByIdAsc(Long voucherId);

    List<AfmLedgerAttach> findByVoucherIdAndStatusOrderByIdAsc(Long voucherId, Integer status);

    void deleteByVoucherId(Long voucherId);
}
