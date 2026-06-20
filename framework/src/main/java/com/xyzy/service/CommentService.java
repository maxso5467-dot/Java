package com.xyzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.Comment;

public interface CommentService extends IService<Comment> {
    ResponseResult commentList(String commentType, Long articleId, Integer pageNum, Integer pageSize);
    ResponseResult addComment(com.xyzy.domain.dto.AddCommentDto addCommentDto);
}
