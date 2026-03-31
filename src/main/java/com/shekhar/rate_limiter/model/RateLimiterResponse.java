package com.shekhar.rate_limiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimiterResponse {
    private boolean allowed;
    private String message;

}
