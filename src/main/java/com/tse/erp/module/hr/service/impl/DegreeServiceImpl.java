package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.entity.Degree;
import com.tse.erp.module.hr.entity.LevelOfEducation;
import com.tse.erp.module.hr.repository.DegreeRepository;
import com.tse.erp.module.hr.repository.LevelOfEducationRepository;
import com.tse.erp.module.hr.service.DegreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DegreeServiceImpl implements DegreeService {

    private final DegreeRepository degreeRepository;
    private final LevelOfEducationRepository levelOfEducationRepository;

    @Override
    public List<Degree> getAllDegrees(Long levelOfEduId) {
        if (levelOfEduId == null) {
            return degreeRepository.findAllByOrderBySortOrderAscIdAsc();
        }
        return degreeRepository
                .findByLevelOfEduIdOrderBySortOrderAscIdAsc(levelOfEduId);
    }

    @Override
    public Degree getDegreeById(Long id) {
        return degreeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Degree not found with id: " + id));
    }

    @Override
    public Degree createDegree(Degree degree) {
        validateLevel(degree.getLevelOfEduId());
        validateDegreeName(degree.getDegreeName());
        validateShortName(degree.getShortName());
        validateSortOrder(degree.getSortOrder());
        StatusUtil.validate(degree.getStatus());

        String degreeName = degree.getDegreeName().trim();
        String shortName = normalizeShortName(degree.getShortName());

        boolean degreeNameExists = !degreeRepository
                .findByDegreeNameIgnoreCase(degreeName)
                .isEmpty();
        if (degreeNameExists) {
            throw new DuplicateResourceException(
                    "Degree already exists: " + degreeName);
        }

        if (shortName != null) {
            boolean shortNameExists = !degreeRepository
                    .findByShortNameIgnoreCase(shortName)
                    .isEmpty();
            if (shortNameExists) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        degree.setId(null);
        degree.setDegreeName(degreeName);
        degree.setShortName(shortName);

        return degreeRepository.save(degree);
    }

    @Override
    public Degree updateDegree(Long id, Degree degree) {
        Degree existing = getDegreeById(id);

        validateLevel(degree.getLevelOfEduId());
        validateDegreeName(degree.getDegreeName());
        validateShortName(degree.getShortName());
        validateSortOrder(degree.getSortOrder());
        StatusUtil.validate(degree.getStatus());

        String degreeName = degree.getDegreeName().trim();
        String shortName = normalizeShortName(degree.getShortName());

        boolean degreeNameDuplicate = degreeRepository
                .findByDegreeNameIgnoreCase(degreeName)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (degreeNameDuplicate) {
            throw new DuplicateResourceException(
                    "Degree already exists: " + degreeName);
        }

        if (shortName != null) {
            boolean shortNameDuplicate = degreeRepository
                    .findByShortNameIgnoreCase(shortName)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (shortNameDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setLevelOfEduId(degree.getLevelOfEduId());
        existing.setDegreeName(degreeName);
        existing.setShortName(shortName);
        existing.setSortOrder(degree.getSortOrder());
        if (degree.getStatus() != null) {
            existing.setStatus(degree.getStatus());
        }

        return degreeRepository.save(existing);
    }

    @Override
    public void deleteDegree(Long id) {
        Degree existing = getDegreeById(id);

        // TODO: block delete when Employee education records use this degree.
        degreeRepository.delete(existing);
    }

    private void validateLevel(Long levelOfEduId) {
        if (levelOfEduId == null) {
            throw new BadRequestException(
                    "Level of education cannot be empty");
        }
        LevelOfEducation level = levelOfEducationRepository
                .findById(levelOfEduId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Level of Education not found with id: "
                                + levelOfEduId));
        if (!Integer.valueOf(1).equals(level.getStatus())) {
            throw new BadRequestException(
                    "Selected Level of Education is not active");
        }
    }

    private void validateDegreeName(String degreeName) {
        if (degreeName == null || degreeName.trim().isEmpty()) {
            throw new BadRequestException(
                    "Degree name cannot be empty");
        }
        if (degreeName.trim().length() < 2) {
            throw new BadRequestException(
                    "Degree name must be at least 2 characters");
        }
        if (degreeName.trim().length() > 50) {
            throw new BadRequestException(
                    "Degree name cannot exceed 50 characters");
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
