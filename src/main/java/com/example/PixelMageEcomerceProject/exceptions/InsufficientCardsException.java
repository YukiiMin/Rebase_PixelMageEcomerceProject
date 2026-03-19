package com.example.PixelMageEcomerceProject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientCardsException extends RuntimeException {
    public InsufficientCardsException(String message) {
        super(message);
    }
}
