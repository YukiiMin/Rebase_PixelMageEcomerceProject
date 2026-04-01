package com.example.PixelMageEcomerceProject.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hashids.Hashids;

import java.io.IOException;

public class HashIdSerializer extends JsonSerializer<Integer> {

    // Using the same salt as HashIdService to maintain consistency
    private static final String SALT = "PixelMage_Secret_Salt_2026";
    private static final int MIN_LENGTH = 5;
    
    private final Hashids hashids = new Hashids(SALT, MIN_LENGTH);

    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(hashids.encode(value));
        }
    }
}
