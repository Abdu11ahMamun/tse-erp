package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.dto.UserBuDto;
import com.tse.erp.module.admin.dto.UserBuRowDto;
import com.tse.erp.module.admin.dto.UserEmployeeDto;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.UserBuMap;
import com.tse.erp.module.admin.entity.UserEmplMap;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserBuMapRepository;
import com.tse.erp.module.admin.repository.UserEmplMapRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.admin.service.UserAssignmentService;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAssignmentServiceImpl implements UserAssignmentService {

    private final UserRepository userRepository;
    private final UserEmplMapRepository userEmplMapRepository;
    private final UserBuMapRepository userBuMapRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public UserEmployeeDto getEmployee(Long userId) {
        findUser(userId);

        List<UserEmplMap> rows = userEmplMapRepository
                .findByUserIdOrderByIdAsc(userId);
        UserEmplMap activeRow = rows.stream()
                .filter(row -> isActive(row.getStatus()))
                .findFirst()
                .orElse(null);

        UserEmployeeDto dto = new UserEmployeeDto();
        dto.setUserId(userId);
        if (activeRow != null) {
            dto.setEmployeeId(activeRow.getEmplId());
            employeeRepository.findById(activeRow.getEmplId())
                    .ifPresent(employee -> {
                        dto.setEmployeeName(employee.getEmployeeName());
                        dto.setEmployeeCode(employee.getEmployeeCode());
                    });
        }
        return dto;
    }

    @Override
    @Transactional
    public UserEmployeeDto saveEmployee(Long userId, UserEmployeeDto dto) {
        User user = findUser(userId);
        List<UserEmplMap> rows = userEmplMapRepository
                .findByUserIdOrderByIdAsc(userId);

        if (dto.getEmployeeId() == null) {
            throw new BadRequestException(
                    "A user must have an Employee. Select another Employee instead of removing it");
        }

        Long employeeId = dto.getEmployeeId();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        if (!isActive(employee.getStatus())) {
            throw new BadRequestException("Selected Employee is not active");
        }
        if (user.getBgId() == null) {
            throw new BadRequestException("User has no Business Group");
        }
        if (!user.getBgId().equals(employee.getBgId())) {
            throw new BadRequestException(
                    "Employee does not belong to the user's Business Group");
        }

        boolean assignedToAnotherUser = userEmplMapRepository
                .findByEmplIdAndStatus(employeeId, 1)
                .stream()
                .anyMatch(row -> !row.getUserId().equals(userId));
        if (assignedToAnotherUser) {
            throw new DuplicateResourceException(
                    "Employee is already assigned to another user");
        }

        UserEmplMap selectedRow = rows.isEmpty()
                ? new UserEmplMap()
                : rows.get(0);
        selectedRow.setUserId(userId);
        selectedRow.setEmplId(employeeId);
        selectedRow.setStatus(1);

        rows.stream()
                .skip(1)
                .forEach(row -> row.setStatus(0));

        List<UserEmplMap> rowsToSave = new ArrayList<>(rows);
        if (rows.isEmpty()) {
            rowsToSave.add(selectedRow);
        }
        userEmplMapRepository.saveAll(rowsToSave);
        return getEmployee(userId);
    }

    @Override
    public UserBuDto getBusinessUnits(Long userId) {
        User user = findUser(userId);
        List<UserBuRowDto> rows = userBuMapRepository
                .findByUserIdOrderByIdAsc(userId)
                .stream()
                .map(this::toRowDto)
                .collect(Collectors.toList());

        UserBuDto dto = new UserBuDto();
        dto.setUserId(userId);
        dto.setBgId(user.getBgId());
        dto.setBusinessUnits(rows);
        return dto;
    }

    @Override
    @Transactional
    public UserBuDto saveBusinessUnits(Long userId, UserBuDto dto) {
        User user = findUser(userId);
        if (user.getBgId() == null) {
            throw new BadRequestException("User has no Business Group");
        }

        List<UserBuRowDto> requestedRows = dto.getBusinessUnits() == null
                ? List.of()
                : dto.getBusinessUnits();
        Set<Long> requestedBuIds = new HashSet<>();

        for (UserBuRowDto row : requestedRows) {
            Long buId = row.getBuId();
            if (buId == null) {
                throw new BadRequestException("Business Unit cannot be empty");
            }

            StatusUtil.validate(row.getStatus());

            BusinessUnit businessUnit = businessUnitRepository.findById(buId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Business Unit not found with id: " + buId));

            if (!user.getBgId().equals(businessUnit.getBgId())) {
                throw new BadRequestException(
                        "Business Unit does not belong to the user's Business Group");
            }

            Integer status = row.getStatus();
            if ((status == null || status == 1)
                    && !isActive(businessUnit.getStatus())) {
                throw new BadRequestException(
                        "Selected Business Unit is not active");
            }

            if (!requestedBuIds.add(buId)) {
                throw new DuplicateResourceException(
                        "Duplicate Business Unit in request: " + buId);
            }
        }

        List<UserBuMap> currentRows = userBuMapRepository
                .findByUserIdOrderByIdAsc(userId);
        Map<Long, UserBuMap> rowsById = currentRows.stream()
                .collect(Collectors.toMap(UserBuMap::getId, row -> row));
        Set<Long> requestedRowIds = new HashSet<>();
        List<UserBuMap> rowsToSave = new ArrayList<>(currentRows);

        for (UserBuRowDto row : requestedRows) {
            UserBuMap entity;
            if (row.getId() != null) {
                entity = rowsById.get(row.getId());
                if (entity == null) {
                    throw new BadRequestException(
                            "Row id " + row.getId()
                                    + " does not belong to this User");
                }
                requestedRowIds.add(row.getId());
                entity.setBuId(row.getBuId());
                if (row.getStatus() != null) {
                    entity.setStatus(row.getStatus());
                }
            } else {
                entity = new UserBuMap();
                entity.setUserId(userId);
                entity.setBuId(row.getBuId());
                if (row.getStatus() != null) {
                    entity.setStatus(row.getStatus());
                }
                rowsToSave.add(entity);
            }
        }

        for (UserBuMap currentRow : currentRows) {
            if (!requestedRowIds.contains(currentRow.getId())) {
                currentRow.setStatus(0);
            }
        }

        Set<Long> finalBuIds = new HashSet<>();
        for (UserBuMap row : rowsToSave) {
            if (!finalBuIds.add(row.getBuId())) {
                throw new DuplicateResourceException(
                        "Business Unit already mapped to this User: "
                                + row.getBuId());
            }
        }

        userBuMapRepository.saveAll(rowsToSave);
        return getBusinessUnits(userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
    }

    private boolean isActive(Integer status) {
        return Integer.valueOf(1).equals(status);
    }

    private UserBuRowDto toRowDto(UserBuMap entity) {
        UserBuRowDto row = new UserBuRowDto();
        row.setId(entity.getId());
        row.setBuId(entity.getBuId());
        row.setStatus(entity.getStatus());
        return row;
    }
}
