package com.example.PixelMageEcomerceProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        // Disable failure on empty beans (like Hibernate proxies)
        builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // Add JavaTimeModule to properly serialize Java 8 date/time types (LocalDate,
        // LocalDateTime)
        builder.modules(new JavaTimeModule());
        return builder;
    }
}
