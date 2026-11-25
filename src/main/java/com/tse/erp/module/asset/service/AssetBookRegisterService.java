package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.dto.AssetBookRegisterDto;

import java.util.List;

public interface AssetBookRegisterService {

    List<AssetBookRegisterDto> getAllAssetBookRegisters(Long buId);

    AssetBookRegisterDto getAssetBookRegisterById(Long id);

    AssetBookRegisterDto createAssetBookRegister(AssetBookRegisterDto dto);

    AssetBookRegisterDto updateAssetBookRegister(Long id, AssetBookRegisterDto dto);

    AssetBookRegisterDto changeStatus(Long id, Integer status);

    void deleteAssetBookRegister(Long id);
}
