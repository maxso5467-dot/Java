package com.xyzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyzy.constants.SystemConstants;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.Category;
import com.xyzy.domain.vo.CategoryVo;
import com.xyzy.mapper.CategoryMapper;
import com.xyzy.service.CategoryService;
import com.xyzy.utils.BeanCopyUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public ResponseResult getCategoryList() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getStatus, SystemConstants.CATEGORY_STATUS_NORMAL);
        List<Category> categories = list(queryWrapper);
        List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(categories, CategoryVo.class);
        return ResponseResult.okResult(categoryVos);
    }
}
