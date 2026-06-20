package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.RoleWriteRequest;
import com.xyzy.domain.entity.Role;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.service.AdminSystemService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@PreAuthorize("hasAuthority('system:role:manage')")
public class AdminRoleController {
    private final AdminSystemService service;
    public AdminRoleController(AdminSystemService service) { this.service = service; }

    @GetMapping("/list") public ResponseResult<PageVo> list(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize, String roleName, String status) { return service.rolePage(pageNum, pageSize, roleName, status); }
    @GetMapping("/all") public ResponseResult<List<Role>> all() { return service.roleList(); }
    @PostMapping public ResponseResult<Long> create(@Valid @RequestBody RoleWriteRequest request) { return service.createRole(request); }
    @PutMapping("/{id}") public ResponseResult<Void> update(@PathVariable Long id, @Valid @RequestBody RoleWriteRequest request) { return service.updateRole(id, request); }
    @DeleteMapping("/{id}") public ResponseResult<Void> delete(@PathVariable Long id) { return service.deleteRole(id); }
    @PutMapping("/{id}/status") public ResponseResult<Void> status(@PathVariable Long id, @RequestParam String status) { return service.changeRoleStatus(id, status); }
    @GetMapping("/{id}/menus") public ResponseResult<List<Long>> menuIds(@PathVariable Long id) { return service.roleMenuIds(id); }
    @PutMapping("/{id}/menus") public ResponseResult<Void> menus(@PathVariable Long id, @RequestBody List<Long> menuIds) { return service.assignRoleMenus(id, menuIds); }
}
