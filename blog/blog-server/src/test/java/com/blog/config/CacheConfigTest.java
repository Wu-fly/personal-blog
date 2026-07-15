package com.blog.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cache configuration test
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheConfigTest {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Test
    void testCacheManagerNotLoadedInTestProfile() {
        // Cache manager should not be loaded in test profile
        assertThat(cacheManager).isNull();
    }
}
