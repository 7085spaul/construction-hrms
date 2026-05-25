package com.payrollapplication.payroll.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActiveWorkersCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ActiveWorkersCacheService.class);
    private static final String ACTIVE_WORKERS_KEY = "active:workers";
    private static final Duration TTL = Duration.ofHours(16);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addActiveWorker(Long workerId, Long siteId, String siteName, String workerName, String clockInTime) {
        try {
            String key = ACTIVE_WORKERS_KEY + ":" + workerId;
            
            Map<String, Object> workerData = Map.of(
                "workerId", workerId,
                "siteId", siteId,
                "siteName", siteName,
                "workerName", workerName,
                "clockInTime", clockInTime
            );
            
            redisTemplate.opsForHash().putAll(key, workerData);
            redisTemplate.expire(key, TTL);
        } catch (Exception e) {
            logger.warn("Failed to add worker {} to Redis cache: {}. Continuing without cache.", workerId, e.getMessage());
        }
    }

    public void removeActiveWorker(Long workerId) {
        try {
            String key = ACTIVE_WORKERS_KEY + ":" + workerId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("Failed to remove worker {} from Redis cache: {}. Continuing without cache.", workerId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getActiveWorker(Long workerId) {
        try {
            String key = ACTIVE_WORKERS_KEY + ":" + workerId;
            return (Map<String, Object>) (Map<?, ?>) redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            logger.warn("Failed to get worker {} from Redis cache: {}. Returning empty data.", workerId, e.getMessage());
            return Map.of();
        }
    }

    public Set<Long> getAllActiveWorkerIds() {
        try {
            Set<String> keys = redisTemplate.keys(ACTIVE_WORKERS_KEY + ":*");
            if (keys == null) {
                return Set.of();
            }
            return keys.stream()
                    .map(key -> key.substring(ACTIVE_WORKERS_KEY.length() + 1))
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.warn("Failed to get active workers from Redis cache: {}. Returning empty set.", e.getMessage());
            return Set.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getActiveWorkerData(Long workerId) {
        try {
            String key = ACTIVE_WORKERS_KEY + ":" + workerId;
            return (Map<String, Object>) (Map<?, ?>) redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            logger.warn("Failed to get worker data {} from Redis cache: {}. Returning empty data.", workerId, e.getMessage());
            return Map.of();
        }
    }

    public void invalidateWorkerCache(Long workerId) {
        removeActiveWorker(workerId);
    }
}
