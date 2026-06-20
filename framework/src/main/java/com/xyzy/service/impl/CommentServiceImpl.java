package com.xyzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyzy.constants.SystemConstants;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.AddCommentDto;
import com.xyzy.domain.entity.Comment;
import com.xyzy.domain.entity.User;
import com.xyzy.domain.vo.CommentVo;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.enums.AppHttpCodeEnum;
import com.xyzy.exception.SystemException;
import com.xyzy.mapper.CommentMapper;
import com.xyzy.service.CommentService;
import com.xyzy.service.UserService;
import com.xyzy.utils.BeanCopyUtils;
import com.xyzy.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserService userService;

    @Override
    public ResponseResult commentList(String commentType, Long articleId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConstants.COMMENT_TYPE_ARTICLE.equals(commentType), Comment::getArticleId, articleId);
        queryWrapper.eq(Comment::getType, commentType);
        queryWrapper.eq(Comment::getRootId, -1L);
        Page<Comment> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);
        List<CommentVo> commentVos = toCommentVoList(page.getRecords());
        return ResponseResult.okResult(new PageVo(commentVos, page.getTotal()));
    }

    @Override
    public ResponseResult addComment(AddCommentDto addCommentDto) {
        if (!StringUtils.hasText(addCommentDto.getContent())) {
            throw new SystemException(AppHttpCodeEnum.CONTENT_NOT_NULL);
        }
        Comment comment = BeanCopyUtils.copyBean(addCommentDto, Comment.class);
        comment.setCreateBy(SecurityUtils.getUserId());
        save(comment);
        return ResponseResult.okResult();
    }

    private List<CommentVo> toCommentVoList(List<Comment> list) {
        List<CommentVo> commentVos = BeanCopyUtils.copyBeanList(list, CommentVo.class);
        for (CommentVo commentVo : commentVos) {
            User user = userService.getById(commentVo.getCreateBy());
            if (Objects.nonNull(user)) {
                commentVo.setUsername(user.getNickName());
            }
            if (commentVo.getToCommentUserId() != null && commentVo.getToCommentUserId() != -1) {
                User toUser = userService.getById(commentVo.getToCommentUserId());
                if (Objects.nonNull(toUser)) {
                    commentVo.setToCommentUserName(toUser.getNickName());
                }
            }
        }
        commentVos = commentVos.stream()
                .filter(o -> o.getRootId() == null || o.getRootId() == -1)
                .collect(Collectors.toList());
        for (CommentVo commentVo : commentVos) {
            List<CommentVo> children = getChildren(commentVo.getId(), list);
            commentVo.setChildren(children);
        }
        return commentVos;
    }

    private List<CommentVo> getChildren(Long id, List<Comment> list) {
        return list.stream()
                .filter(o -> Objects.equals(o.getRootId(), id))
                .map(o -> {
                    CommentVo vo = BeanCopyUtils.copyBean(o, CommentVo.class);
                    User user = userService.getById(vo.getCreateBy());
                    if (Objects.nonNull(user)) {
                        vo.setUsername(user.getNickName());
                    }
                    if (vo.getToCommentUserId() != null && vo.getToCommentUserId() != -1) {
                        User toUser = userService.getById(vo.getToCommentUserId());
                        if (Objects.nonNull(toUser)) {
                            vo.setToCommentUserName(toUser.getNickName());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
