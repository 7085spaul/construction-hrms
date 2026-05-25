package com.payrollapplication.payroll.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class RedisCacheErrorHandler implements CacheErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logger.warn("Cache get error for cache [{}] key [{}]: {}. Falling back to database.", 
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        logger.warn("Cache put error for cache [{}] key [{}]: {}. Skipping cache update.", 
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logger.warn("Cache evict error for cache [{}] key [{}]: {}. Skipping cache eviction.", 
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logger.warn("Cache clear error for cache [{}]: {}. Skipping cache clear.", 
                cache.getName(), exception.getMessage());
    }
}
