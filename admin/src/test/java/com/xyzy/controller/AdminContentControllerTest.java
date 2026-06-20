package com.xyzy.controller;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.service.AdminContentService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminContentControllerTest {

    @Test
    void articleListDelegatesAndRequiresListPermission() throws Exception {
        AdminContentService service = mock(AdminContentService.class);
        PageVo page = new PageVo();
        when(service.articlePage(1, 10, null, null)).thenReturn(ResponseResult.okResult(page));
        AdminArticleController controller = new AdminArticleController(service);

        assertEquals(page, controller.list(1, 10, null, null).getData());
        PreAuthorize guard = AdminArticleController.class
                .getMethod("list", int.class, int.class, String.class, String.class)
                .getAnnotation(PreAuthorize.class);
        assertNotNull(guard);
        assertEquals("hasAuthority('content:article:list')", guard.value());
    }
}
