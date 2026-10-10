package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DegreeRepository
        extends JpaRepository<Degree, Long> {

    List<Degree> findAllByOrderBySortOrderAscIdAsc();

    List<Degree> findByLevelOfEduIdOrderBySortOrderAscIdAsc(
            Long levelOfEduId);

    List<Degree> findByDegreeNameIgnoreCase(String degreeName);

    List<Degree> findByShortNameIgnoreCase(String shortName);

    boolean existsByLevelOfEduId(Long levelOfEduId);
}
