package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.hr.entity.OfficeNotice;
import com.tse.erp.module.hr.repository.OfficeNoticeRepository;
import com.tse.erp.module.hr.service.OfficeNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeNoticeServiceImpl implements OfficeNoticeService {

    private final OfficeNoticeRepository officeNoticeRepository;
    private final BusinessGroupRepository businessGroupRepository;

    @Override
    public List<OfficeNotice> getAllOfficeNotices(Long bgId, boolean current) {

        List<OfficeNotice> list = bgId == null
                ? officeNoticeRepository.findAllByOrderByIdDesc()
                : officeNoticeRepository.findByBgIdOrderByIdDesc(bgId);

        if (!current) {
            return list;
        }

        // Ekhon dekhano jabe emon notice, important age, tarpor notun
        LocalDate today = LocalDate.now();
        return list.stream()
                .filter(n -> Integer.valueOf(1).equals(n.getStatus()))
                .filter(n -> !n.getPublishDate().isAfter(today)
                        && !n.getExpireDate().isBefore(today))
                .sorted(Comparator
                        .comparing(OfficeNotice::getIsImportant).reversed()
                        .thenComparing(OfficeNotice::getPublishDate,
                                Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public OfficeNotice getOfficeNoticeById(Long id) {
        return officeNoticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office Notice not found with id: " + id));
    }

    @Override
    public OfficeNotice createOfficeNotice(OfficeNotice officeNotice) {

        prepare(officeNotice);
        StatusUtil.validate(officeNotice.getStatus());

        officeNotice.setId(null);

        // Status default 1, BaseEntity @PrePersist handle korbe
        return officeNoticeRepository.save(officeNotice);
    }

    @Override
    public OfficeNotice updateOfficeNotice(Long id, OfficeNotice officeNotice) {

        OfficeNotice existing = getOfficeNoticeById(id);

        prepare(officeNotice);
        StatusUtil.validate(officeNotice.getStatus());

        existing.setBgId(officeNotice.getBgId());
        existing.setTitle(officeNotice.getTitle());
        existing.setPublishDate(officeNotice.getPublishDate());
        existing.setExpireDate(officeNotice.getExpireDate());
        existing.setAttachment(officeNotice.getAttachment());
        existing.setIsImportant(officeNotice.getIsImportant());
        existing.setDescription(officeNotice.getDescription());
        if (officeNotice.getStatus() != null) {
            existing.setStatus(officeNotice.getStatus());
        }

        return officeNoticeRepository.save(existing);
    }

    @Override
    public void deleteOfficeNotice(Long id) {
        OfficeNotice existing = getOfficeNoticeById(id);
        officeNoticeRepository.delete(existing);
    }

    // =========================================
    // VALIDATE + CLEAN
    // =========================================
    private void prepare(OfficeNotice n) {

        // Business Group
        if (n.getBgId() == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }
        businessGroupRepository.findById(n.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + n.getBgId()));

        // Title
        if (n.getTitle() == null || n.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Title cannot be empty");
        }
        String title = n.getTitle().trim();
        if (title.length() < 2) {
            throw new BadRequestException(
                    "Title must be at least 2 characters");
        }
        if (title.length() > 200) {
            throw new BadRequestException(
                    "Title cannot exceed 200 characters");
        }
        if (hasTag(title)) {
            throw new BadRequestException(
                    "Title contains unsupported characters");
        }

        // Dates
        if (n.getPublishDate() == null) {
            throw new BadRequestException("Publish date cannot be empty");
        }
        if (n.getExpireDate() == null) {
            throw new BadRequestException("Expire date cannot be empty");
        }
        if (n.getExpireDate().isBefore(n.getPublishDate())) {
            throw new BadRequestException(
                    "Expire date cannot be before publish date");
        }

        // Attachment (reference only, upload pore)
        String attachment = null;
        if (n.getAttachment() != null && !n.getAttachment().trim().isEmpty()) {
            attachment = n.getAttachment().trim();
            if (attachment.length() > 255) {
                throw new BadRequestException(
                        "Attachment reference cannot exceed 255 characters");
            }
            if (attachment.contains("..")) {
                throw new BadRequestException(
                        "Invalid attachment reference");
            }
        }

        // Is Important
        Integer important = n.getIsImportant() == null ? 0 : n.getIsImportant();
        if (important != 0 && important != 1) {
            throw new BadRequestException("Is important must be 0 or 1");
        }

        // Description
        String description = null;
        if (n.getDescription() != null && !n.getDescription().trim().isEmpty()) {
            description = n.getDescription().trim();
            if (description.length() > 5000) {
                throw new BadRequestException(
                        "Description cannot exceed 5000 characters");
            }
            if (hasTag(description)) {
                throw new BadRequestException(
                        "Description contains unsupported characters");
            }
        }

        n.setTitle(title);
        n.setAttachment(attachment);
        n.setIsImportant(important);
        n.setDescription(description);
    }

    private boolean hasTag(String v) {
        return v.indexOf('<') >= 0 || v.indexOf('>') >= 0;
    }
}