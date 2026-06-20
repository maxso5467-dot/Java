package com.xyzy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailVo {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private String categoryName;
    private Long viewCount;
    private String isComment;
    private Date createTime;
}
