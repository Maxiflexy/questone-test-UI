package com.fundquest.auth.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Feign clients
 * Enables Feign clients and configures timeouts and retry policies
 */
@Configuration
@EnableFeignClients(basePackages = "com.fundquest.auth.client")
public class FeignConfiguration {

    /**
     * Configure Feign request options with timeouts
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                10, TimeUnit.SECONDS,    // Connect timeout: 10 seconds
                30, TimeUnit.SECONDS,    // Read timeout: 30 seconds
                true                     // Follow redirects
        );
    }

    /**
     * Configure retry policy for failed requests
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                100L,    // Initial retry interval: 100ms
                1000L,   // Maximum retry interval: 1 second
                3        // Maximum attempts: 3
        );
    }

    /**
     * Configure Feign logging level
     * BASIC logs only HTTP method, URL, response status, and execution time
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}