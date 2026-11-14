package com.tse.erp.module.inventory.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemAttributeRequestDto;
import com.tse.erp.module.inventory.dto.InvItemAttributeResponseDto;
import com.tse.erp.module.inventory.entity.InvItemAttribute;
import com.tse.erp.module.inventory.repository.InvItemAttributeRepository;
import com.tse.erp.module.inventory.service.InvItemAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvItemAttributeServiceImpl implements InvItemAttributeService {

    private final InvItemAttributeRepository itemAttributeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<InvItemAttributeResponseDto> getAllItemAttributes(
            Long bgId, Long buId, String attributeName, String attributeCode, Integer status) {
        return itemAttributeRepository.searchItemAttributes(bgId, buId, attributeName, attributeCode, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvItemAttributeResponseDto getItemAttributeById(Long id) {
        return toResponse(findAttribute(id));
    }

    @Override
    public String generateNextAttributeCode(Long buId) {
        if (buId == null) {
            throw new BadRequestException("Business Unit ID is required to generate next Attribute Code");
        }
        long count = itemAttributeRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("ATTR-%05d", next);
        while (itemAttributeRepository.existsByBuIdAndAttributeCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("ATTR-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public InvItemAttributeResponseDto createItemAttribute(InvItemAttributeRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getAttributeName() == null || request.getAttributeName().trim().isEmpty()) {
            throw new BadRequestException("Attribute Name is required");
        }
        String attributeName = request.getAttributeName().trim();
        if (attributeName.length() > 150) {
            throw new BadRequestException("Attribute Name cannot exceed 150 characters");
        }

        if (itemAttributeRepository.existsByBuIdAndAttributeNameIgnoreCase(request.getBuId(), attributeName)) {
            throw new DuplicateResourceException("Attribute Name '" + attributeName + "' already exists for this Business Unit");
        }

        String attributeCode = request.getAttributeCode();
        if (attributeCode == null || attributeCode.trim().isEmpty()) {
            attributeCode = generateNextAttributeCode(request.getBuId());
        } else {
            attributeCode = attributeCode.trim();
            if (attributeCode.length() > 50) {
                throw new BadRequestException("Attribute Code cannot exceed 50 characters");
            }
            if (itemAttributeRepository.existsByBuIdAndAttributeCodeIgnoreCase(request.getBuId(), attributeCode)) {
                throw new DuplicateResourceException("Attribute Code '" + attributeCode + "' already exists for this Business Unit");
            }
        }

        StatusUtil.validate(request.getStatus());

        InvItemAttribute entity = InvItemAttribute.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .attributeName(attributeName)
                .attributeCode(attributeCode)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toResponse(itemAttributeRepository.save(entity));
    }

    @Override
    @Transactional
    public InvItemAttributeResponseDto updateItemAttribute(Long id, InvItemAttributeRequestDto request) {
        InvItemAttribute existing = findAttribute(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getAttributeName() == null || request.getAttributeName().trim().isEmpty()) {
            throw new BadRequestException("Attribute Name is required");
        }
        String attributeName = request.getAttributeName().trim();
        if (attributeName.length() > 150) {
            throw new BadRequestException("Attribute Name cannot exceed 150 characters");
        }

        itemAttributeRepository.findByBuIdAndAttributeNameIgnoreCase(request.getBuId(), attributeName)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Attribute Name '" + attributeName + "' already exists for this Business Unit");
                    }
                });

        if (request.getAttributeCode() == null || request.getAttributeCode().trim().isEmpty()) {
            throw new BadRequestException("Attribute Code cannot be empty");
        }
        String attributeCode = request.getAttributeCode().trim();
        if (attributeCode.length() > 50) {
            throw new BadRequestException("Attribute Code cannot exceed 50 characters");
        }

        itemAttributeRepository.findByBuIdAndAttributeCodeIgnoreCase(request.getBuId(), attributeCode)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Attribute Code '" + attributeCode + "' already exists for this Business Unit");
                    }
                });

        StatusUtil.validate(request.getStatus());

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setAttributeName(attributeName);
        existing.setAttributeCode(attributeCode);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return toResponse(itemAttributeRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteItemAttribute(Long id) {
        InvItemAttribute existing = findAttribute(id);
        itemAttributeRepository.delete(existing);
    }

    private InvItemAttribute findAttribute(Long id) {
        return itemAttributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item Attribute not found with id: " + id));
    }

    private InvItemAttributeResponseDto toResponse(InvItemAttribute entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String createdUserName = entity.getCreatedUserId() != null
                ? userRepository.findById(entity.getCreatedUserId()).map(User::getUserName).orElse(null)
                : null;

        String createdEmpName = null;
        if (entity.getCreatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getCreatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                createdEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        String updatedUserName = entity.getUpdatedUserId() != null
                ? userRepository.findById(entity.getUpdatedUserId()).map(User::getUserName).orElse(null)
                : null;

        String updatedEmpName = null;
        if (entity.getUpdatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getUpdatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                updatedEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        return InvItemAttributeResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .attributeName(entity.getAttributeName())
                .attributeCode(entity.getAttributeCode())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdUserName(createdUserName)
                .createdEmpId(entity.getCreatedEmpId())
                .createdEmpName(createdEmpName)
                .createdDatetime(entity.getCreatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedUserName(updatedUserName)
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedEmpName(updatedEmpName)
                .updatedDatetime(entity.getUpdatedDatetime())
                .updatedApp(entity.getUpdatedApp())
                .build();
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

