package com.example.PixelMageEcomerceProject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/redis-test")
@Tag(name = "Redis Test", description = "API for testing Redis connection on Railway")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/ping")
    public ResponseEntity<String> pingRedis() {
        try {
            // Write to Redis
            redisTemplate.opsForValue().set("ping", "pong");
            // Read from Redis
            String value = redisTemplate.opsForValue().get("ping");

            if ("pong".equals(value)) {
                return ResponseEntity.ok("✅ Successfully connected to Redis! ping -> " + value);
            } else {
                return ResponseEntity.internalServerError()
                        .body("❌ Connected to Redis, but returned wrong value: " + value);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Failed to connect to Redis. Error: " + e.getMessage());
        }
    }
}
