package com.xyzy.security;

import com.xyzy.domain.entity.LoginUser;
import com.xyzy.utils.RedisCache;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LoginSessionService {
    private static final Set<String> SCOPES = Set.of("blog", "admin");

    private final RedisCache redisCache;

    public LoginSessionService(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    public void save(String scope, LoginUser loginUser) {
        redisCache.setCacheObject(key(scope, loginUser.getUser().getId()), loginUser);
    }

    public LoginUser get(String scope, Long userId) {
        return redisCache.getCacheObject(key(scope, userId));
    }

    public void delete(String scope, Long userId) {
        redisCache.deleteObject(key(scope, userId));
    }

    private String key(String scope, Long userId) {
        if (!SCOPES.contains(scope)) {
            throw new IllegalArgumentException("Unknown login scope: " + scope);
        }
        return scope + "login:" + userId;
    }
}
