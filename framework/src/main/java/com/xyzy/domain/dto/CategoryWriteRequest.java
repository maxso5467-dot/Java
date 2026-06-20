package com.xyzy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryWriteRequest(
        @NotBlank(message = "分类名称不能为空") @Size(max = 128) String name,
        Long pid,
        @Size(max = 512) String description,
        String status) {
}
