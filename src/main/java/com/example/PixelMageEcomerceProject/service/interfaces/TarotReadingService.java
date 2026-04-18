package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Map;

import com.example.PixelMageEcomerceProject.dto.response.SpreadResponse;
import com.example.PixelMageEcomerceProject.entity.ReadingSession;

public interface TarotReadingService {
    List<SpreadResponse> getAllSpreads();

    Map<String, Object> createSession(Integer accountId, Integer spreadId, String mode, String mainQuestion);

    Map<String, Object> drawCards(Integer sessionId, boolean allowReversed);

    Map<String, Object> interpretSession(Integer sessionId);
    List<ReadingSession> getSessionsByAccount(Integer accountId);
    ReadingSession getSessionById(Integer sessionId);

    /**
     * Hủy session đang PENDING/INTERPRETING (người dùng bỏ giữa chừng).
     * Chỉ cho phép hủy session của chính account đó.
     */
    void cancelSession(Integer sessionId, Integer accountId);
}
