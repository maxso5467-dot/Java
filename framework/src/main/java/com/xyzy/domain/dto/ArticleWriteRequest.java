package com.xyzy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ArticleWriteRequest(
        @NotBlank(message = "文章标题不能为空") @Size(max = 256) String title,
        @NotBlank(message = "文章内容不能为空") String content,
        @NotBlank(message = "文章摘要不能为空") @Size(max = 1024) String summary,
        @NotNull(message = "文章分类不能为空") Long categoryId,
        String thumbnail,
        String isTop,
        String status,
        String isComment,
        List<Long> tagIds) {
}
