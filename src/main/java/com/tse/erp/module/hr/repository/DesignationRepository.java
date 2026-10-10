package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationRepository
        extends JpaRepository<Designation, Long> {

    List<Designation> findAllByOrderByIdDesc();

    List<Designation> findByBgIdOrderByIdDesc(Long bgId);

    List<Designation> findByBgIdAndDesignationNameIgnoreCase(
            Long bgId, String designationName);

    List<Designation> findByBgIdAndShortNameIgnoreCase(
            Long bgId, String shortName);
}
