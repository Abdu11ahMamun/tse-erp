package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.dto.UserTypeGroupDto;
import com.tse.erp.module.admin.service.UserTypeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-types/{userTypeId}/business-units")
@RequiredArgsConstructor
public class UserTypeGroupController {

    private final UserTypeGroupService userTypeGroupService;

    @GetMapping
    public ResponseEntity<UserTypeGroupDto> getByUserType(
            @PathVariable Long userTypeId) {
        return ResponseEntity.ok(
                userTypeGroupService.getByUserType(userTypeId));
    }

    @PutMapping
    public ResponseEntity<UserTypeGroupDto> save(
            @PathVariable Long userTypeId,
            @RequestBody UserTypeGroupDto dto) {
        return ResponseEntity.ok(
                userTypeGroupService.save(userTypeId, dto));
    }
}
