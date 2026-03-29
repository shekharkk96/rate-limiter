package com.shekhar.rate_limiter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisTokenBucketRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTokenBucketRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public synchronized boolean consumeToken(String apiKey, int capacity, int refillRatePerSec){
        String tokenKey = "bucket:" + apiKey + ":tokens";
        String timestampKey = "bucket:" + apiKey + ":ts";

        long now = System.currentTimeMillis() / 1000;

        Integer tokens = (Integer) redisTemplate.opsForValue().get(tokenKey);
        Long lastRefill = (Long) redisTemplate.opsForValue().get(timestampKey);

        if(tokens == null || lastRefill == null){
            redisTemplate.opsForValue().set(tokenKey, capacity - 1);
            redisTemplate.opsForValue().set(timestampKey, now);
            return true;
        }

        long elapsed = now - lastRefill;
        int newToken = Math.min(capacity, tokens + (int) elapsed * refillRatePerSec);

        redisTemplate.opsForValue().set(tokenKey, newToken);
        redisTemplate.opsForValue().set(timestampKey, now);

        if (newToken > 0){
            redisTemplate.opsForValue().decrement(tokenKey);
            return true;
        }
        return false;
    }

}
