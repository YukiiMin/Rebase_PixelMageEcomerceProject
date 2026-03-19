package com.example.PixelMageEcomerceProject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CardLockedInSessionException extends RuntimeException {
    public CardLockedInSessionException(String message) {
        super(message);
    }
}
