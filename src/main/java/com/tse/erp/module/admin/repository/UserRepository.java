package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByOrderByIdDesc();

    List<User> findByBuIdOrderByIdDesc(Long buId);

    List<User> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    // Duplicate check
    List<User> findByUserNameIgnoreCase(String userName);

    List<User> findByEmailIgnoreCase(String email);
}
