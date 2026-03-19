package com.example.PixelMageEcomerceProject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RedisUnavailableException extends RuntimeException {

    public RedisUnavailableException(String message) {
        super(message);
    }
}
