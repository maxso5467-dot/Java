package com.xyzy.controller;

import com.xyzy.service.AdminSystemService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class AdminSystemControllerTest {

    @Test
    void userControllerRequiresUserManagementPermission() {
        AdminUserController controller = new AdminUserController(mock(AdminSystemService.class));
        assertNotNull(controller);
        PreAuthorize guard = AdminUserController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(guard);
        assertEquals("hasAuthority('system:user:manage')", guard.value());
    }
}
