package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.entity.LevelOfEducation;
import com.tse.erp.module.hr.repository.DegreeRepository;
import com.tse.erp.module.hr.repository.LevelOfEducationRepository;
import com.tse.erp.module.hr.service.LevelOfEducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelOfEducationServiceImpl
        implements LevelOfEducationService {

    private final LevelOfEducationRepository levelOfEducationRepository;
    private final DegreeRepository degreeRepository;

    @Override
    public List<LevelOfEducation> getAllLevelOfEducations() {
        return levelOfEducationRepository
                .findAllByOrderBySortOrderAscIdAsc();
    }

    @Override
    public LevelOfEducation getLevelOfEducationById(Long id) {
        return levelOfEducationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Level of Education not found with id: " + id));
    }

    @Override
    public LevelOfEducation createLevelOfEducation(
            LevelOfEducation levelOfEducation) {
        validateLevelOfEducation(levelOfEducation.getLevelOfEdu());
        validateShortName(levelOfEducation.getShortName());
        validateSortOrder(levelOfEducation.getSortOrder());
        StatusUtil.validate(levelOfEducation.getStatus());

        String levelOfEdu = levelOfEducation.getLevelOfEdu().trim();
        String shortName = normalizeShortName(
                levelOfEducation.getShortName());

        boolean levelOfEduExists = !levelOfEducationRepository
                .findByLevelOfEduIgnoreCase(levelOfEdu)
                .isEmpty();
        if (levelOfEduExists) {
            throw new DuplicateResourceException(
                    "Level of Education already exists: " + levelOfEdu);
        }

        if (shortName != null) {
            boolean shortNameExists = !levelOfEducationRepository
                    .findByShortNameIgnoreCase(shortName)
                    .isEmpty();
            if (shortNameExists) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        levelOfEducation.setId(null);
        levelOfEducation.setLevelOfEdu(levelOfEdu);
        levelOfEducation.setShortName(shortName);

        return levelOfEducationRepository.save(levelOfEducation);
    }

    @Override
    public LevelOfEducation updateLevelOfEducation(
            Long id, LevelOfEducation levelOfEducation) {
        LevelOfEducation existing = getLevelOfEducationById(id);

        validateLevelOfEducation(levelOfEducation.getLevelOfEdu());
        validateShortName(levelOfEducation.getShortName());
        validateSortOrder(levelOfEducation.getSortOrder());
        StatusUtil.validate(levelOfEducation.getStatus());

        String levelOfEdu = levelOfEducation.getLevelOfEdu().trim();
        String shortName = normalizeShortName(
                levelOfEducation.getShortName());

        boolean levelOfEduDuplicate = levelOfEducationRepository
                .findByLevelOfEduIgnoreCase(levelOfEdu)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (levelOfEduDuplicate) {
            throw new DuplicateResourceException(
                    "Level of Education already exists: " + levelOfEdu);
        }

        if (shortName != null) {
            boolean shortNameDuplicate = levelOfEducationRepository
                    .findByShortNameIgnoreCase(shortName)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (shortNameDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setLevelOfEdu(levelOfEdu);
        existing.setShortName(shortName);
        existing.setSortOrder(levelOfEducation.getSortOrder());
        if (levelOfEducation.getStatus() != null) {
            existing.setStatus(levelOfEducation.getStatus());
        }

        return levelOfEducationRepository.save(existing);
    }

    @Override
    public void deleteLevelOfEducation(Long id) {
        LevelOfEducation existing = getLevelOfEducationById(id);

        // TODO: block delete when Employee education records use this level, after that module exists.
        if (degreeRepository.existsByLevelOfEduId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Degrees exist under this Level of Education");
        }

        levelOfEducationRepository.delete(existing);
    }

    private void validateLevelOfEducation(String levelOfEdu) {
        if (levelOfEdu == null || levelOfEdu.trim().isEmpty()) {
            throw new BadRequestException(
                    "Level of education cannot be empty");
        }
        if (levelOfEdu.trim().length() < 2) {
            throw new BadRequestException(
                    "Level of education must be at least 2 characters");
        }
        if (levelOfEdu.trim().length() > 50) {
            throw new BadRequestException(
                    "Level of education cannot exceed 50 characters");
        }
    }

    private void validateShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return;
        }
        if (shortName.trim().length() < 2) {
            throw new BadRequestException(
                    "Short name must be at least 2 characters");
        }
        if (shortName.trim().length() > 5) {
            throw new BadRequestException(
                    "Short name cannot exceed 5 characters");
        }
    }

    private String normalizeShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return null;
        }
        return shortName.trim();
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder != null && sortOrder < 1) {
            throw new BadRequestException(
                    "Sort order must be greater than 0");
        }
    }
}
