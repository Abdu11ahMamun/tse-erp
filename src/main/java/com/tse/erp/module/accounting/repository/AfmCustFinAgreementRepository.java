package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmCustFinAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfmCustFinAgreementRepository extends JpaRepository<AfmCustFinAgreement, Long> {

    List<AfmCustFinAgreement> findByCustomerIdOrderByIdDesc(Long customerId);

    List<AfmCustFinAgreement> findByCustomerIdAndStatus(Long customerId, Integer status);

    Optional<AfmCustFinAgreement> findFirstByCustomerIdAndStatusOrderByIdDesc(Long customerId, Integer status);
}

