package com.blog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Rate limit annotation for API endpoints
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum number of requests allowed
     */
    int maxCount() default 100;

    /**
     * Time window duration
     */
    long duration() default 1;

    /**
     * Time unit for the duration
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * Rate limit key type
     */
    KeyType keyType() default KeyType.IP;

    /**
     * Custom key prefix (optional)
     */
    String keyPrefix() default "";

    enum KeyType {
        IP,           // Rate limit by IP address
        USER,         // Rate limit by user ID
        PHONE,        // Rate limit by phone number
        CUSTOM        // Custom key from request parameter
    }
}
