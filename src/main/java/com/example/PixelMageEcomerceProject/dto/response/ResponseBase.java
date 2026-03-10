package com.example.PixelMageEcomerceProject.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBase<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ResponseEntity<ResponseBase<T>> ok(T data, String message) {
        return ResponseEntity.ok(new ResponseBase<>(HttpStatus.OK.value(), message, data));
    }

    public static <T> ResponseEntity<ResponseBase<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseBase<>(HttpStatus.CREATED.value(), message, data));
    }

    public static <T> ResponseEntity<ResponseBase<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ResponseBase<>(status.value(), message, null));
    }

    public static <T> ResponseEntity<ResponseBase<T>> success(String message) {
        return ResponseEntity.ok(new ResponseBase<>(HttpStatus.OK.value(), message, null));
    }
}
