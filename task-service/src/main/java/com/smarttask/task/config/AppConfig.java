package com.smarttask.task.config;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AppConfig {

    // Resolves current user from gateway header
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes)
                        RequestContextHolder
                                .getRequestAttributes();
                if (attrs != null) {
                    String username = attrs.getRequest()
                            .getHeader("X-Auth-Username");
                    if (username != null
                            && !username.isBlank()) {
                        return Optional.of(username);
                    }
                }
            } catch (Exception ignored) {}
            return Optional.of("system");
        };
    }

    // Create ObjectMapper directly — no builder dependency
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //mapper.setVisibility(
               // PropertyAccessor.FIELD,
                //JsonAutoDetect.Visibility.ANY);
        return mapper;
    }
}