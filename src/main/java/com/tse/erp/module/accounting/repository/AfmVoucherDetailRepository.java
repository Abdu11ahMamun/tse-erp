package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmVoucherDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmVoucherDetailRepository extends JpaRepository<AfmVoucherDetail, Long> {

    List<AfmVoucherDetail> findByVoucherIdOrderByIdAsc(Long voucherId);

    List<AfmVoucherDetail> findByVoucherIdAndStatusOrderByIdAsc(Long voucherId, Integer status);

    void deleteByVoucherId(Long voucherId);
}
