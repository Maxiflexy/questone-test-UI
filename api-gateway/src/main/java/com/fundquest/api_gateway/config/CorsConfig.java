package com.fundquest.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Parse allowed origins
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        corsConfig.setAllowedOriginPatterns(origins);

        // Parse allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        corsConfig.setAllowedMethods(methods);

        // Parse allowed headers
        if ("*".equals(allowedHeaders.trim())) {
            corsConfig.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            corsConfig.setAllowedHeaders(headers);
        }

        // Set credentials and max age
        corsConfig.setAllowCredentials(allowCredentials);
        corsConfig.setMaxAge(maxAge);

        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}