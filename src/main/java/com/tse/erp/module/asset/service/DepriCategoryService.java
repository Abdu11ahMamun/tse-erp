package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.entity.DepriCategory;

import java.util.List;

public interface DepriCategoryService {

    List<DepriCategory> getAllDepriCategories(Long buId);

    DepriCategory getDepriCategoryById(Long id);

    DepriCategory createDepriCategory(DepriCategory depriCategory);

    DepriCategory updateDepriCategory(Long id, DepriCategory depriCategory);

    void deleteDepriCategory(Long id);
}
