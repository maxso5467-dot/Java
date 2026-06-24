package com.xyzy.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.Article;
import com.xyzy.domain.vo.ArticleListVo;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.mapper.ArticleMapper;
import com.xyzy.service.impl.ArticleServiceImpl;
import com.xyzy.utils.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArticleServiceImplTest {

    @Test
    void articleListAcceptsLongViewCountsFromRedis() {
        ArticleMapper articleMapper = mock(ArticleMapper.class);
        CategoryService categoryService = mock(CategoryService.class);
        RedisCache redisCache = mock(RedisCache.class);

        Article article = new Article()
                .setId(1L)
                .setTitle("Spring Boot")
                .setSummary("summary")
                .setCategoryId(2L)
                .setViewCount(1L);

        when(articleMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<Article> page = invocation.getArgument(0);
            page.setRecords(List.of(article));
            page.setTotal(1);
            return page;
        });
        when(redisCache.getCacheMapValue("article:viewCount", "1")).thenReturn(120L);

        ArticleServiceImpl service = new ArticleServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", articleMapper);
        ReflectionTestUtils.setField(service, "categoryService", categoryService);
        ReflectionTestUtils.setField(service, "redisCache", redisCache);

        ResponseResult result = assertDoesNotThrow(() -> service.articleList(1, 10, null));

        assertEquals(200, result.getCode());
        PageVo page = (PageVo) result.getData();
        ArticleListVo vo = (ArticleListVo) page.getRows().get(0);
        assertEquals(120L, vo.getViewCount());
    }
}
