package com.ss.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@Slf4j
public class CorsConfig {

    @Value("${management.endpoints.web.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Value("${management.endpoints.web.cors.allowed-methods:}")
    private List<String> allowedMethods;

    @Value("${management.endpoints.web.cors.allowed-headers:}")
    private List<String> allowedHeaders;

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("cors allowedOrigins {}, allowedMethods {}, allowedHeaders {}", allowedOrigins, allowedMethods, allowedHeaders);
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(Duration.of(86400, ChronoUnit.SECONDS));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
