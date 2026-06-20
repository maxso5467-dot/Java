package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.LinkWriteRequest;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.service.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/link")
@PreAuthorize("hasAuthority('content:link:manage')")
public class AdminLinkController {
    private final AdminContentService service;
    public AdminLinkController(AdminContentService service) { this.service = service; }

    @GetMapping("/list") public ResponseResult<PageVo> list(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize, String name, String status) { return service.linkPage(pageNum, pageSize, name, status); }
    @PostMapping public ResponseResult<Long> create(@Valid @RequestBody LinkWriteRequest request) { return service.createLink(request); }
    @PutMapping("/{id}") public ResponseResult<Void> update(@PathVariable Long id, @Valid @RequestBody LinkWriteRequest request) { return service.updateLink(id, request); }
    @DeleteMapping("/{id}") public ResponseResult<Void> delete(@PathVariable Long id) { return service.deleteLink(id); }
}
