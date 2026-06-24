package com.xyzy.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisCacheTest {

    @Test
    void incrementCacheMapValueHandlesSerializedLongValues() {
        RedisTemplate redisTemplate = mock(RedisTemplate.class);
        HashOperations hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("article:viewCount", "1")).thenReturn(120L);

        RedisCache redisCache = new RedisCache();
        redisCache.redisTemplate = redisTemplate;

        redisCache.incrementCacheMapValue("article:viewCount", "1", 1);

        verify(hashOperations).put("article:viewCount", "1", 121L);
    }
}
