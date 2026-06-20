package com.xyzy.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("t_article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;
    private String summary;

    private Long categoryId;

    @TableField(exist = false)
    private String categoryName;

    private String thumbnail;

    private String isTop;

    private String status;

    private Long viewCount;

    private String isComment;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Integer delFlag;
}
