package com.xyzy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LinkWriteRequest(
        @NotBlank(message = "网站名称不能为空") @Size(max = 256) String name,
        String logo,
        @Size(max = 512) String description,
        @NotBlank(message = "网站地址不能为空") @Size(max = 128) String address,
        String status) {
}
