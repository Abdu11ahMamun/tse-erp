package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmLedgerAttachDto;
import com.tse.erp.module.accounting.dto.AfmLedgerDetailDto;
import com.tse.erp.module.accounting.dto.AfmLedgerRequestDto;
import com.tse.erp.module.accounting.dto.AfmLedgerResponseDto;
import com.tse.erp.module.accounting.entity.*;
import com.tse.erp.module.accounting.repository.*;
import com.tse.erp.module.accounting.service.AfmLedgerService;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AfmLedgerServiceImpl implements AfmLedgerService {

    private final AfmLedgerRepository ledgerRepository;
    private final AfmLedgerDetailRepository ledgerDetailRepository;
    private final AfmLedgerAttachRepository ledgerAttachRepository;
    private final AfmVoucherRepository voucherRepository;
    private final AfmVoucherDetailRepository voucherDetailRepository;
    private final AfmVoucherAttachRepository voucherAttachRepository;
    private final AfmLookupValueRepository lookupValueRepository;
    private final AfmChartOfAccountsRepository chartOfAccountsRepository;
    private final AfmCostCenterRepository costCenterRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmLedgerResponseDto> getAllLedgers(Long bgId, Long buId, String voucherType, Integer status, LocalDate fromDate, LocalDate toDate) {
        syncAllMissingVouchers();
        List<AfmLedger> ledgers = ledgerRepository.searchLedgers(bgId, buId, voucherType, status, fromDate, toDate);
        return ledgers.stream().map(this::toResponse).toList();
    }

    private void syncAllMissingVouchers() {
        try {
            List<AfmVoucher> vouchers = voucherRepository.findAll();
            for (AfmVoucher voucher : vouchers) {
                Optional<AfmLedger> existing = ledgerRepository
                        .findByVoucherSourceAndVoucherSourceLink("AFM_VOUCHER", String.valueOf(voucher.getId()));
                if (existing.isEmpty() && voucher.getBuId() != null && voucher.getVoucherNo() != null) {
                    existing = ledgerRepository.findByBuIdAndVoucherNoIgnoreCase(voucher.getBuId(), voucher.getVoucherNo());
                }
                if (existing.isEmpty()) {
                    List<AfmVoucherDetail> details = voucherDetailRepository.findByVoucherIdOrderByIdAsc(voucher.getId());
                    List<AfmVoucherAttach> attaches = voucherAttachRepository.findByVoucherIdOrderByIdAsc(voucher.getId());
                    syncFromVoucherCreate(voucher, details, attaches);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public AfmLedgerResponseDto getLedgerById(Long id) {
        return toResponse(findLedger(id));
    }

    @Override
    public AfmLedgerResponseDto getLedgerByNo(Long buId, String voucherNo) {
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        if (voucherNo == null || voucherNo.trim().isEmpty()) {
            throw new BadRequestException("Voucher No cannot be empty");
        }
        AfmLedger ledger = ledgerRepository.findByBuIdAndVoucherNoIgnoreCase(buId, voucherNo.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found with Voucher No: " + voucherNo));
        return toResponse(ledger);
    }

    @Override
    public List<AfmLedgerDetailDto> getLedgerDetails(Long ledgerId) {
        findLedger(ledgerId);
        return ledgerDetailRepository.findByVoucherIdOrderByIdAsc(ledgerId)
                .stream()
                .map(this::toDetailDto)
                .toList();
    }

    @Override
    public List<AfmLedgerAttachDto> getLedgerAttachments(Long ledgerId) {
        findLedger(ledgerId);
        return ledgerAttachRepository.findByVoucherIdOrderByIdAsc(ledgerId)
                .stream()
                .map(this::toAttachDto)
                .toList();
    }

    @Override
    @Transactional
    public AfmLedgerResponseDto createLedger(AfmLedgerRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        StatusUtil.validate(request.getStatus());

        String voucherNo = request.getVoucherNo() != null ? request.getVoucherNo().trim() : null;
        if (voucherNo != null && !voucherNo.isEmpty()) {
            boolean duplicate = ledgerRepository.existsByBuIdAndVoucherNoIgnoreCase(request.getBuId(), voucherNo);
            if (duplicate) {
                throw new DuplicateResourceException("Voucher No already exists in Ledger for this Business Unit: " + voucherNo);
            }
        }

        Long voucherTypeId = request.getVoucherTypeId();
        String voucherType = request.getVoucherType() != null ? request.getVoucherType().trim() : null;
        if (voucherTypeId != null && (voucherType == null || voucherType.isEmpty())) {
            voucherType = lookupValueRepository.findById(voucherTypeId)
                    .map(AfmLookupValue::getValue)
                    .orElse(null);
        }

        Long paymentTypeId = request.getPaymentTypeId();
        String paymentType = request.getPaymentType() != null ? request.getPaymentType().trim() : null;
        if (paymentTypeId != null && (paymentType == null || paymentType.isEmpty())) {
            paymentType = lookupValueRepository.findById(paymentTypeId)
                    .map(AfmLookupValue::getValue)
                    .orElse(null);
        }

        Long paymentThroughId = request.getPaymentThroughId();
        String paymentThrough = request.getPaymentThrough() != null ? request.getPaymentThrough().trim() : null;
        if (paymentThroughId != null && (paymentThrough == null || paymentThrough.isEmpty())) {
            paymentThrough = lookupValueRepository.findById(paymentThroughId)
                    .map(AfmLookupValue::getValue)
                    .orElse(null);
        }

        AfmLedger ledger = AfmLedger.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .voucherDate(request.getVoucherDate() != null ? request.getVoucherDate() : LocalDate.now())
                .voucherTypeId(voucherTypeId)
                .voucherType(voucherType)
                .voucherNo(voucherNo)
                .locationId(request.getLocationId())
                .paymentTypeId(paymentTypeId)
                .paymentType(paymentType)
                .paymentTo(request.getPaymentTo() != null ? request.getPaymentTo().trim() : null)
                .paymentThroughId(paymentThroughId)
                .paymentThrough(paymentThrough)
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

        ledger.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        ledger.setCreatedApp(AppConstants.APP_NAME);

        AfmLedger savedLedger = ledgerRepository.save(ledger);

        // Save Details
        if (request.getLedgerDetails() != null && !request.getLedgerDetails().isEmpty()) {
            List<AfmLedgerDetail> details = new ArrayList<>();
            for (AfmLedgerDetailDto dto : request.getLedgerDetails()) {
                AfmLedgerDetail detail = toDetailEntity(dto, savedLedger.getId(), savedLedger.getBgId(), savedLedger.getBuId());
                detail.setCreatedApp(AppConstants.APP_NAME);
                details.add(detail);
            }
            ledgerDetailRepository.saveAll(details);
        }

        // Save Attachments
        if (request.getLedgerAttachments() != null && !request.getLedgerAttachments().isEmpty()) {
            List<AfmLedgerAttach> attaches = new ArrayList<>();
            for (AfmLedgerAttachDto dto : request.getLedgerAttachments()) {
                AfmLedgerAttach attach = toAttachEntity(dto, savedLedger.getId(), savedLedger.getBgId(), savedLedger.getBuId());
                attach.setCreatedApp(AppConstants.APP_NAME);
                attaches.add(attach);
            }
            ledgerAttachRepository.saveAll(attaches);
        }

        return toResponse(savedLedger);
    }

    @Override
    @Transactional
    public AfmLedgerResponseDto updateLedger(Long id, AfmLedgerRequestDto request) {
        AfmLedger existing = findLedger(id);
        validateParents(request.getBgId(), request.getBuId());
        StatusUtil.validate(request.getStatus());

        String voucherNo = request.getVoucherNo() != null ? request.getVoucherNo().trim() : null;
        if (voucherNo != null && !voucherNo.isEmpty()) {
            boolean duplicate = ledgerRepository.findByBuIdAndVoucherNoIgnoreCase(request.getBuId(), voucherNo)
                    .stream()
                    .anyMatch(l -> !l.getId().equals(id));
            if (duplicate) {
                throw new DuplicateResourceException("Voucher No already exists in Ledger for this Business Unit: " + voucherNo);
            }
        }

        Long voucherTypeId = request.getVoucherTypeId();
        String voucherType = request.getVoucherType() != null ? request.getVoucherType().trim() : null;
        if (voucherTypeId != null && (voucherType == null || voucherType.isEmpty())) {
            voucherType = lookupValueRepository.findById(voucherTypeId)
                    .map(AfmLookupValue::getValue)
                    .orElse(existing.getVoucherType());
        }

        Long paymentTypeId = request.getPaymentTypeId();
        String paymentType = request.getPaymentType() != null ? request.getPaymentType().trim() : null;
        if (paymentTypeId != null && (paymentType == null || paymentType.isEmpty())) {
            paymentType = lookupValueRepository.findById(paymentTypeId)
                    .map(AfmLookupValue::getValue)
                    .orElse(existing.getPaymentType());
        }

        Long paymentThroughId = request.getPaymentThroughId();
        String paymentThrough = request.getPaymentThrough() != null ? request.getPaymentThrough().trim() : null;
        if (paymentThroughId != null && (paymentThrough == null || paymentThrough.isEmpty())) {
            paymentThrough = lookupValueRepository.findById(paymentThroughId)
                    .map(AfmLookupValue::getValue)
                    .orElse(existing.getPaymentThrough());
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        if (request.getVoucherDate() != null) existing.setVoucherDate(request.getVoucherDate());
        existing.setVoucherTypeId(voucherTypeId != null ? voucherTypeId : existing.getVoucherTypeId());
        if (voucherType != null) existing.setVoucherType(voucherType);
        existing.setVoucherNo(voucherNo);
        existing.setLocationId(request.getLocationId());
        existing.setPaymentTypeId(paymentTypeId != null ? paymentTypeId : existing.getPaymentTypeId());
        if (paymentType != null) existing.setPaymentType(paymentType);
        existing.setPaymentTo(request.getPaymentTo() != null ? request.getPaymentTo().trim() : null);
        existing.setPaymentThroughId(paymentThroughId != null ? paymentThroughId : existing.getPaymentThroughId());
        if (paymentThrough != null) existing.setPaymentThrough(paymentThrough);
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

        AfmLedger savedLedger = ledgerRepository.save(existing);

        // Synchronize details
        ledgerDetailRepository.deleteByVoucherId(savedLedger.getId());
        if (request.getLedgerDetails() != null && !request.getLedgerDetails().isEmpty()) {
            List<AfmLedgerDetail> details = new ArrayList<>();
            for (AfmLedgerDetailDto dto : request.getLedgerDetails()) {
                AfmLedgerDetail detail = toDetailEntity(dto, savedLedger.getId(), savedLedger.getBgId(), savedLedger.getBuId());
                detail.setCreatedApp(AppConstants.APP_NAME);
                details.add(detail);
            }
            ledgerDetailRepository.saveAll(details);
        }

        // Synchronize attachments
        ledgerAttachRepository.deleteByVoucherId(savedLedger.getId());
        if (request.getLedgerAttachments() != null && !request.getLedgerAttachments().isEmpty()) {
            List<AfmLedgerAttach> attaches = new ArrayList<>();
            for (AfmLedgerAttachDto dto : request.getLedgerAttachments()) {
                AfmLedgerAttach attach = toAttachEntity(dto, savedLedger.getId(), savedLedger.getBgId(), savedLedger.getBuId());
                attach.setCreatedApp(AppConstants.APP_NAME);
                attaches.add(attach);
            }
            ledgerAttachRepository.saveAll(attaches);
        }

        return toResponse(savedLedger);
    }

    @Override
    @Transactional
    public void deleteLedger(Long id) {
        AfmLedger existing = findLedger(id);
        ledgerDetailRepository.deleteByVoucherId(id);
        ledgerAttachRepository.deleteByVoucherId(id);
        ledgerRepository.delete(existing);
    }

    // -------------------------------------------------------------
    // SYNC FROM VOUCHER (One-way: Voucher -> Ledger only)
    // -------------------------------------------------------------
    @Override
    @Transactional
    public void syncFromVoucherCreate(AfmVoucher voucher, List<AfmVoucherDetail> details, List<AfmVoucherAttach> attaches) {
        if (voucher == null) return;

        AfmLedger ledger = AfmLedger.builder()
                .bgId(voucher.getBgId())
                .buId(voucher.getBuId())
                .voucherDate(voucher.getVoucherDate())
                .voucherTypeId(voucher.getVoucherTypeId())
                .voucherType(voucher.getVoucherType())
                .voucherNo(voucher.getVoucherNo())
                .locationId(voucher.getLocationId())
                .paymentTypeId(voucher.getPaymentTypeId())
                .paymentType(voucher.getPaymentType())
                .paymentTo(voucher.getPaymentTo())
                .paymentThroughId(voucher.getPaymentThroughId())
                .paymentThrough(voucher.getPaymentThrough())
                .periodName(voucher.getPeriodName())
                .voucherAmount(voucher.getVoucherAmount())
                .voucherSource("AFM_VOUCHER")
                .voucherSourceLink(String.valueOf(voucher.getId()))
                .voucherStatus(voucher.getVoucherStatus())
                .approvalCode(voucher.getApprovalCode())
                .bankAccount(voucher.getBankAccount())
                .chequeNo(voucher.getChequeNo())
                .chequeDate(voucher.getChequeDate())
                .chequeAmount(voucher.getChequeAmount())
                .isPosted(voucher.getIsPosted())
                .isReconciled(voucher.getIsReconciled())
                .narration(voucher.getNarration())
                .build();

        ledger.setStatus(voucher.getStatus() != null ? voucher.getStatus() : 1);
        ledger.setCreatedApp(AppConstants.APP_NAME);

        AfmLedger savedLedger = ledgerRepository.save(ledger);

        if (details != null && !details.isEmpty()) {
            List<AfmLedgerDetail> ledgerDetails = new ArrayList<>();
            for (AfmVoucherDetail vd : details) {
                AfmLedgerDetail ld = AfmLedgerDetail.builder()
                        .bgId(vd.getBgId() != null ? vd.getBgId() : savedLedger.getBgId())
                        .buId(vd.getBuId() != null ? vd.getBuId() : savedLedger.getBuId())
                        .voucherId(savedLedger.getId())
                        .coaId(vd.getCoaId())
                        .coaSourceType(vd.getCoaSourceType())
                        .coaSourceName(vd.getCoaSourceName())
                        .coaSourceId(vd.getCoaSourceId())
                        .costCenterId(vd.getCostCenterId())
                        .drAmount(vd.getDrAmount())
                        .crAmount(vd.getCrAmount())
                        .build();
                ld.setStatus(vd.getStatus() != null ? vd.getStatus() : 1);
                ld.setCreatedApp(AppConstants.APP_NAME);
                ledgerDetails.add(ld);
            }
            ledgerDetailRepository.saveAll(ledgerDetails);
        }

        if (attaches != null && !attaches.isEmpty()) {
            List<AfmLedgerAttach> ledgerAttaches = new ArrayList<>();
            for (AfmVoucherAttach va : attaches) {
                AfmLedgerAttach la = AfmLedgerAttach.builder()
                        .bgId(va.getBgId() != null ? va.getBgId() : savedLedger.getBgId())
                        .buId(va.getBuId() != null ? va.getBuId() : savedLedger.getBuId())
                        .voucherId(savedLedger.getId())
                        .attachmentFileName(va.getAttachmentFileName())
                        .attachmentFile(va.getAttachmentFile())
                        .isActive(va.getIsActive())
                        .build();
                la.setStatus(va.getStatus() != null ? va.getStatus() : 1);
                la.setCreatedApp(AppConstants.APP_NAME);
                ledgerAttaches.add(la);
            }
            ledgerAttachRepository.saveAll(ledgerAttaches);
        }
    }

    @Override
    @Transactional
    public void syncFromVoucherUpdate(AfmVoucher voucher, List<AfmVoucherDetail> details, List<AfmVoucherAttach> attaches) {
        if (voucher == null) return;

        Optional<AfmLedger> ledgerOpt = ledgerRepository
                .findByVoucherSourceAndVoucherSourceLink("AFM_VOUCHER", String.valueOf(voucher.getId()));
        if (ledgerOpt.isEmpty() && voucher.getBuId() != null && voucher.getVoucherNo() != null) {
            ledgerOpt = ledgerRepository.findByBuIdAndVoucherNoIgnoreCase(voucher.getBuId(), voucher.getVoucherNo());
        }

        AfmLedger ledger = ledgerOpt.orElseGet(() -> AfmLedger.builder()
                .voucherSource("AFM_VOUCHER")
                .voucherSourceLink(String.valueOf(voucher.getId()))
                .build());

        ledger.setBgId(voucher.getBgId());
        ledger.setBuId(voucher.getBuId());
        ledger.setVoucherDate(voucher.getVoucherDate());
        ledger.setVoucherTypeId(voucher.getVoucherTypeId());
        ledger.setVoucherType(voucher.getVoucherType());
        ledger.setVoucherNo(voucher.getVoucherNo());
        ledger.setLocationId(voucher.getLocationId());
        ledger.setPaymentTypeId(voucher.getPaymentTypeId());
        ledger.setPaymentType(voucher.getPaymentType());
        ledger.setPaymentTo(voucher.getPaymentTo());
        ledger.setPaymentThroughId(voucher.getPaymentThroughId());
        ledger.setPaymentThrough(voucher.getPaymentThrough());
        ledger.setPeriodName(voucher.getPeriodName());
        ledger.setVoucherAmount(voucher.getVoucherAmount());
        ledger.setVoucherSource("AFM_VOUCHER");
        ledger.setVoucherSourceLink(String.valueOf(voucher.getId()));
        ledger.setVoucherStatus(voucher.getVoucherStatus());
        ledger.setApprovalCode(voucher.getApprovalCode());
        ledger.setBankAccount(voucher.getBankAccount());
        ledger.setChequeNo(voucher.getChequeNo());
        ledger.setChequeDate(voucher.getChequeDate());
        ledger.setChequeAmount(voucher.getChequeAmount());
        ledger.setIsPosted(voucher.getIsPosted());
        ledger.setIsReconciled(voucher.getIsReconciled());
        ledger.setNarration(voucher.getNarration());
        ledger.setStatus(voucher.getStatus() != null ? voucher.getStatus() : 1);
        ledger.setUpdatedApp(AppConstants.APP_NAME);

        AfmLedger savedLedger = ledgerRepository.save(ledger);

        // Delete previous detail rows and insert updated
        ledgerDetailRepository.deleteByVoucherId(savedLedger.getId());
        if (details != null && !details.isEmpty()) {
            List<AfmLedgerDetail> ledgerDetails = new ArrayList<>();
            for (AfmVoucherDetail vd : details) {
                AfmLedgerDetail ld = AfmLedgerDetail.builder()
                        .bgId(vd.getBgId() != null ? vd.getBgId() : savedLedger.getBgId())
                        .buId(vd.getBuId() != null ? vd.getBuId() : savedLedger.getBuId())
                        .voucherId(savedLedger.getId())
                        .coaId(vd.getCoaId())
                        .coaSourceType(vd.getCoaSourceType())
                        .coaSourceName(vd.getCoaSourceName())
                        .coaSourceId(vd.getCoaSourceId())
                        .costCenterId(vd.getCostCenterId())
                        .drAmount(vd.getDrAmount())
                        .crAmount(vd.getCrAmount())
                        .build();
                ld.setStatus(vd.getStatus() != null ? vd.getStatus() : 1);
                ld.setCreatedApp(AppConstants.APP_NAME);
                ld.setUpdatedApp(AppConstants.APP_NAME);
                ledgerDetails.add(ld);
            }
            ledgerDetailRepository.saveAll(ledgerDetails);
        }

        // Delete previous attachments and insert updated
        ledgerAttachRepository.deleteByVoucherId(savedLedger.getId());
        if (attaches != null && !attaches.isEmpty()) {
            List<AfmLedgerAttach> ledgerAttaches = new ArrayList<>();
            for (AfmVoucherAttach va : attaches) {
                AfmLedgerAttach la = AfmLedgerAttach.builder()
                        .bgId(va.getBgId() != null ? va.getBgId() : savedLedger.getBgId())
                        .buId(va.getBuId() != null ? va.getBuId() : savedLedger.getBuId())
                        .voucherId(savedLedger.getId())
                        .attachmentFileName(va.getAttachmentFileName())
                        .attachmentFile(va.getAttachmentFile())
                        .isActive(va.getIsActive())
                        .build();
                la.setStatus(va.getStatus() != null ? va.getStatus() : 1);
                la.setCreatedApp(AppConstants.APP_NAME);
                la.setUpdatedApp(AppConstants.APP_NAME);
                ledgerAttaches.add(la);
            }
            ledgerAttachRepository.saveAll(ledgerAttaches);
        }
    }

    @Override
    @Transactional
    public void syncFromVoucherDelete(Long voucherId, Long buId, String voucherNo) {
        if (voucherId == null) return;
        Optional<AfmLedger> ledgerOpt = ledgerRepository
                .findByVoucherSourceAndVoucherSourceLink("AFM_VOUCHER", String.valueOf(voucherId));
        if (ledgerOpt.isEmpty() && buId != null && voucherNo != null) {
            ledgerOpt = ledgerRepository.findByBuIdAndVoucherNoIgnoreCase(buId, voucherNo);
        }
        ledgerOpt.ifPresent(ledger -> {
            ledgerDetailRepository.deleteByVoucherId(ledger.getId());
            ledgerAttachRepository.deleteByVoucherId(ledger.getId());
            ledgerRepository.delete(ledger);
        });
    }

    private AfmLedger findLedger(Long id) {
        return ledgerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found with id: " + id));
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Group not found with id: " + bgId));
        businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found with id: " + buId));
    }

    private AfmLedgerDetail toDetailEntity(AfmLedgerDetailDto dto, Long ledgerId, Long bgId, Long buId) {
        AfmLedgerDetail entity = AfmLedgerDetail.builder()
                .bgId(dto.getBgId() != null ? dto.getBgId() : bgId)
                .buId(dto.getBuId() != null ? dto.getBuId() : buId)
                .voucherId(ledgerId)
                .coaId(dto.getCoaId())
                .coaSourceType(dto.getCoaSourceType())
                .coaSourceName(dto.getCoaSourceName())
                .coaSourceId(dto.getCoaSourceId())
                .costCenterId(dto.getCostCenterId())
                .drAmount(dto.getDrAmount() != null ? dto.getDrAmount() : BigDecimal.ZERO)
                .crAmount(dto.getCrAmount() != null ? dto.getCrAmount() : BigDecimal.ZERO)
                .build();
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return entity;
    }

    private AfmLedgerAttach toAttachEntity(AfmLedgerAttachDto dto, Long ledgerId, Long bgId, Long buId) {
        AfmLedgerAttach entity = AfmLedgerAttach.builder()
                .bgId(dto.getBgId() != null ? dto.getBgId() : bgId)
                .buId(dto.getBuId() != null ? dto.getBuId() : buId)
                .voucherId(ledgerId)
                .attachmentFileName(dto.getAttachmentFileName())
                .attachmentFile(dto.getAttachmentFile())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : 1)
                .build();
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return entity;
    }

    private AfmLedgerResponseDto toResponse(AfmLedger entity) {
        String bgName = entity.getBgId() != null
                ? businessGroupRepository.findById(entity.getBgId()).map(BusinessGroup::getGroupName).orElse(null)
                : null;
        String buName = entity.getBuId() != null
                ? businessUnitRepository.findById(entity.getBuId()).map(BusinessUnit::getBusinessUnit).orElse(null)
                : null;
        String voucherTypeName = entity.getVoucherTypeId() != null
                ? lookupValueRepository.findById(entity.getVoucherTypeId()).map(AfmLookupValue::getValue).orElse(entity.getVoucherType())
                : entity.getVoucherType();
        String paymentTypeName = entity.getPaymentTypeId() != null
                ? lookupValueRepository.findById(entity.getPaymentTypeId()).map(AfmLookupValue::getValue).orElse(entity.getPaymentType())
                : entity.getPaymentType();
        String paymentThroughName = entity.getPaymentThroughId() != null
                ? lookupValueRepository.findById(entity.getPaymentThroughId()).map(AfmLookupValue::getValue).orElse(entity.getPaymentThrough())
                : entity.getPaymentThrough();

        List<AfmLedgerDetailDto> details = ledgerDetailRepository.findByVoucherIdOrderByIdAsc(entity.getId())
                .stream()
                .map(this::toDetailDto)
                .toList();

        List<AfmLedgerAttachDto> attaches = ledgerAttachRepository.findByVoucherIdOrderByIdAsc(entity.getId())
                .stream()
                .map(this::toAttachDto)
                .toList();

        return AfmLedgerResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .buId(entity.getBuId())
                .businessGroupName(bgName)
                .businessUnitName(buName)
                .voucherDate(entity.getVoucherDate())
                .voucherTypeId(entity.getVoucherTypeId())
                .voucherTypeName(voucherTypeName)
                .voucherType(entity.getVoucherType())
                .voucherNo(entity.getVoucherNo())
                .locationId(entity.getLocationId())
                .paymentTypeId(entity.getPaymentTypeId())
                .paymentTypeName(paymentTypeName)
                .paymentType(entity.getPaymentType())
                .paymentTo(entity.getPaymentTo())
                .paymentThroughId(entity.getPaymentThroughId())
                .paymentThroughName(paymentThroughName)
                .paymentThrough(entity.getPaymentThrough())
                .periodName(entity.getPeriodName())
                .voucherAmount(entity.getVoucherAmount())
                .voucherSource(entity.getVoucherSource())
                .voucherSourceLink(entity.getVoucherSourceLink())
                .voucherStatus(entity.getVoucherStatus())
                .approvalCode(entity.getApprovalCode())
                .bankAccount(entity.getBankAccount())
                .chequeNo(entity.getChequeNo())
                .chequeDate(entity.getChequeDate())
                .chequeAmount(entity.getChequeAmount())
                .isPosted(entity.getIsPosted())
                .isReconciled(entity.getIsReconciled())
                .narration(entity.getNarration())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdEmpId(entity.getCreatedEmpId())
                .createdDatetime(entity.getCreatedDatetime())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedDatetime(entity.getUpdatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedApp(entity.getUpdatedApp())
                .ledgerDetails(details)
                .ledgerAttachments(attaches)
                .build();
    }

    private AfmLedgerDetailDto toDetailDto(AfmLedgerDetail entity) {
        String coaCode = null;
        String coaName = null;
        if (entity.getCoaId() != null) {
            Optional<AfmChartOfAccounts> coaOpt = chartOfAccountsRepository.findById(entity.getCoaId());
            if (coaOpt.isPresent()) {
                coaCode = coaOpt.get().getAccountCode();
                coaName = coaOpt.get().getAccountHeadName();
            }
        }
        String ccName = null;
        if (entity.getCostCenterId() != null) {
            ccName = costCenterRepository.findById(entity.getCostCenterId())
                    .map(AfmCostCenter::getCostCenter)
                    .orElse(null);
        }
        return AfmLedgerDetailDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .buId(entity.getBuId())
                .voucherId(entity.getVoucherId())
                .coaId(entity.getCoaId())
                .accountCode(coaCode)
                .accountHeadName(coaName)
                .coaSourceType(entity.getCoaSourceType())
                .coaSourceName(entity.getCoaSourceName())
                .coaSourceId(entity.getCoaSourceId())
                .costCenterId(entity.getCostCenterId())
                .costCenterName(ccName)
                .drAmount(entity.getDrAmount())
                .crAmount(entity.getCrAmount())
                .status(entity.getStatus())
                .build();
    }

    private AfmLedgerAttachDto toAttachDto(AfmLedgerAttach entity) {
        return AfmLedgerAttachDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .buId(entity.getBuId())
                .voucherId(entity.getVoucherId())
                .attachmentFileName(entity.getAttachmentFileName())
                .attachmentFile(entity.getAttachmentFile())
                .isActive(entity.getIsActive())
                .status(entity.getStatus())
                .build();
    }
}
