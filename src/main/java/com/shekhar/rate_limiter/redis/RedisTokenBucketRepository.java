package com.shekhar.rate_limiter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisTokenBucketRepository {

    private final StringRedisTemplate redisTemplate;

    public synchronized boolean consumeToken(String apiKey, int capacity, int refillRatePerSec){
        String tokenKey = "bucket:" + apiKey + ":tokens";
        String timestampKey = "bucket:" + apiKey + ":ts";

        long now = System.currentTimeMillis() / 1000;

        String tokensStr = redisTemplate.opsForValue().get(tokenKey);
        String lastRefillStr = redisTemplate.opsForValue().get(timestampKey);

        Integer tokens = tokensStr != null ? Integer.parseInt(tokensStr) : null;
        Long lastRefill = lastRefillStr != null ? Long.parseLong(lastRefillStr) : null;

        if(tokens == null || lastRefill == null){
            redisTemplate.opsForValue().set(tokenKey, String.valueOf(capacity - 1));
            redisTemplate.opsForValue().set(timestampKey, String.valueOf(now));
            return true;
        }

        long elapsed = now - lastRefill;
        int newToken = Math.min(capacity, tokens + (int) elapsed * refillRatePerSec);

        redisTemplate.opsForValue().set(tokenKey, String.valueOf(newToken));
        redisTemplate.opsForValue().set(timestampKey, String.valueOf(now));

        if (newToken > 0){
            redisTemplate.opsForValue().decrement(tokenKey);
            return true;
        }
        return false;
    }

}
