package com.blog.service;

import com.blog.config.CacheConfig;
import com.blog.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Cache service test
 */
@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheServiceImpl(cacheManager);
        when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    @Test
    void testEvictHotArticlesCache() {
        // When
        cacheService.evictHotArticlesCache();

        // Then
        verify(cacheManager).getCache(CacheConfig.HOT_ARTICLES_CACHE);
        verify(cache).clear();
    }

    @Test
    void testEvictUserInfoCache() {
        // When
        cacheService.evictUserInfoCache(1L);

        // Then
        verify(cacheManager).getCache(CacheConfig.USER_INFO_CACHE);
        verify(cache).evict("1");
    }

    @Test
    void testEvictAllUserInfoCache() {
        // When
        cacheService.evictAllUserInfoCache();

        // Then
        verify(cacheManager).getCache(CacheConfig.USER_INFO_CACHE);
        verify(cache).clear();
    }

    @Test
    void testEvictCategoriesCache() {
        // When
        cacheService.evictCategoriesCache();

        // Then
        verify(cacheManager).getCache(CacheConfig.CATEGORIES_CACHE);
        verify(cache).clear();
    }

    @Test
    void testEvictTagsCache() {
        // When
        cacheService.evictTagsCache();

        // Then
        verify(cacheManager).getCache(CacheConfig.TAGS_CACHE);
        verify(cache).clear();
    }

    @Test
    void testEvictCarouselCache() {
        // When
        cacheService.evictCarouselCache();

        // Then
        verify(cacheManager).getCache(CacheConfig.CAROUSEL_CACHE);
        verify(cache).clear();
    }

    @Test
    void testEvictArticleDetailCache() {
        // When
        cacheService.evictArticleDetailCache(1L);

        // Then
        verify(cacheManager).getCache(CacheConfig.ARTICLE_DETAIL_CACHE);
        verify(cache).evict("1");
    }

    @Test
    void testEvictAllArticleDetailCache() {
        // When
        cacheService.evictAllArticleDetailCache();

        // Then
        verify(cacheManager).getCache(CacheConfig.ARTICLE_DETAIL_CACHE);
        verify(cache).clear();
    }

    @Test
    void testEvictAllCaches() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList(
                CacheConfig.HOT_ARTICLES_CACHE,
                CacheConfig.USER_INFO_CACHE,
                CacheConfig.CAROUSEL_CACHE
        ));

        // When
        cacheService.evictAllCaches();

        // Then
        verify(cacheManager).getCacheNames();
        verify(cache, times(3)).clear();
    }

    @Test
    void testEvictUserInfoCacheWithNullUserId() {
        // When
        cacheService.evictUserInfoCache(null);

        // Then
        verify(cacheManager, never()).getCache(anyString());
        verify(cache, never()).evict(anyString());
    }

    @Test
    void testEvictArticleDetailCacheWithNullArticleId() {
        // When
        cacheService.evictArticleDetailCache(null);

        // Then
        verify(cacheManager, never()).getCache(anyString());
        verify(cache, never()).evict(anyString());
    }
}
