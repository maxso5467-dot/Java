package com.xyzy.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminUserWriteRequest(
        @NotBlank(message = "用户名不能为空") @Size(max = 64) String userName,
        @NotBlank(message = "昵称不能为空") @Size(max = 64) String nickName,
        String password,
        @Email(message = "邮箱格式不正确") @Size(max = 64) String email,
        @Size(max = 32) String phonenumber,
        String sex,
        String status,
        List<Long> roleIds) {
}
