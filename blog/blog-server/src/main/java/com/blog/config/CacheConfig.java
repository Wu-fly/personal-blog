package com.blog.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration with different TTL for different cache types
 * Only active in non-test profiles
 */
@Configuration
@EnableCaching
@Profile("!test")
public class CacheConfig {
    
    // Cache names
    public static final String HOT_ARTICLES_CACHE = "hotArticles";
    public static final String USER_INFO_CACHE = "userInfo";
    public static final String CATEGORIES_CACHE = "categories";
    public static final String TAGS_CACHE = "tags";
    public static final String CAROUSEL_CACHE = "carousel";
    public static final String ARTICLE_DETAIL_CACHE = "articleDetail";
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Create ObjectMapper with JavaTimeModule for LocalDateTime support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        
        // Create serializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer))
                .disableCachingNullValues();
        
        // Custom cache configurations with different TTL
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Hot articles cache - TTL: 1 hour
        cacheConfigurations.put(HOT_ARTICLES_CACHE, 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // User info cache - TTL: 30 minutes
        cacheConfigurations.put(USER_INFO_CACHE, 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Categories cache - TTL: 24 hours
        cacheConfigurations.put(CATEGORIES_CACHE, 
                defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Tags cache - TTL: 24 hours
        cacheConfigurations.put(TAGS_CACHE, 
                defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Carousel cache - TTL: 1 hour
        cacheConfigurations.put(CAROUSEL_CACHE, 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Article detail cache - TTL: 1 hour
        cacheConfigurations.put(ARTICLE_DETAIL_CACHE, 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
