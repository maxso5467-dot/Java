package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.CategoryWriteRequest;
import com.xyzy.domain.entity.Category;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.service.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/content/category")
@PreAuthorize("hasAuthority('content:category:manage')")
public class AdminCategoryController {
    private final AdminContentService service;
    public AdminCategoryController(AdminContentService service) { this.service = service; }

    @GetMapping("/list")
    public ResponseResult<PageVo> list(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize, String name) {
        return service.categoryPage(pageNum, pageSize, name);
    }
    @GetMapping("/all") public ResponseResult<List<Category>> all() { return service.categoryList(); }
    @PostMapping public ResponseResult<Long> create(@Valid @RequestBody CategoryWriteRequest request) { return service.createCategory(request); }
    @PutMapping("/{id}") public ResponseResult<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryWriteRequest request) { return service.updateCategory(id, request); }
    @DeleteMapping("/{id}") public ResponseResult<Void> delete(@PathVariable Long id) { return service.deleteCategory(id); }
}
