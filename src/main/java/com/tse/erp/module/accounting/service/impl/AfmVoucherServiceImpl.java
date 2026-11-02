package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.*;
import com.tse.erp.module.accounting.entity.AfmVoucher;
import com.tse.erp.module.accounting.entity.AfmVoucherAttach;
import com.tse.erp.module.accounting.entity.AfmVoucherDetail;
import com.tse.erp.module.accounting.repository.AfmVoucherAttachRepository;
import com.tse.erp.module.accounting.repository.AfmVoucherDetailRepository;
import com.tse.erp.module.accounting.repository.AfmVoucherRepository;
import com.tse.erp.module.accounting.service.AfmVoucherService;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AfmVoucherServiceImpl implements AfmVoucherService {

    private final AfmVoucherRepository voucherRepository;
    private final AfmVoucherDetailRepository voucherDetailRepository;
    private final AfmVoucherAttachRepository voucherAttachRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmVoucherResponseDto> getAllVouchers(Long bgId, Long buId, String voucherType, Integer status, LocalDate fromDate, LocalDate toDate) {
        List<AfmVoucher> vouchers = voucherRepository.searchVouchers(bgId, buId, voucherType, status, fromDate, toDate);
        return vouchers.stream().map(this::toResponse).toList();
    }

    @Override
    public AfmVoucherResponseDto getVoucherById(Long id) {
        return toResponse(findVoucher(id));
    }

    @Override
    public AfmVoucherResponseDto getVoucherByNo(Long buId, String voucherNo) {
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        if (voucherNo == null || voucherNo.trim().isEmpty()) {
            throw new BadRequestException("Voucher No cannot be empty");
        }
        AfmVoucher voucher = voucherRepository.findByBuIdAndVoucherNoIgnoreCase(buId, voucherNo.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with Voucher No: " + voucherNo));
        return toResponse(voucher);
    }

    @Override
    public List<AfmVoucherDetailDto> getVoucherDetails(Long voucherId) {
        findVoucher(voucherId);
        return voucherDetailRepository.findByVoucherIdOrderByIdAsc(voucherId)
                .stream()
                .map(this::toDetailDto)
                .toList();
    }

    @Override
    public List<AfmVoucherAttachDto> getVoucherAttachments(Long voucherId) {
        findVoucher(voucherId);
        return voucherAttachRepository.findByVoucherIdOrderByIdAsc(voucherId)
                .stream()
                .map(this::toAttachDto)
                .toList();
    }

    @Override
    @Transactional
    public AfmVoucherResponseDto createVoucher(AfmVoucherRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        StatusUtil.validate(request.getStatus());

        String voucherNo = request.getVoucherNo() != null ? request.getVoucherNo().trim() : null;
        if (voucherNo != null && !voucherNo.isEmpty()) {
            boolean duplicate = voucherRepository.existsByBuIdAndVoucherNoIgnoreCase(request.getBuId(), voucherNo);
            if (duplicate) {
                throw new DuplicateResourceException("Voucher No already exists for this Business Unit: " + voucherNo);
            }
        }

        AfmVoucher voucher = AfmVoucher.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .voucherDate(request.getVoucherDate() != null ? request.getVoucherDate() : LocalDate.now())
                .voucherType(request.getVoucherType() != null ? request.getVoucherType().trim() : null)
                .voucherNo(voucherNo)
                .locationId(request.getLocationId())
                .paymentType(request.getPaymentType() != null ? request.getPaymentType().trim() : null)
                .paymentTo(request.getPaymentTo() != null ? request.getPaymentTo().trim() : null)
                .paymentThrough(request.getPaymentThrough() != null ? request.getPaymentThrough().trim() : null)
                .periodName(request.getPeriodName() != null ? request.getPeriodName().trim() : null)
                .voucherAmount(request.getVoucherAmount() != null ? request.getVoucherAmount() : BigDecimal.ZERO)
                .voucherSource(request.getVoucherSource() != null ? request.getVoucherSource().trim() : null)
                .voucherSourceLink(request.getVoucherSourceLink() != null ? request.getVoucherSourceLink().trim() : null)
                .voucherStatus(request.getVoucherStatus() != null ? request.getVoucherStatus().trim() : "DRAFT")
                .approvalCode(request.getApprovalCode() != null ? request.getApprovalCode().trim() : null)
                .bankAccount(request.getBankAccount() != null ? request.getBankAccount().trim() : null)
                .chequeNo(request.getChequeNo() != null ? request.getChequeNo().trim() : null)
                .chequeDate(request.getChequeDate())
                .chequeAmount(request.getChequeAmount())
                .isPosted(request.getIsPosted() != null ? request.getIsPosted() : 0)
                .isReconciled(request.getIsReconciled() != null ? request.getIsReconciled() : 0)
                .narration(request.getNarration() != null ? request.getNarration().trim() : null)
                .build();

        voucher.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        voucher.setCreatedApp(AppConstants.APP_NAME);

        AfmVoucher savedVoucher = voucherRepository.save(voucher);

        // Save Details
        if (request.getVoucherDetails() != null && !request.getVoucherDetails().isEmpty()) {
            List<AfmVoucherDetail> details = new ArrayList<>();
            for (AfmVoucherDetailDto dto : request.getVoucherDetails()) {
                AfmVoucherDetail detail = toDetailEntity(dto, savedVoucher.getId(), savedVoucher.getBgId(), savedVoucher.getBuId());
                detail.setCreatedApp(AppConstants.APP_NAME);
                details.add(detail);
            }
            voucherDetailRepository.saveAll(details);
        }

        // Save Attachments
        if (request.getVoucherAttachments() != null && !request.getVoucherAttachments().isEmpty()) {
            List<AfmVoucherAttach> attaches = new ArrayList<>();
            for (AfmVoucherAttachDto dto : request.getVoucherAttachments()) {
                AfmVoucherAttach attach = toAttachEntity(dto, savedVoucher.getId(), savedVoucher.getBgId(), savedVoucher.getBuId());
                attach.setCreatedApp(AppConstants.APP_NAME);
                attaches.add(attach);
            }
            voucherAttachRepository.saveAll(attaches);
        }

        return toResponse(savedVoucher);
    }

    @Override
    @Transactional
    public AfmVoucherResponseDto updateVoucher(Long id, AfmVoucherRequestDto request) {
        AfmVoucher existing = findVoucher(id);

        validateParents(request.getBgId(), request.getBuId());
        StatusUtil.validate(request.getStatus());

        String voucherNo = request.getVoucherNo() != null ? request.getVoucherNo().trim() : null;
        if (voucherNo != null && !voucherNo.isEmpty()) {
            boolean duplicate = voucherRepository.findByBuIdAndVoucherNoIgnoreCase(request.getBuId(), voucherNo)
                    .stream()
                    .anyMatch(v -> !v.getId().equals(id));
            if (duplicate) {
                throw new DuplicateResourceException("Voucher No already exists for this Business Unit: " + voucherNo);
            }
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        if (request.getVoucherDate() != null) existing.setVoucherDate(request.getVoucherDate());
        existing.setVoucherType(request.getVoucherType() != null ? request.getVoucherType().trim() : null);
        existing.setVoucherNo(voucherNo);
        existing.setLocationId(request.getLocationId());
        existing.setPaymentType(request.getPaymentType() != null ? request.getPaymentType().trim() : null);
        existing.setPaymentTo(request.getPaymentTo() != null ? request.getPaymentTo().trim() : null);
        existing.setPaymentThrough(request.getPaymentThrough() != null ? request.getPaymentThrough().trim() : null);
        existing.setPeriodName(request.getPeriodName() != null ? request.getPeriodName().trim() : null);
        if (request.getVoucherAmount() != null) existing.setVoucherAmount(request.getVoucherAmount());
        existing.setVoucherSource(request.getVoucherSource() != null ? request.getVoucherSource().trim() : null);
        existing.setVoucherSourceLink(request.getVoucherSourceLink() != null ? request.getVoucherSourceLink().trim() : null);
        if (request.getVoucherStatus() != null) existing.setVoucherStatus(request.getVoucherStatus().trim());
        existing.setApprovalCode(request.getApprovalCode() != null ? request.getApprovalCode().trim() : null);
        existing.setBankAccount(request.getBankAccount() != null ? request.getBankAccount().trim() : null);
        existing.setChequeNo(request.getChequeNo() != null ? request.getChequeNo().trim() : null);
        existing.setChequeDate(request.getChequeDate());
        existing.setChequeAmount(request.getChequeAmount());
        if (request.getIsPosted() != null) existing.setIsPosted(request.getIsPosted());
        if (request.getIsReconciled() != null) existing.setIsReconciled(request.getIsReconciled());
        existing.setNarration(request.getNarration() != null ? request.getNarration().trim() : null);
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmVoucher savedVoucher = voucherRepository.save(existing);

        // Synchronize Detail Rows in-place
        syncDetails(savedVoucher, request.getVoucherDetails());

        // Synchronize Attachments in-place
        syncAttachments(savedVoucher, request.getVoucherAttachments());

        return toResponse(savedVoucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        AfmVoucher existing = findVoucher(id);
        List<AfmVoucherDetail> details = voucherDetailRepository.findByVoucherIdOrderByIdAsc(id);
        if (!details.isEmpty()) {
            voucherDetailRepository.deleteAll(details);
        }
        List<AfmVoucherAttach> attaches = voucherAttachRepository.findByVoucherIdOrderByIdAsc(id);
        if (!attaches.isEmpty()) {
            voucherAttachRepository.deleteAll(attaches);
        }
        voucherRepository.delete(existing);
    }

    // Detail Sub-resource CRUD
    @Override
    @Transactional
    public AfmVoucherDetailDto addVoucherDetail(Long voucherId, AfmVoucherDetailDto detailDto) {
        AfmVoucher voucher = findVoucher(voucherId);
        AfmVoucherDetail entity = toDetailEntity(detailDto, voucherId, voucher.getBgId(), voucher.getBuId());
        entity.setCreatedApp(AppConstants.APP_NAME);
        AfmVoucherDetail saved = voucherDetailRepository.save(entity);
        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public AfmVoucherDetailDto updateVoucherDetail(Long voucherId, Long detailId, AfmVoucherDetailDto detailDto) {
        findVoucher(voucherId);
        AfmVoucherDetail existing = voucherDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher detail not found with id: " + detailId));
        if (!voucherId.equals(existing.getVoucherId())) {
            throw new BadRequestException("Detail row with id " + detailId + " does not belong to Voucher " + voucherId);
        }
        existing.setCoaId(detailDto.getCoaId());
        existing.setCoaSourceName(detailDto.getCoaSourceName() != null ? detailDto.getCoaSourceName().trim() : null);
        existing.setCoaSourceId(detailDto.getCoaSourceId());
        existing.setCostCenterId(detailDto.getCostCenterId());
        existing.setDrAmount(detailDto.getDrAmount() != null ? detailDto.getDrAmount() : BigDecimal.ZERO);
        existing.setCrAmount(detailDto.getCrAmount() != null ? detailDto.getCrAmount() : BigDecimal.ZERO);
        if (detailDto.getStatus() != null) existing.setStatus(detailDto.getStatus());
        existing.setUpdatedApp(AppConstants.APP_NAME);
        return toDetailDto(voucherDetailRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteVoucherDetail(Long voucherId, Long detailId) {
        findVoucher(voucherId);
        AfmVoucherDetail existing = voucherDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher detail not found with id: " + detailId));
        if (!voucherId.equals(existing.getVoucherId())) {
            throw new BadRequestException("Detail row with id " + detailId + " does not belong to Voucher " + voucherId);
        }
        voucherDetailRepository.delete(existing);
    }

    // Attachment Sub-resource CRUD
    @Override
    @Transactional
    public AfmVoucherAttachDto addVoucherAttachment(Long voucherId, AfmVoucherAttachDto attachDto) {
        AfmVoucher voucher = findVoucher(voucherId);
        AfmVoucherAttach entity = toAttachEntity(attachDto, voucherId, voucher.getBgId(), voucher.getBuId());
        entity.setCreatedApp(AppConstants.APP_NAME);
        AfmVoucherAttach saved = voucherAttachRepository.save(entity);
        return toAttachDto(saved);
    }

    @Override
    @Transactional
    public AfmVoucherAttachDto updateVoucherAttachment(Long voucherId, Long attachId, AfmVoucherAttachDto attachDto) {
        findVoucher(voucherId);
        AfmVoucherAttach existing = voucherAttachRepository.findById(attachId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher attachment not found with id: " + attachId));
        if (!voucherId.equals(existing.getVoucherId())) {
            throw new BadRequestException("Attachment with id " + attachId + " does not belong to Voucher " + voucherId);
        }
        existing.setAttachmentFileName(attachDto.getAttachmentFileName() != null ? attachDto.getAttachmentFileName().trim() : null);
        if (attachDto.getAttachmentFile() != null) existing.setAttachmentFile(attachDto.getAttachmentFile());
        if (attachDto.getIsActive() != null) existing.setIsActive(attachDto.getIsActive());
        if (attachDto.getStatus() != null) existing.setStatus(attachDto.getStatus());
        existing.setUpdatedApp(AppConstants.APP_NAME);
        return toAttachDto(voucherAttachRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteVoucherAttachment(Long voucherId, Long attachId) {
        findVoucher(voucherId);
        AfmVoucherAttach existing = voucherAttachRepository.findById(attachId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher attachment not found with id: " + attachId));
        if (!voucherId.equals(existing.getVoucherId())) {
            throw new BadRequestException("Attachment with id " + attachId + " does not belong to Voucher " + voucherId);
        }
        voucherAttachRepository.delete(existing);
    }

    // =========================================
    // HELPER & SYNCHRONIZATION METHODS
    // =========================================

    private void syncDetails(AfmVoucher voucher, List<AfmVoucherDetailDto> incoming) {
        List<AfmVoucherDetail> current = voucherDetailRepository.findByVoucherIdOrderByIdAsc(voucher.getId());
        Map<Long, AfmVoucherDetail> map = new HashMap<>();
        current.forEach(d -> map.put(d.getId(), d));

        Set<Long> processedIds = new HashSet<>();
        List<AfmVoucherDetail> toSave = new ArrayList<>();

        if (incoming != null) {
            for (AfmVoucherDetailDto dto : incoming) {
                if (dto.getId() != null && map.containsKey(dto.getId())) {
                    AfmVoucherDetail ex = map.get(dto.getId());
                    ex.setBgId(voucher.getBgId());
                    ex.setBuId(voucher.getBuId());
                    ex.setCoaId(dto.getCoaId());
                    ex.setCoaSourceName(dto.getCoaSourceName() != null ? dto.getCoaSourceName().trim() : null);
                    ex.setCoaSourceId(dto.getCoaSourceId());
                    ex.setCostCenterId(dto.getCostCenterId());
                    ex.setDrAmount(dto.getDrAmount() != null ? dto.getDrAmount() : BigDecimal.ZERO);
                    ex.setCrAmount(dto.getCrAmount() != null ? dto.getCrAmount() : BigDecimal.ZERO);
                    if (dto.getStatus() != null) ex.setStatus(dto.getStatus());
                    ex.setUpdatedApp(AppConstants.APP_NAME);
                    toSave.add(ex);
                    processedIds.add(dto.getId());
                } else {
                    AfmVoucherDetail newDetail = toDetailEntity(dto, voucher.getId(), voucher.getBgId(), voucher.getBuId());
                    newDetail.setId(null);
                    newDetail.setCreatedApp(AppConstants.APP_NAME);
                    toSave.add(newDetail);
                }
            }
        }

        List<AfmVoucherDetail> toDelete = current.stream()
                .filter(d -> !processedIds.contains(d.getId()))
                .toList();

        if (!toDelete.isEmpty()) voucherDetailRepository.deleteAll(toDelete);
        if (!toSave.isEmpty()) voucherDetailRepository.saveAll(toSave);
    }

    private void syncAttachments(AfmVoucher voucher, List<AfmVoucherAttachDto> incoming) {
        List<AfmVoucherAttach> current = voucherAttachRepository.findByVoucherIdOrderByIdAsc(voucher.getId());
        Map<Long, AfmVoucherAttach> map = new HashMap<>();
        current.forEach(a -> map.put(a.getId(), a));

        Set<Long> processedIds = new HashSet<>();
        List<AfmVoucherAttach> toSave = new ArrayList<>();

        if (incoming != null) {
            for (AfmVoucherAttachDto dto : incoming) {
                if (dto.getId() != null && map.containsKey(dto.getId())) {
                    AfmVoucherAttach ex = map.get(dto.getId());
                    ex.setBgId(voucher.getBgId());
                    ex.setBuId(voucher.getBuId());
                    ex.setAttachmentFileName(dto.getAttachmentFileName() != null ? dto.getAttachmentFileName().trim() : null);
                    if (dto.getAttachmentFile() != null) ex.setAttachmentFile(dto.getAttachmentFile());
                    if (dto.getIsActive() != null) ex.setIsActive(dto.getIsActive());
                    if (dto.getStatus() != null) ex.setStatus(dto.getStatus());
                    ex.setUpdatedApp(AppConstants.APP_NAME);
                    toSave.add(ex);
                    processedIds.add(dto.getId());
                } else {
                    AfmVoucherAttach newAttach = toAttachEntity(dto, voucher.getId(), voucher.getBgId(), voucher.getBuId());
                    newAttach.setId(null);
                    newAttach.setCreatedApp(AppConstants.APP_NAME);
                    toSave.add(newAttach);
                }
            }
        }

        List<AfmVoucherAttach> toDelete = current.stream()
                .filter(a -> !processedIds.contains(a.getId()))
                .toList();

        if (!toDelete.isEmpty()) voucherAttachRepository.deleteAll(toDelete);
        if (!toSave.isEmpty()) voucherAttachRepository.saveAll(toSave);
    }

    private AfmVoucher findVoucher(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
    }

    private AfmVoucherResponseDto toResponse(AfmVoucher voucher) {
        List<AfmVoucherDetailDto> details = voucherDetailRepository
                .findByVoucherIdOrderByIdAsc(voucher.getId())
                .stream()
                .map(this::toDetailDto)
                .toList();

        List<AfmVoucherAttachDto> attachments = voucherAttachRepository
                .findByVoucherIdOrderByIdAsc(voucher.getId())
                .stream()
                .map(this::toAttachDto)
                .toList();

        return AfmVoucherResponseDto.builder()
                .id(voucher.getId())
                .bgId(voucher.getBgId())
                .buId(voucher.getBuId())
                .voucherDate(voucher.getVoucherDate())
                .voucherType(voucher.getVoucherType())
                .voucherNo(voucher.getVoucherNo())
                .locationId(voucher.getLocationId())
                .paymentType(voucher.getPaymentType())
                .paymentTo(voucher.getPaymentTo())
                .paymentThrough(voucher.getPaymentThrough())
                .periodName(voucher.getPeriodName())
                .voucherAmount(voucher.getVoucherAmount())
                .voucherSource(voucher.getVoucherSource())
                .voucherSourceLink(voucher.getVoucherSourceLink())
                .voucherStatus(voucher.getVoucherStatus())
                .approvalCode(voucher.getApprovalCode())
                .bankAccount(voucher.getBankAccount())
                .chequeNo(voucher.getChequeNo())
                .chequeDate(voucher.getChequeDate())
                .chequeAmount(voucher.getChequeAmount())
                .isPosted(voucher.getIsPosted())
                .isReconciled(voucher.getIsReconciled())
                .narration(voucher.getNarration())
                .status(voucher.getStatus())
                .createdUserId(voucher.getCreatedUserId())
                .createdEmpId(voucher.getCreatedEmpId())
                .createdDatetime(voucher.getCreatedDatetime())
                .updatedUserId(voucher.getUpdatedUserId())
                .updatedEmpId(voucher.getUpdatedEmpId())
                .updatedDatetime(voucher.getUpdatedDatetime())
                .createdApp(voucher.getCreatedApp())
                .updatedApp(voucher.getUpdatedApp())
                .voucherDetails(details)
                .voucherAttachments(attachments)
                .build();
    }

    private AfmVoucherDetailDto toDetailDto(AfmVoucherDetail detail) {
        return AfmVoucherDetailDto.builder()
                .id(detail.getId())
                .bgId(detail.getBgId())
                .buId(detail.getBuId())
                .voucherId(detail.getVoucherId())
                .coaId(detail.getCoaId())
                .coaSourceName(detail.getCoaSourceName())
                .coaSourceId(detail.getCoaSourceId())
                .costCenterId(detail.getCostCenterId())
                .drAmount(detail.getDrAmount())
                .crAmount(detail.getCrAmount())
                .status(detail.getStatus())
                .build();
    }

    private AfmVoucherAttachDto toAttachDto(AfmVoucherAttach attach) {
        return AfmVoucherAttachDto.builder()
                .id(attach.getId())
                .bgId(attach.getBgId())
                .buId(attach.getBuId())
                .voucherId(attach.getVoucherId())
                .attachmentFileName(attach.getAttachmentFileName())
                .attachmentFile(attach.getAttachmentFile())
                .isActive(attach.getIsActive())
                .status(attach.getStatus())
                .build();
    }

    private AfmVoucherDetail toDetailEntity(AfmVoucherDetailDto dto, Long voucherId, Long bgId, Long buId) {
        return AfmVoucherDetail.builder()
                .id(dto.getId())
                .bgId(dto.getBgId() != null ? dto.getBgId() : bgId)
                .buId(dto.getBuId() != null ? dto.getBuId() : buId)
                .voucherId(voucherId)
                .coaId(dto.getCoaId())
                .coaSourceName(dto.getCoaSourceName() != null ? dto.getCoaSourceName().trim() : null)
                .coaSourceId(dto.getCoaSourceId())
                .costCenterId(dto.getCostCenterId())
                .drAmount(dto.getDrAmount() != null ? dto.getDrAmount() : BigDecimal.ZERO)
                .crAmount(dto.getCrAmount() != null ? dto.getCrAmount() : BigDecimal.ZERO)
                .build();
    }

    private AfmVoucherAttach toAttachEntity(AfmVoucherAttachDto dto, Long voucherId, Long bgId, Long buId) {
        return AfmVoucherAttach.builder()
                .id(dto.getId())
                .bgId(dto.getBgId() != null ? dto.getBgId() : bgId)
                .buId(dto.getBuId() != null ? dto.getBuId() : buId)
                .voucherId(voucherId)
                .attachmentFileName(dto.getAttachmentFileName() != null ? dto.getAttachmentFileName().trim() : null)
                .attachmentFile(dto.getAttachmentFile())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : 1)
                .build();
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) throw new BadRequestException("Business Group id cannot be empty");
        if (buId == null) throw new BadRequestException("Business Unit id cannot be empty");

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found with id: " + buId));

        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Selected Business Unit is not active");
        }
        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException("Business Unit does not belong to selected Business Group");
        }
    }
}
