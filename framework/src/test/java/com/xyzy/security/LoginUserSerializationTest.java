package com.xyzy.security;

import com.xyzy.domain.entity.LoginUser;
import com.xyzy.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoginUserSerializationTest {

    @Test
    void serializesLoginSessionWithDefaultRedisSerializer() {
        LoginUser loginUser = new LoginUser(
                new User().setId(1L).setUserName("admin").setStatus("0").setDelFlag(0),
                List.of("system:user:manage"));

        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();

        assertDoesNotThrow(() -> serializer.serialize(loginUser));
    }
}
