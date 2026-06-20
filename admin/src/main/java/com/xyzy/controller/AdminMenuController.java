package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.MenuWriteRequest;
import com.xyzy.domain.vo.MenuTreeVo;
import com.xyzy.service.AdminSystemService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
@PreAuthorize("hasAuthority('system:menu:manage')")
public class AdminMenuController {
    private final AdminSystemService service;
    public AdminMenuController(AdminSystemService service) { this.service = service; }

    @GetMapping("/tree") public ResponseResult<List<MenuTreeVo>> tree() { return service.menuTree(); }
    @PostMapping public ResponseResult<Long> create(@Valid @RequestBody MenuWriteRequest request) { return service.createMenu(request); }
    @PutMapping("/{id}") public ResponseResult<Void> update(@PathVariable Long id, @Valid @RequestBody MenuWriteRequest request) { return service.updateMenu(id, request); }
    @DeleteMapping("/{id}") public ResponseResult<Void> delete(@PathVariable Long id) { return service.deleteMenu(id); }
}
