package com.example.PixelMageEcomerceProject.config;

import com.example.PixelMageEcomerceProject.service.HashIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HashIdConverter implements Converter<String, Integer> {

    private final HashIdService hashIdService;

    @Override
    public Integer convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        // Try parsing as normal Integer first (e.g. if Swagger passes normal integers)
        try {
            return Integer.parseInt(source);
        } catch (NumberFormatException e) {
            // Not a normal integer, attempt to decode as Hashid
            return hashIdService.decode(source)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid ID format"));
        }
    }
}
