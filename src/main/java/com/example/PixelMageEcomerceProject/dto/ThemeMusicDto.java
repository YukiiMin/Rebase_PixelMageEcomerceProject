package com.example.PixelMageEcomerceProject.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

public class ThemeMusicDto {

    @Data
    @Builder
    public static class Response {
        private Long musicId;
        private String title;
        private String artist;
        private String url;
        private Boolean active;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ActivateRequest {
        private Long musicId;
    }
}
