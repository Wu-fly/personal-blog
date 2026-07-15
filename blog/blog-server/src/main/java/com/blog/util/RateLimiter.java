package com.blog.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiter using sliding window algorithm
 */
@Component
public class RateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    public RateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if the request is allowed based on rate limit
     *
     * @param key       Unique identifier for the rate limit (e.g., "login:192.168.1.1" or "sms:13800138000")
     * @param maxCount  Maximum number of requests allowed
     * @param duration  Time window duration
     * @param unit      Time unit for the duration
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int maxCount, long duration, TimeUnit unit) {
        String redisKey = "rate_limit:" + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - unit.toMillis(duration);

        // Remove old entries outside the time window
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

        // Count current requests in the window
        Long count = redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);

        if (count != null && count >= maxCount) {
            return false;
        }

        // Add current request
        redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);

        // Set expiration for the key
        redisTemplate.expire(redisKey, duration, unit);

        return true;
    }

    /**
     * Get remaining requests for a key
     *
     * @param key       Unique identifier for the rate limit
     * @param maxCount  Maximum number of requests allowed
     * @param duration  Time window duration
     * @param unit      Time unit for the duration
     * @return number of remaining requests
     */
    public long getRemainingRequests(String key, int maxCount, long duration, TimeUnit unit) {
        String redisKey = "rate_limit:" + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - unit.toMillis(duration);

        Long count = redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);
        return maxCount - (count != null ? count : 0);
    }

    /**
     * Reset rate limit for a key
     *
     * @param key Unique identifier for the rate limit
     */
    public void reset(String key) {
        String redisKey = "rate_limit:" + key;
        redisTemplate.delete(redisKey);
    }
}
