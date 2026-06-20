package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.LoginRequest;
import com.xyzy.domain.vo.AdminLoginVo;
import com.xyzy.service.AdminLoginService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminLoginController {
    private final AdminLoginService service;

    public AdminLoginController(AdminLoginService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseResult<AdminLoginVo> login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/logout")
    public ResponseResult<Void> logout() {
        return service.logout();
    }

    @GetMapping("/getInfo")
    public ResponseResult<AdminLoginVo> getInfo() {
        return service.getInfo();
    }
}
