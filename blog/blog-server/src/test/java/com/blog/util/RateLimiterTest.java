package com.blog.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimiter
 */
@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        rateLimiter = new RateLimiter(redisTemplate);
    }

    @Test
    void testIsAllowed_FirstRequest_ShouldAllow() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);

        // Act
        boolean allowed = rateLimiter.isAllowed(key, 5, 1, TimeUnit.MINUTES);

        // Assert
        assertTrue(allowed);
        verify(zSetOperations).removeRangeByScore(eq("rate_limit:" + key), anyDouble(), anyDouble());
        verify(zSetOperations).count(eq("rate_limit:" + key), anyDouble(), anyDouble());
        verify(zSetOperations).add(eq("rate_limit:" + key), anyString(), anyDouble());
        verify(redisTemplate).expire(eq("rate_limit:" + key), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void testIsAllowed_WithinLimit_ShouldAllow() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(3L);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);

        // Act
        boolean allowed = rateLimiter.isAllowed(key, 5, 1, TimeUnit.MINUTES);

        // Assert
        assertTrue(allowed);
    }

    @Test
    void testIsAllowed_ExceedLimit_ShouldDeny() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(5L);

        // Act
        boolean allowed = rateLimiter.isAllowed(key, 5, 1, TimeUnit.MINUTES);

        // Assert
        assertFalse(allowed);
        verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
    }

    @Test
    void testIsAllowed_AtLimit_ShouldDeny() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(5L);

        // Act
        boolean allowed = rateLimiter.isAllowed(key, 5, 1, TimeUnit.MINUTES);

        // Assert
        assertFalse(allowed);
    }

    @Test
    void testGetRemainingRequests_NoRequests_ShouldReturnMax() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(0L);

        // Act
        long remaining = rateLimiter.getRemainingRequests(key, 5, 1, TimeUnit.MINUTES);

        // Assert
        assertEquals(5, remaining);
    }

    @Test
    void testGetRemainingRequests_SomeRequests_ShouldReturnCorrectCount() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(3L);

        // Act
        long remaining = rateLimiter.getRemainingRequests(key, 5, 1, TimeUnit.MINUTES);

        // Assert
        assertEquals(2, remaining);
    }

    @Test
    void testGetRemainingRequests_AtLimit_ShouldReturnZero() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(5L);

        // Act
        long remaining = rateLimiter.getRemainingRequests(key, 5, 1, TimeUnit.MINUTES);

        // Assert
        assertEquals(0, remaining);
    }

    @Test
    void testReset_ShouldDeleteKey() {
        // Arrange
        String key = "test:user1";

        // Act
        rateLimiter.reset(key);

        // Assert
        verify(redisTemplate).delete("rate_limit:" + key);
    }

    @Test
    void testIsAllowed_DifferentTimeUnits_ShouldWork() {
        // Arrange
        String key = "test:user1";
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);

        // Act & Assert - Seconds
        assertTrue(rateLimiter.isAllowed(key, 100, 1, TimeUnit.SECONDS));
        verify(redisTemplate).expire(eq("rate_limit:" + key), eq(1L), eq(TimeUnit.SECONDS));

        // Act & Assert - Hours
        reset(redisTemplate, zSetOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        
        assertTrue(rateLimiter.isAllowed(key, 3, 1, TimeUnit.HOURS));
        verify(redisTemplate).expire(eq("rate_limit:" + key), eq(1L), eq(TimeUnit.HOURS));
    }
}
