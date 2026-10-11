package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.dto.AssetReceiveDto;

import java.util.List;

public interface AssetReceiveService {

    List<AssetReceiveDto> getAllAssetReceives(Long buId);

    AssetReceiveDto getAssetReceiveById(Long id);

    AssetReceiveDto createAssetReceive(AssetReceiveDto dto);

    AssetReceiveDto updateAssetReceive(Long id, AssetReceiveDto dto);

    AssetReceiveDto changeStatus(Long id, Integer status);
}
