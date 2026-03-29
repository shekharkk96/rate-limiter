package com.shekhar.rate_limiter.controller;

import com.shekhar.rate_limiter.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    @GetMapping("api/data")
    public ResponseEntity<?> getData(@RequestHeader("X-API-KEY") String spiKey) {

    }
}
