package com.school.management.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration using @EnableCaching.
 *
 * By declaring this, Spring will intercept any @Cacheable/@CacheEvict/@CachePut
 * annotations anywhere in the application.
 *
 * Cache Strategy:
 * - dashboard_stats : 5 minutes TTL (stats change infrequently)
 * - class_list : 30 minutes TTL (very stable data)
 * - subject_list : 30 minutes TTL
 * - Default : 10 minutes
 *
 * Values are serialized as JSON so they are human-readable in Redis.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // ObjectMapper with JavaTimeModule to handle LocalDate/LocalDateTime in Redis
                ObjectMapper redisObjectMapper = new ObjectMapper();
                redisObjectMapper.registerModule(new JavaTimeModule());
                redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                redisObjectMapper.activateDefaultTyping(
                                redisObjectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL);

                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                redisObjectMapper);

                // Base config: JSON serialization, 10 min default TTL
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(jsonSerializer))
                                .disableCachingNullValues();

                // Per-cache TTL overrides
                Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
                cacheConfigs.put("dashboard_stats",
                                defaultConfig.entryTtl(Duration.ofMinutes(5)));
                cacheConfigs.put("class_list",
                                defaultConfig.entryTtl(Duration.ofMinutes(30)));
                cacheConfigs.put("subject_list",
                                defaultConfig.entryTtl(Duration.ofMinutes(30)));
                cacheConfigs.put("active_year",
                                defaultConfig.entryTtl(Duration.ofHours(1)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigs)
                                .build();
        }
}
