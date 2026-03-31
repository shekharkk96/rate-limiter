package com.shekhar.rate_limiter.service;

import com.shekhar.rate_limiter.model.RateLimiterResponse;
import com.shekhar.rate_limiter.redis.RedisTokenBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RedisTokenBucketRepository redisTokenBucketRepository;

    private final int BUCKET_CAPACITY = 10;
    private final int REFILL_RATE = 1;

    public RateLimiterResponse allowRequest(String apiKey){
        boolean allow = redisTokenBucketRepository.consumeToken(apiKey, BUCKET_CAPACITY, REFILL_RATE);
        if (allow){
            return new RateLimiterResponse(true, "Request allowed");
        }else{
            return new RateLimiterResponse(false, "Rate limit exceeded");
        }
    }
}
