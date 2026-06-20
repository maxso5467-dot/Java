package com.xyzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.Category;

public interface CategoryService extends IService<Category> {
    ResponseResult getCategoryList();
}
