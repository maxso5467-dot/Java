package com.xyzy.security;

import com.xyzy.domain.entity.LoginUser;
import com.xyzy.domain.entity.User;
import com.xyzy.utils.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoginSessionServiceTest {

    @Test
    void exposesPermissionStringsAsAuthorities() {
        LoginUser user = new LoginUser(new User(), List.of("system:user:manage"));

        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertEquals(List.of("system:user:manage"), authorities);
    }

    @Test
    void storesAdminAndBlogSessionsUnderSeparateKeys() {
        RedisCache redis = mock(RedisCache.class);
        LoginSessionService sessions = new LoginSessionService(redis);
        LoginUser user = new LoginUser(new User().setId(7L), List.of());

        sessions.save("admin", user);
        sessions.save("blog", user);

        verify(redis).setCacheObject("adminlogin:7", user);
        verify(redis).setCacheObject("bloglogin:7", user);
    }
}
