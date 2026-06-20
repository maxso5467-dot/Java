package com.xyzy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagWriteRequest(
        @NotBlank(message = "标签名称不能为空") @Size(max = 128) String name,
        @Size(max = 512) String remark) {
}
