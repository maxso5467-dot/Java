package com.xyzy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentDto {
    private Long articleId;
    private String type;
    private Long rootId;
    private String content;
    private Long toCommentUserId;
    private Long toCommentId;
}
