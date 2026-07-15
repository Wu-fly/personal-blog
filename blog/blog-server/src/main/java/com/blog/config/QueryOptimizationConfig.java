package com.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Query Optimization Configuration
 * 
 * Configures various query optimization settings for the application.
 */
@Configuration
public class QueryOptimizationConfig {
    
    /**
     * Query optimization properties
     */
    @Bean
    @ConfigurationProperties(prefix = "app.query-optimization")
    public QueryOptimizationProperties queryOptimizationProperties() {
        return new QueryOptimizationProperties();
    }
    
    /**
     * Query Optimization Properties
     */
    public static class QueryOptimizationProperties {
        
        /**
         * Maximum page number for offset pagination
         * Default: 100
         */
        private int maxPageNumber = 100;
        
        /**
         * Default page size
         * Default: 20
         */
        private int defaultPageSize = 20;
        
        /**
         * Maximum page size
         * Default: 100
         */
        private int maxPageSize = 100;
        
        /**
         * Enable cursor-based pagination
         * Default: true
         */
        private boolean enableCursorPagination = true;
        
        /**
         * Enable query result caching
         * Default: true
         */
        private boolean enableQueryCache = true;
        
        /**
         * Query cache TTL in seconds
         * Default: 300 (5 minutes)
         */
        private int queryCacheTtl = 300;
        
        /**
         * Enable slow query logging
         * Default: true
         */
        private boolean enableSlowQueryLog = true;
        
        /**
         * Slow query threshold in milliseconds
         * Default: 1000 (1 second)
         */
        private long slowQueryThreshold = 1000;
        
        // Getters and Setters
        
        public int getMaxPageNumber() {
            return maxPageNumber;
        }
        
        public void setMaxPageNumber(int maxPageNumber) {
            this.maxPageNumber = maxPageNumber;
        }
        
        public int getDefaultPageSize() {
            return defaultPageSize;
        }
        
        public void setDefaultPageSize(int defaultPageSize) {
            this.defaultPageSize = defaultPageSize;
        }
        
        public int getMaxPageSize() {
            return maxPageSize;
        }
        
        public void setMaxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }
        
        public boolean isEnableCursorPagination() {
            return enableCursorPagination;
        }
        
        public void setEnableCursorPagination(boolean enableCursorPagination) {
            this.enableCursorPagination = enableCursorPagination;
        }
        
        public boolean isEnableQueryCache() {
            return enableQueryCache;
        }
        
        public void setEnableQueryCache(boolean enableQueryCache) {
            this.enableQueryCache = enableQueryCache;
        }
        
        public int getQueryCacheTtl() {
            return queryCacheTtl;
        }
        
        public void setQueryCacheTtl(int queryCacheTtl) {
            this.queryCacheTtl = queryCacheTtl;
        }
        
        public boolean isEnableSlowQueryLog() {
            return enableSlowQueryLog;
        }
        
        public void setEnableSlowQueryLog(boolean enableSlowQueryLog) {
            this.enableSlowQueryLog = enableSlowQueryLog;
        }
        
        public long getSlowQueryThreshold() {
            return slowQueryThreshold;
        }
        
        public void setSlowQueryThreshold(long slowQueryThreshold) {
            this.slowQueryThreshold = slowQueryThreshold;
        }
    }
}
