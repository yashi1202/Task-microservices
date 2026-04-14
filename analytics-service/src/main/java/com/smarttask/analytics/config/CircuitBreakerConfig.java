package com.smarttask.analytics.config;

import io.github.resilience4j.circuitbreaker
        .CircuitBreaker;
import io.github.resilience4j.circuitbreaker
        .CircuitBreakerRegistry;
import io.github.resilience4j.core.registry
        .EntryAddedEvent;
import io.github.resilience4j.core.registry
        .EntryRemovedEvent;
import io.github.resilience4j.core.registry
        .EntryReplacedEvent;
import io.github.resilience4j.core.registry
        .RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation
        .Bean;
import org.springframework.context.annotation
        .Configuration;

@Slf4j
@Configuration
public class CircuitBreakerConfig {

    // Log all circuit breaker state changes
    @Bean
    public RegistryEventConsumer<CircuitBreaker>
            circuitBreakerEventConsumer() {
        return new RegistryEventConsumer<>() {

            @Override
            public void onEntryAddedEvent(
                    EntryAddedEvent<CircuitBreaker>
                            event) {
                event.getAddedEntry()
                        .getEventPublisher()
                        .onStateTransition(e ->
                    log.warn(
                        "Circuit Breaker [{}] "
                        + "state changed: {} → {}",
                        e.getCircuitBreakerName(),
                        e.getStateTransition()
                            .getFromState(),
                        e.getStateTransition()
                            .getToState()))
                        .onFailureRateExceeded(e ->
                    log.error(
                        "Circuit Breaker [{}] "
                        + "failure rate exceeded: {}%",
                        e.getCircuitBreakerName(),
                        e.getFailureRate()))
                        .onCallNotPermitted(e ->
                    log.warn(
                        "Circuit Breaker [{}] "
                        + "blocked request — "
                        + "circuit is OPEN",
                        e.getCircuitBreakerName()))
                        .onSuccess(e ->
                    log.debug(
                        "Circuit Breaker [{}] "
                        + "call succeeded in {}ms",
                        e.getCircuitBreakerName(),
                        e.getElapsedDuration()
                            .toMillis()))
                        .onError(e ->
                    log.error(
                        "Circuit Breaker [{}] "
                        + "call failed: {}",
                        e.getCircuitBreakerName(),
                        e.getThrowable()
                            .getMessage()));
            }

            @Override
            public void onEntryRemovedEvent(
                    EntryRemovedEvent<CircuitBreaker>
                            event) {}

            @Override
            public void onEntryReplacedEvent(
                    EntryReplacedEvent<CircuitBreaker>
                            event) {}
        };
    }
}