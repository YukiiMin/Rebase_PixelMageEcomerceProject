package com.example.PixelMageEcomerceProject.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer  jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // TTL per cache name
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("card-templates",           defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("card-template-by-id",       defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigs.put("spreads",                   defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("public-collections",        defaultConfig.entryTtl(Duration.ofHours(1)));
        // CardContent caches — 6h TTL, evicted on CUD operations
        cacheConfigs.put("card-contents-active",      defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigs.put("card-contents-all",         defaultConfig.entryTtl(Duration.ofHours(6)));
        // Product catalog — ít thay đổi, 1h TTL
        cacheConfigs.put("products",                  defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("product-by-id",             defaultConfig.entryTtl(Duration.ofHours(1)));
        // Pack inventory — thay đổi khi staff tạo/bán, 30 phút TTL
        cacheConfigs.put("packs",                     defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("packs-by-status",           defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("packs-by-product-status",   defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // Achievement definitions — ít thay đổi, 6h TTL
        cacheConfigs.put("achievement-definitions",   defaultConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofHours(1)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
