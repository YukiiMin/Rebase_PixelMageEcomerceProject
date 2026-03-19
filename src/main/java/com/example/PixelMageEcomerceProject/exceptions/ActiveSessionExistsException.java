package com.example.PixelMageEcomerceProject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ActiveSessionExistsException extends RuntimeException {

    private final Integer activeSessionId;

    public ActiveSessionExistsException(Integer activeSessionId) {
        super("Bạn đang có một phiên đọc bài chưa hoàn thành.");
        this.activeSessionId = activeSessionId;
    }

    public Integer getActiveSessionId() {
        return activeSessionId;
    }
}
