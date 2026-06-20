package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.AddCommentDto;
import com.xyzy.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/commentList")
    public ResponseResult commentList(Long articleId, Integer pageNum, Integer pageSize) {
        return commentService.commentList("0", articleId, pageNum, pageSize);
    }

    @GetMapping("/linkCommentList")
    public ResponseResult linkCommentList(Integer pageNum, Integer pageSize) {
        return commentService.commentList("1", null, pageNum, pageSize);
    }

    @PostMapping
    public ResponseResult addComment(@RequestBody AddCommentDto addCommentDto) {
        return commentService.addComment(addCommentDto);
    }
}
