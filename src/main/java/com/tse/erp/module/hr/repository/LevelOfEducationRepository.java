package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.LevelOfEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelOfEducationRepository
        extends JpaRepository<LevelOfEducation, Long> {

    List<LevelOfEducation> findAllByOrderBySortOrderAscIdAsc();

    List<LevelOfEducation> findByLevelOfEduIgnoreCase(String levelOfEdu);

    List<LevelOfEducation> findByShortNameIgnoreCase(String shortName);
}
