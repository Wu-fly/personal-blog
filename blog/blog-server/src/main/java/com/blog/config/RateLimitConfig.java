package com.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Rate limit configuration
 * Enables AOP for rate limiting aspect
 */
@Configuration
@EnableAspectJAutoProxy
public class RateLimitConfig {
    // Configuration for rate limiting
    // The actual rate limiting is handled by RateLimitAspect
}
