package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.ArticleWriteRequest;
import com.xyzy.domain.entity.Article;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.service.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/article")
public class AdminArticleController {
    private final AdminContentService service;
    public AdminArticleController(AdminContentService service) { this.service = service; }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('content:article:list')")
    public ResponseResult<PageVo> list(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       String title, String status) {
        return service.articlePage(pageNum, pageSize, title, status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('content:article:list')")
    public ResponseResult<Article> detail(@PathVariable Long id) { return service.articleDetail(id); }

    @PostMapping
    @PreAuthorize("hasAuthority('content:article:add')")
    public ResponseResult<Long> create(@Valid @RequestBody ArticleWriteRequest request) {
        return service.createArticle(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:article:edit')")
    public ResponseResult<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleWriteRequest request) {
        return service.updateArticle(id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('content:article:edit')")
    public ResponseResult<Void> status(@PathVariable Long id, @RequestParam String status) {
        return service.changeArticleStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:article:remove')")
    public ResponseResult<Void> delete(@PathVariable Long id) { return service.deleteArticle(id); }
}
