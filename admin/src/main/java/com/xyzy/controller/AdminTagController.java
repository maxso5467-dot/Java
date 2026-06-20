package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.TagWriteRequest;
import com.xyzy.domain.entity.Tag;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.service.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/content/tag")
@PreAuthorize("hasAuthority('content:tag:manage')")
public class AdminTagController {
    private final AdminContentService service;
    public AdminTagController(AdminContentService service) { this.service = service; }

    @GetMapping("/list") public ResponseResult<PageVo> list(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize, String name) { return service.tagPage(pageNum, pageSize, name); }
    @GetMapping("/all") public ResponseResult<List<Tag>> all() { return service.tagList(); }
    @PostMapping public ResponseResult<Long> create(@Valid @RequestBody TagWriteRequest request) { return service.createTag(request); }
    @PutMapping("/{id}") public ResponseResult<Void> update(@PathVariable Long id, @Valid @RequestBody TagWriteRequest request) { return service.updateTag(id, request); }
    @DeleteMapping("/{id}") public ResponseResult<Void> delete(@PathVariable Long id) { return service.deleteTag(id); }
}
