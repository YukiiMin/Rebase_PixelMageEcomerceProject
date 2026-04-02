package com.example.PixelMageEcomerceProject.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.dto.response.SpreadResponse;
import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.service.interfaces.TarotReadingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/readings")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TarotReadingController {

    private final TarotReadingService tarotReadingService;

    /**
     * Lấy danh sách các kiểu trải bài (Spread)
     */
    @GetMapping("/spreads")
    public ResponseEntity<ResponseBase<List<SpreadResponse>>> getSpreads() {
        try {
            List<SpreadResponse> spreads = tarotReadingService.getAllSpreads();
            return ResponseBase.ok(spreads, "Success");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Bắt đầu một phiên bốc bài mới
     */
    @PostMapping("/sessions")
    public ResponseEntity<ResponseBase<Map<String, Object>>> createSession(@RequestBody Map<String, Object> payload) {
        try {
            // Hardcode accountId=1 for testing purpose. Usually we get it from JWT Context
            Integer accountId = 1;
            Integer spreadId = (Integer) payload.get("spreadId");
            String mode = (String) payload.get("mode");

            Map<String, Object> result = tarotReadingService.createSession(accountId, spreadId, mode);
            return ResponseBase.ok(result, "Session created successfully");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Người dùng lật bài (Gán thẻ bài vào vị trí)
     */
    @PostMapping("/sessions/{id}/draw")
    public ResponseEntity<ResponseBase<Map<String, Object>>> drawCards(@PathVariable("id") Integer sessionId,
            @RequestBody Map<String, Object> payload) {
        try {
            Boolean allowReversed = (Boolean) payload.getOrDefault("allowReversed", false);
            Map<String, Object> result = tarotReadingService.drawCards(sessionId, allowReversed);
            return ResponseBase.ok(result, "Cards drawn and saved to session");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Gọi trigger giải bài từ n8n/AI hoặc Fallback Local
     */
    @GetMapping("/sessions/{id}/interpret")
    public ResponseEntity<ResponseBase<Map<String, Object>>> interpretSession(@PathVariable("id") Integer sessionId) {
        try {
            Map<String, Object> result = tarotReadingService.interpretSession(sessionId);
            return ResponseBase.ok(result, "Interpretation generated");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Lấy danh sách các phiên đọc bài của tôi
     */
    @GetMapping("/sessions")
    public ResponseEntity<ResponseBase<List<ReadingSession>>> getMyReadingSessions() {
        try {
            // Hardcode accountId=1 for testing purpose. Usually we get it from JWT Context
            Integer accountId = 1;
            List<ReadingSession> sessions = tarotReadingService.getSessionsByAccount(accountId);
            return ResponseBase.ok(sessions, "Sessions retrieved successfully");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
