package com.example.PixelMageEcomerceProject.service;

import org.hashids.Hashids;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HashIdService {

    // Same salt across the entire application to ensure stable encoding/decoding.
    private static final String SALT = "PixelMage_Secret_Salt_2026";
    // Ensure ids are at least 5 characters long to prevent them looking too simple.
    private static final int MIN_LENGTH = 5;
    
    private final Hashids hashids;

    public HashIdService() {
        this.hashids = new Hashids(SALT, MIN_LENGTH);
    }

    /**
     * Encode an internal integer ID to a public Hashid string.
     */
    public String encode(Integer id) {
        if (id == null) return null;
        return hashids.encode(id);
    }

    /**
     * Decode a public Hashid string back to an internal integer ID.
     */
    public Optional<Integer> decode(String hashId) {
        if (hashId == null || hashId.isEmpty()) {
            return Optional.empty();
        }
        long[] decoded = hashids.decode(hashId);
        if (decoded.length == 0) {
            return Optional.empty();
        }
        return Optional.of((int) decoded[0]);
    }
}
