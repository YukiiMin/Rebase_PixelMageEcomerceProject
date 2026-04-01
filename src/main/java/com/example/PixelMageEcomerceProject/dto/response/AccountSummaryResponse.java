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
public class AccountSummaryResponse {
    private Integer customerId;
    private String email;
    private String name;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
