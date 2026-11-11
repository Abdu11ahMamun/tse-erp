package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemAttributeDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvItemAttributeDtlsRepository extends JpaRepository<InvItemAttributeDtls, Long> {

    List<InvItemAttributeDtls> findByItemIdOrderByIdAsc(Long itemId);

    List<InvItemAttributeDtls> findByItemIdAndStatusOrderByIdAsc(Long itemId, Integer status);

    void deleteByItemId(Long itemId);
}

