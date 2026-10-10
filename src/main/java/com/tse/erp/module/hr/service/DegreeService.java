package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.Degree;

import java.util.List;

public interface DegreeService {

    List<Degree> getAllDegrees(Long levelOfEduId);

    Degree getDegreeById(Long id);

    Degree createDegree(Degree degree);

    Degree updateDegree(Long id, Degree degree);

    void deleteDegree(Long id);
}
