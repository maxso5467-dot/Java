package com.xyzy;

import com.xyzy.controller.ArticleController;
import com.xyzy.domain.ResponseResult;
import com.xyzy.service.ArticleService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlogRegressionTest {

    @Test
    void publicHotArticleEndpointStillDelegatesToExistingService() {
        ArticleService service = mock(ArticleService.class);
        ResponseResult<?> expected = ResponseResult.okResult();
        when(service.hotArticleList()).thenReturn(expected);

        ResponseResult<?> actual = new ArticleController(service).hotArticleList();

        assertSame(expected, actual);
    }
}
