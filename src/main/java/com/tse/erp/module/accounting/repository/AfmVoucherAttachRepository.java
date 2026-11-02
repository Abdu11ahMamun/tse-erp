package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmVoucherAttach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmVoucherAttachRepository extends JpaRepository<AfmVoucherAttach, Long> {

    List<AfmVoucherAttach> findByVoucherIdOrderByIdAsc(Long voucherId);

    List<AfmVoucherAttach> findByVoucherIdAndStatusOrderByIdAsc(Long voucherId, Integer status);

    void deleteByVoucherId(Long voucherId);
}
