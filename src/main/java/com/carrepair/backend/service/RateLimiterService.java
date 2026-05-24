package com.carrepair.backend.service;

import com.carrepair.backend.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public void check(String action, String identifier, int maxAttempts, long windowMinutes) {

        String key = RATE_LIMIT_PREFIX + action + ":" + identifier;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, windowMinutes, TimeUnit.MINUTES);
        }

        if (count > maxAttempts) {
            throw new RateLimitExceededException(
                    "Too many " + action + " attempts. Please try again after " + windowMinutes + " minutes."
            );
        }
    }
}
