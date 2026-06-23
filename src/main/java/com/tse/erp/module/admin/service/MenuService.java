package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.entity.Menu;
import java.util.List;

public interface MenuService {

    List<Menu> getAllMenus();

    List<Menu> getMenusByModuleId(Long moduleId);

    Menu getMenuById(Long id);

    Menu createMenu(Menu menu);

    Menu updateMenu(Long id, Menu menu);

    void deleteMenu(Long id);
}