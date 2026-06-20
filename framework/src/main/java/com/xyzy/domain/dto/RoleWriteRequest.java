package com.xyzy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoleWriteRequest(
        @NotBlank(message = "角色名称不能为空") @Size(max = 64) String roleName,
        @NotBlank(message = "角色权限字符串不能为空") @Size(max = 64) String roleKey,
        @NotNull(message = "显示顺序不能为空") Integer roleSort,
        String status,
        List<Long> menuIds) {
}
