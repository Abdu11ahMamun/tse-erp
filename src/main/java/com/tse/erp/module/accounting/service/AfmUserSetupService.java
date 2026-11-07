package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmUserSetupRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserSetupResponseDto;
import com.tse.erp.module.accounting.dto.UserOptionDto;

import java.util.List;

public interface AfmUserSetupService {

    List<AfmUserSetupResponseDto> getAllUserSetups(Long bgId, Long buId, Long userId, Integer status);

    AfmUserSetupResponseDto getUserSetupById(Long id);

    AfmUserSetupResponseDto createUserSetup(AfmUserSetupRequestDto request);

    AfmUserSetupResponseDto updateUserSetup(Long id, AfmUserSetupRequestDto request);

    void deleteUserSetup(Long id);

    List<UserOptionDto> getAvailableUsers(Long bgId, Long buId);
}

