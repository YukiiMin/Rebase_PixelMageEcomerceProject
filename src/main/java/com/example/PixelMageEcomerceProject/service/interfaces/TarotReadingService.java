package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Map;

import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.entity.Spread;

public interface TarotReadingService {
    List<Spread> getAllSpreads();

    Map<String, Object> createSession(Integer accountId, Integer spreadId, String mode);

    Map<String, Object> drawCards(Integer sessionId, boolean allowReversed);

    Map<String, Object> interpretSession(Integer sessionId);
    List<ReadingSession> getSessionsByAccount(Integer accountId);
}
