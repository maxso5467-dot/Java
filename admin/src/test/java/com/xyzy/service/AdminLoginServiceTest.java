package com.xyzy.service;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.LoginRequest;
import com.xyzy.domain.entity.LoginUser;
import com.xyzy.domain.entity.User;
import com.xyzy.domain.vo.AdminLoginVo;
import com.xyzy.exception.SystemException;
import com.xyzy.mapper.RoleMapper;
import com.xyzy.security.LoginSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLoginServiceTest {

    @Test
    void savesAdminSessionAndReturnsToken() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        LoginSessionService sessions = mock(LoginSessionService.class);
        RoleMapper roles = mock(RoleMapper.class);
        AdminLoginService service = new AdminLoginService(manager, sessions, roles);
        LoginUser principal = new LoginUser(new User().setId(1L).setUserName("admin"), List.of("system:user:manage"));
        when(manager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(roles.selectRoleKeysByUserId(1L)).thenReturn(List.of("admin"));

        ResponseResult<AdminLoginVo> result = service.login(new LoginRequest("admin", "Admin123!"));

        assertNotNull(result.getData());
        assertFalse(result.getData().getToken().isBlank());
        verify(sessions).save("admin", principal);
    }

    @Test
    void rejectsAuthenticatedUserWithoutAdminRole() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        LoginSessionService sessions = mock(LoginSessionService.class);
        RoleMapper roles = mock(RoleMapper.class);
        AdminLoginService service = new AdminLoginService(manager, sessions, roles);
        LoginUser principal = new LoginUser(new User().setId(2L), List.of());
        when(manager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        when(roles.selectRoleKeysByUserId(2L)).thenReturn(List.of("reader"));

        assertThrows(SystemException.class,
                () -> service.login(new LoginRequest("reader", "password")));
    }
}
