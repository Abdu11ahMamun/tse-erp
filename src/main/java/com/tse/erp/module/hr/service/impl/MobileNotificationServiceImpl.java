package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.dto.MobileNotificationCreateDto;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.entity.MobileNotification;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.hr.repository.MobileNotificationRepository;
import com.tse.erp.module.hr.service.MobileNotificationService;
import com.tse.erp.module.hr.validation.HrLookupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MobileNotificationServiceImpl
        implements MobileNotificationService {

    private final MobileNotificationRepository mobileNotificationRepository;
    private final EmployeeRepository employeeRepository;
    private final HrLookupValidator hrLookupValidator;

    @Override
    public List<MobileNotification> getAllMobileNotifications(
            Long buId, Long emplId) {
        if (buId != null && emplId != null) {
            return mobileNotificationRepository
                    .findByBuIdAndEmplIdOrderByIdDesc(buId, emplId);
        }
        if (buId != null) {
            return mobileNotificationRepository.findByBuIdOrderByIdDesc(buId);
        }
        if (emplId != null) {
            return mobileNotificationRepository
                    .findByEmplIdOrderByIdDesc(emplId);
        }
        return mobileNotificationRepository.findAllByOrderByIdDesc();
    }

    @Override
    public MobileNotification getMobileNotificationById(Long id) {
        return mobileNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mobile Notification not found with id: " + id));
    }

    @Override
    @Transactional
    public List<MobileNotification> createMobileNotifications(
            MobileNotificationCreateDto dto) {
        if (dto.getEmployeeIds() == null
                || dto.getEmployeeIds().isEmpty()) {
            throw new BadRequestException("Employee cannot be empty");
        }
        List<Long> employeeIds = new ArrayList<>(
                new LinkedHashSet<>(dto.getEmployeeIds()));
        StatusUtil.validate(dto.getStatus());
        String title = cleanTitle(dto.getTitle());
        String body = cleanBody(dto.getBody());

        List<Employee> employees = new ArrayList<>();
        for (Long employeeId : employeeIds) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with id: " + employeeId));
            if (!Integer.valueOf(1).equals(employee.getStatus())) {
                throw new BadRequestException(
                        "Selected Employee is not active: " + employeeId);
            }
            if (dto.getBuIds() != null && !dto.getBuIds().isEmpty()
                    && !dto.getBuIds().contains(employee.getBuId())) {
                throw new BadRequestException(
                        "Employee " + employeeId
                                + " does not belong to the selected Business Units");
            }
            if (dto.getDeptIds() != null && !dto.getDeptIds().isEmpty()
                    && !dto.getDeptIds().contains(employee.getDeptId())) {
                throw new BadRequestException(
                        "Employee " + employeeId
                                + " does not belong to the selected Departments");
            }
            hrLookupValidator.validate(
                    dto.getNotificationTypeId(), employee.getBgId(),
                    "Notification Type", "Notification type");
            employees.add(employee);
        }

        List<MobileNotification> notifications = new ArrayList<>();
        for (Employee employee : employees) {
            MobileNotification notification = new MobileNotification();
            notification.setBuId(employee.getBuId());
            notification.setDeptId(employee.getDeptId());
            notification.setEmplId(employee.getId());
            notification.setNotificationTypeId(dto.getNotificationTypeId());
            notification.setTitle(title);
            notification.setBody(body);
            if (dto.getStatus() != null) {
                notification.setStatus(dto.getStatus());
            }
            notifications.add(notification);
        }
        return mobileNotificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public MobileNotification updateMobileNotification(
            Long id, MobileNotification row) {
        MobileNotification existing = getMobileNotificationById(id);
        StatusUtil.validate(row.getStatus());
        Employee employee = employeeRepository
                .findById(existing.getEmplId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + existing.getEmplId()));
        hrLookupValidator.validate(
                row.getNotificationTypeId(), employee.getBgId(),
                "Notification Type", "Notification type");

        existing.setNotificationTypeId(row.getNotificationTypeId());
        existing.setTitle(cleanTitle(row.getTitle()));
        existing.setBody(cleanBody(row.getBody()));
        if (row.getStatus() != null) {
            existing.setStatus(row.getStatus());
        }
        return mobileNotificationRepository.save(existing);
    }

    @Override
    public void deleteMobileNotification(Long id) {
        MobileNotification existing = getMobileNotificationById(id);
        mobileNotificationRepository.delete(existing);
    }

    private String cleanTitle(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Title cannot be empty");
        }
        String title = value.trim();
        if (title.length() < 2) {
            throw new BadRequestException(
                    "Title must be at least 2 characters");
        }
        if (title.length() > 150) {
            throw new BadRequestException(
                    "Title cannot exceed 150 characters");
        }
        if (title.contains("<") || title.contains(">")) {
            throw new BadRequestException(
                    "Title contains unsupported characters");
        }
        return title;
    }

    private String cleanBody(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Body cannot be empty");
        }
        String body = value.trim();
        if (body.length() > 1000) {
            throw new BadRequestException(
                    "Body cannot exceed 1000 characters");
        }
        if (body.contains("<") || body.contains(">")) {
            throw new BadRequestException(
                    "Body contains unsupported characters");
        }
        return body;
    }
}
