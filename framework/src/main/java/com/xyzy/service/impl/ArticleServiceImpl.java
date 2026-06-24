package com.xyzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyzy.constants.SystemConstants;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.Article;
import com.xyzy.domain.entity.Category;
import com.xyzy.domain.vo.ArticleDetailVo;
import com.xyzy.domain.vo.ArticleListVo;
import com.xyzy.domain.vo.HotArticleVo;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.mapper.ArticleMapper;
import com.xyzy.service.ArticleService;
import com.xyzy.service.CategoryService;
import com.xyzy.utils.BeanCopyUtils;
import com.xyzy.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult hotArticleList() {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        queryWrapper.orderByDesc(Article::getViewCount);
        Page<Article> page = new Page<>(1, 10);
        page(page, queryWrapper);
        List<Article> articles = page.getRecords();
        List<HotArticleVo> vos = BeanCopyUtils.copyBeanList(articles, HotArticleVo.class);
        return ResponseResult.okResult(vos);
    }

    @Override
    public ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        queryWrapper.eq(Objects.nonNull(categoryId) && categoryId > 0, Article::getCategoryId, categoryId);
        queryWrapper.orderByDesc(Article::getIsTop);
        Page<Article> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);
        List<Article> articles = page.getRecords();
        articles = articles.stream().map(article -> {
            Number viewCount = redisCache.getCacheMapValue(SystemConstants.REDIS_ARTICLE_VIEWCOUNT, article.getId().toString());
            if (Objects.nonNull(viewCount)) {
                article.setViewCount(viewCount.longValue());
            }
            return article;
        }).collect(Collectors.toList());
        List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(articles, ArticleListVo.class);
        for (ArticleListVo vo : articleListVos) {
            Category category = categoryService.getById(vo.getCategoryId());
            if (Objects.nonNull(category)) {
                vo.setCategoryName(category.getName());
            }
        }
        PageVo pageVo = new PageVo(articleListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    @Override
    public ResponseResult getArticleDetail(Long id) {
        Article article = getById(id);
        Number viewCount = redisCache.getCacheMapValue(SystemConstants.REDIS_ARTICLE_VIEWCOUNT, id.toString());
        if (Objects.nonNull(viewCount)) {
            article.setViewCount(viewCount.longValue());
        }
        ArticleDetailVo articleDetailVo = BeanCopyUtils.copyBean(article, ArticleDetailVo.class);
        Category category = categoryService.getById(articleDetailVo.getCategoryId());
        if (Objects.nonNull(category)) {
            articleDetailVo.setCategoryName(category.getName());
        }
        return ResponseResult.okResult(articleDetailVo);
    }

    @Override
    public ResponseResult updateViewCount(Long id) {
        redisCache.incrementCacheMapValue(SystemConstants.REDIS_ARTICLE_VIEWCOUNT, id.toString(), 1);
        return ResponseResult.okResult();
    }
}
