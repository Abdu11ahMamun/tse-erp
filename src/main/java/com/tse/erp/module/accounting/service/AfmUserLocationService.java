package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmUserLocationRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserLocationResponseDto;

import java.util.List;

public interface AfmUserLocationService {

    List<AfmUserLocationResponseDto> getAllUserLocations(Long bgId, Long buId, String location, Integer status);

    AfmUserLocationResponseDto getUserLocationById(Long id);

    AfmUserLocationResponseDto createUserLocation(AfmUserLocationRequestDto request);

    AfmUserLocationResponseDto updateUserLocation(Long id, AfmUserLocationRequestDto request);

    void deleteUserLocation(Long id);
}

