package com.xyzy.service;

import com.xyzy.domain.dto.ArticleWriteRequest;
import com.xyzy.domain.entity.ArticleTag;
import com.xyzy.domain.entity.Category;
import com.xyzy.exception.SystemException;
import com.xyzy.mapper.ArticleMapper;
import com.xyzy.mapper.ArticleTagMapper;
import com.xyzy.mapper.CategoryMapper;
import com.xyzy.mapper.LinkMapper;
import com.xyzy.mapper.TagMapper;
import com.xyzy.utils.RedisCache;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminContentServiceTest {

    @Test
    void rejectsDeletingCategoryUsedByAnArticle() {
        ArticleMapper articles = mock(ArticleMapper.class);
        AdminContentService service = service(articles, mock(ArticleTagMapper.class));
        when(articles.selectCount(any())).thenReturn(1L);

        assertThrows(SystemException.class, () -> service.deleteCategory(2L));
    }

    @Test
    void savesEveryDistinctArticleTag() {
        ArticleMapper articles = mock(ArticleMapper.class);
        ArticleTagMapper articleTags = mock(ArticleTagMapper.class);
        CategoryMapper categories = mock(CategoryMapper.class);
        when(categories.selectById(1L)).thenReturn(new Category().setId(1L));
        AdminContentService service = service(articles, articleTags, categories);
        ArticleWriteRequest request = new ArticleWriteRequest(
                "title", "content", "summary", 1L, null, "0", "1", "1", List.of(2L, 2L, 3L));

        service.createArticle(request);

        verify(articleTags, times(2)).insert(any(ArticleTag.class));
    }

    private AdminContentService service(ArticleMapper articles, ArticleTagMapper articleTags) {
        return service(articles, articleTags, mock(CategoryMapper.class));
    }

    private AdminContentService service(ArticleMapper articles, ArticleTagMapper articleTags, CategoryMapper categories) {
        return new AdminContentService(articles, categories, mock(TagMapper.class),
                mock(LinkMapper.class), articleTags, mock(RedisCache.class));
    }
}
