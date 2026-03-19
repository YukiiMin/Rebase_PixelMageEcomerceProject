package com.example.PixelMageEcomerceProject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class SessionExpiredException extends RuntimeException {

    public SessionExpiredException(Integer sessionId) {
        super("Phiên EXPLORE #" + sessionId + " đã hết hạn (30 phút). Vui lòng tạo phiên mới.");
    }
}
