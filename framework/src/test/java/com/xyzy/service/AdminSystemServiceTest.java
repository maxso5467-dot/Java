package com.xyzy.service;

import com.xyzy.domain.dto.AdminUserWriteRequest;
import com.xyzy.domain.entity.User;
import com.xyzy.exception.SystemException;
import com.xyzy.mapper.MenuMapper;
import com.xyzy.mapper.RoleMapper;
import com.xyzy.mapper.RoleMenuMapper;
import com.xyzy.mapper.UserMapper;
import com.xyzy.mapper.UserRoleMapper;
import com.xyzy.security.LoginSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminSystemServiceTest {

    @Test
    void encodesPasswordBeforeCreatingUser() {
        UserMapper users = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("Secret123!")).thenReturn("bcrypt-value");
        AdminSystemService service = service(users, encoder);

        service.createUser(new AdminUserWriteRequest(
                "new-user", "New User", "Secret123!", "new@example.com", null, "2", "0", List.of()));

        verify(encoder).encode("Secret123!");
        verify(users).insert(any(User.class));
    }

    @Test
    void protectsBuiltInAdministratorFromDeletion() {
        AdminSystemService service = service(mock(UserMapper.class), mock(PasswordEncoder.class));
        assertThrows(SystemException.class, () -> service.deleteUser(1L));
    }

    private AdminSystemService service(UserMapper users, PasswordEncoder encoder) {
        return new AdminSystemService(users, mock(RoleMapper.class), mock(MenuMapper.class),
                mock(UserRoleMapper.class), mock(RoleMenuMapper.class), encoder, mock(LoginSessionService.class));
    }
}
