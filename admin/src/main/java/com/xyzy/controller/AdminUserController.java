package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.AdminUserWriteRequest;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.service.AdminSystemService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@PreAuthorize("hasAuthority('system:user:manage')")
public class AdminUserController {
    private final AdminSystemService service;
    public AdminUserController(AdminSystemService service) { this.service = service; }

    @GetMapping("/list")
    public ResponseResult<PageVo> list(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       String userName, String status) {
        return service.userPage(pageNum, pageSize, userName, status);
    }
    @PostMapping public ResponseResult<Long> create(@Valid @RequestBody AdminUserWriteRequest request) { return service.createUser(request); }
    @PutMapping("/{id}") public ResponseResult<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUserWriteRequest request) { return service.updateUser(id, request); }
    @DeleteMapping("/{id}") public ResponseResult<Void> delete(@PathVariable Long id) { return service.deleteUser(id); }
    @PutMapping("/{id}/status") public ResponseResult<Void> status(@PathVariable Long id, @RequestParam String status) { return service.changeUserStatus(id, status); }
    @GetMapping("/{id}/roles") public ResponseResult<List<Long>> roleIds(@PathVariable Long id) { return service.userRoleIds(id); }
    @PutMapping("/{id}/roles") public ResponseResult<Void> roles(@PathVariable Long id, @RequestBody List<Long> roleIds) { return service.assignUserRoles(id, roleIds); }
}
