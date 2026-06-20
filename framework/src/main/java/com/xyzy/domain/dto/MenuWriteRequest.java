package com.xyzy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuWriteRequest(
        @NotBlank(message = "菜单名称不能为空") @Size(max = 64) String menuName,
        @NotNull(message = "父菜单不能为空") Long parentId,
        @NotNull(message = "显示顺序不能为空") Integer orderNum,
        @Size(max = 200) String path,
        @Size(max = 255) String component,
        @NotBlank(message = "菜单类型不能为空") String menuType,
        String visible,
        String status,
        @Size(max = 128) String perms,
        @Size(max = 100) String icon) {
}
