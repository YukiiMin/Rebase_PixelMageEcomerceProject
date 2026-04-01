package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarotReadingResponse {
    private Integer sessionId;
    private String status;
    private String mode;
    private Object spread; // Simplified
    private List<Object> positions; // Simplified
    private LocalDateTime createdAt;
}
