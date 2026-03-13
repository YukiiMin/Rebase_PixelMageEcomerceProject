package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.service.interfaces.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockServiceImpl implements RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean tryLock(String lockKey, long ttlSeconds) {
        // SETNX (Set If Not Exists) với TTL — atomic operation
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", Duration.ofSeconds(ttlSeconds));
        boolean result = Boolean.TRUE.equals(acquired);
        if (result) {
            log.debug("[LOCK] Acquired lock: {}", lockKey);
        } else {
            log.debug("[LOCK] Failed to acquire lock (already locked): {}", lockKey);
        }
        return result;
    }

    @Override
    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
        log.debug("[LOCK] Released lock: {}", lockKey);
    }
}
