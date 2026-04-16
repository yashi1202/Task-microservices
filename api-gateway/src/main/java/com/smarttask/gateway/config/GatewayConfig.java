package com.smarttask.gateway.config;

import com.smarttask.gateway.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive
        .CorsWebFilter;
import org.springframework.web.cors.reactive
        .UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class GatewayConfig {

    // Register JwtAuthFilter as a named filter
    // so application.yml can reference it by name
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    // CORS configuration — allows frontend to call gateway
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT",
                        "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
    
    @Bean
    public RedisTemplate<String, Object>
            redisTemplate(
                    RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(
                new StringRedisSerializer());
        template.setValueSerializer(
                new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}