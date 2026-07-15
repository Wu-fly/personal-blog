package com.blog.service;

/**
 * Cache service interface for managing cache operations
 */
public interface CacheService {
    
    /**
     * Evict hot articles cache
     */
    void evictHotArticlesCache();
    
    /**
     * Evict user info cache by user ID
     * @param userId User ID
     */
    void evictUserInfoCache(Long userId);
    
    /**
     * Evict all user info cache
     */
    void evictAllUserInfoCache();
    
    /**
     * Evict categories cache
     */
    void evictCategoriesCache();
    
    /**
     * Evict tags cache
     */
    void evictTagsCache();
    
    /**
     * Evict carousel cache
     */
    void evictCarouselCache();
    
    /**
     * Evict article detail cache by article ID
     * @param articleId Article ID
     */
    void evictArticleDetailCache(Long articleId);
    
    /**
     * Evict all article detail cache
     */
    void evictAllArticleDetailCache();
    
    /**
     * Evict all caches
     */
    void evictAllCaches();
}
