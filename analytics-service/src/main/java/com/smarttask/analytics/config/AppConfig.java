package com.smarttask.analytics.config;

import org.springframework.beans.factory.annotation
        .Value;
import org.springframework.cache.annotation
        .EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
        .Configuration;
import org.springframework.data.redis.cache
        .RedisCacheConfiguration;
import org.springframework.data.redis.cache
        .RedisCacheManager;
import org.springframework.data.redis.connection
        .RedisConnectionFactory;
import org.springframework.data.redis.serializer
        .GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer
        .RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function
        .client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
@EnableCaching
public class AppConfig {
	
	public static final String CACHE_DASHBOARD =
            "dashboard";


    @Value("${task-service.url:http://localhost:8082}")
    private String taskServiceUrl;

    // ─── WebClient ────────────────────────────────

    @Bean
    public WebClient taskServiceClient() {
        return WebClient.builder()
                .baseUrl(taskServiceUrl)
                .build();
    }

    // ─── ObjectMapper ─────────────────────────────
    // Jackson 3.x — no JavaTimeModule needed
    // date/time support is built in

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // ─── Redis Cache Manager ──────────────────────

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory) {

        RedisCacheConfiguration config =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(
                                Duration.ofMinutes(5))
                        .serializeKeysWith(
                            RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(
                                    new StringRedisSerializer()))
                        .serializeValuesWith(
                            RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(
                                    new StringRedisSerializer()))
                        .disableCachingNullValues();

        return RedisCacheManager
                .builder(factory)
                .cacheDefaults(config)
                .build();
}
}