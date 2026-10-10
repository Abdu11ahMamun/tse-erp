package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.LevelOfEducation;

import java.util.List;

public interface LevelOfEducationService {

    List<LevelOfEducation> getAllLevelOfEducations();

    LevelOfEducation getLevelOfEducationById(Long id);

    LevelOfEducation createLevelOfEducation(
            LevelOfEducation levelOfEducation);

    LevelOfEducation updateLevelOfEducation(
            Long id, LevelOfEducation levelOfEducation);

    void deleteLevelOfEducation(Long id);
}
