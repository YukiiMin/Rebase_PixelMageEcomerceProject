package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Integer customerId;
    private String email;
    private String name;
    private String avatarUrl;
    private String phoneNumber;
    private String role;
    private String authProvider;
    private Boolean emailVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Boolean guestReadingUsedToday;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer customerId;
        private String email;
        private String name;
        private String role;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }
}
