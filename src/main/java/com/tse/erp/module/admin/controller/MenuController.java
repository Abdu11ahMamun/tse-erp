    package com.tse.erp.module.admin.controller;

    import com.tse.erp.module.admin.entity.Menu;
    import com.tse.erp.module.admin.service.MenuService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import java.util.List;

    @RestController
    @RequestMapping("/api/v1/menus")
    @RequiredArgsConstructor
    @CrossOrigin(origins = "*")
    public class MenuController {

        private final MenuService menuService;

        @GetMapping
        public ResponseEntity<List<Menu>> getAllMenus() {
            return ResponseEntity.ok(menuService.getAllMenus());
        }

        // Module id diye filter
        @GetMapping("/module/{moduleId}")
        public ResponseEntity<List<Menu>> getMenusByModule(
                @PathVariable Long moduleId) {
            return ResponseEntity.ok(
                    menuService.getMenusByModuleId(moduleId));
        }

        @GetMapping("/{id}")
        public ResponseEntity<Menu> getMenuById(@PathVariable Long id) {
            return ResponseEntity.ok(menuService.getMenuById(id));
        }

        @PostMapping
        public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(menuService.createMenu(menu));
        }

        @PutMapping("/{id}")
        public ResponseEntity<Menu> updateMenu(
                @PathVariable Long id,
                @RequestBody Menu menu) {
            return ResponseEntity.ok(menuService.updateMenu(id, menu));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
            menuService.deleteMenu(id);
            return ResponseEntity.noContent().build();
        }
    }