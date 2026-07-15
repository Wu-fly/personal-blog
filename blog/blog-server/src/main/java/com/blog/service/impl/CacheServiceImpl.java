package com.blog.service.impl;

import com.blog.config.CacheConfig;
import com.blog.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Cache service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {
    
    private final CacheManager cacheManager;
    
    @Override
    public void evictHotArticlesCache() {
        evictCache(CacheConfig.HOT_ARTICLES_CACHE);
        log.info("Evicted hot articles cache");
    }
    
    @Override
    public void evictUserInfoCache(Long userId) {
        if (userId != null) {
            evictCache(CacheConfig.USER_INFO_CACHE, userId.toString());
            log.info("Evicted user info cache for user: {}", userId);
        }
    }
    
    @Override
    public void evictAllUserInfoCache() {
        evictCache(CacheConfig.USER_INFO_CACHE);
        log.info("Evicted all user info cache");
    }
    
    @Override
    public void evictCategoriesCache() {
        evictCache(CacheConfig.CATEGORIES_CACHE);
        log.info("Evicted categories cache");
    }
    
    @Override
    public void evictTagsCache() {
        evictCache(CacheConfig.TAGS_CACHE);
        log.info("Evicted tags cache");
    }
    
    @Override
    public void evictCarouselCache() {
        evictCache(CacheConfig.CAROUSEL_CACHE);
        log.info("Evicted carousel cache");
    }
    
    @Override
    public void evictArticleDetailCache(Long articleId) {
        if (articleId != null) {
            evictCache(CacheConfig.ARTICLE_DETAIL_CACHE, articleId.toString());
            log.info("Evicted article detail cache for article: {}", articleId);
        }
    }
    
    @Override
    public void evictAllArticleDetailCache() {
        evictCache(CacheConfig.ARTICLE_DETAIL_CACHE);
        log.info("Evicted all article detail cache");
    }
    
    @Override
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            evictCache(cacheName);
        });
        log.info("Evicted all caches");
    }
    
    /**
     * Evict cache by name
     */
    private void evictCache(String cacheName) {
        try {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        } catch (Exception e) {
            log.error("Failed to evict cache: {}", cacheName, e);
        }
    }
    
    /**
     * Evict cache by name and key
     */
    private void evictCache(String cacheName, String key) {
        try {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).evict(key);
        } catch (Exception e) {
            log.error("Failed to evict cache: {} with key: {}", cacheName, key, e);
        }
    }
}
