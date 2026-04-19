package com.example.PixelMageEcomerceProject.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.dto.response.SpreadResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.service.interfaces.TarotReadingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            log.error("[TAROT] getSpreads failed: {}", e.getMessage(), e);
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Bắt đầu một phiên bốc bài mới
     */
    @PostMapping("/sessions")
    public ResponseEntity<ResponseBase<Map<String, Object>>> createSession(
            @RequestBody Map<String, Object> payload,
            org.springframework.security.core.Authentication auth) {
        try {
            Integer accountId = extractAccountId(auth);
            Integer spreadId = (Integer) payload.get("spreadId");
            String mode = (String) payload.get("mode");
            String mainQuestion = (String) payload.get("mainQuestion");
            log.info("[TAROT] createSession: accountId={}, spreadId={}, mode={}, mainQuestion={}", accountId, spreadId, mode, mainQuestion);

            Map<String, Object> result = tarotReadingService.createSession(accountId, spreadId, mode, mainQuestion);
            return ResponseBase.ok(result, "Session created successfully");
        } catch (IllegalStateException e) {
            log.warn("[TAROT] createSession auth error: {}", e.getMessage());
            return ResponseBase.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (com.example.PixelMageEcomerceProject.exceptions.ActiveSessionExistsException e) {
            log.warn("[TAROT] createSession conflict: {}", e.getMessage());
            return ResponseBase.error(HttpStatus.CONFLICT, e.getMessage(), Map.of("activeSessionId", e.getActiveSessionId()));
        } catch (Exception e) {
            log.error("[TAROT] createSession failed: {}", e.getMessage(), e);
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
            log.error("[TAROT] drawCards sessionId={} failed: {}", sessionId, e.getMessage(), e);
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
            log.error("[TAROT] interpretSession sessionId={} failed: {}", sessionId, e.getMessage(), e);
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Lấy thông tin chi tiết một phiên
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ResponseBase<ReadingSession>> getSessionById(@PathVariable("id") Integer sessionId) {
        try {
            ReadingSession session = tarotReadingService.getSessionById(sessionId);
            return ResponseBase.ok(session, "Session retrieved successfully");
        } catch (Exception e) {
            log.error("[TAROT] getSessionById failed: {}", e.getMessage(), e);
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Lấy danh sách các phiên đọc bài của tôi
     */
    @GetMapping("/sessions")
    public ResponseEntity<ResponseBase<List<ReadingSession>>> getMyReadingSessions(
            org.springframework.security.core.Authentication auth) {
        try {
            Integer accountId = extractAccountId(auth);
            List<ReadingSession> sessions = tarotReadingService.getSessionsByAccount(accountId);
            return ResponseBase.ok(sessions, "Sessions retrieved successfully");
        } catch (IllegalStateException e) {
            log.warn("[TAROT] getMyReadingSessions auth error: {}", e.getMessage());
            return ResponseBase.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("[TAROT] getMyReadingSessions failed: {}", e.getMessage(), e);
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Hủy / bỏ giữa chừng một phiên đang PENDING hoặc INTERPRETING.
     * Idempotent — nếu session đã COMPLETED/EXPIRED thì trả về 200 bình thường.
     */
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ResponseBase<Void>> cancelSession(
            @PathVariable("id") Integer sessionId,
            org.springframework.security.core.Authentication auth) {
        try {
            Integer accountId = extractAccountId(auth);
            tarotReadingService.cancelSession(sessionId, accountId);
            log.info("[TAROT] cancelSession #{} — OK by accountId={}", sessionId, accountId);
            return ResponseBase.ok(null, "Session cancelled successfully");
        } catch (IllegalStateException e) {
            log.warn("[TAROT] cancelSession auth error: {}", e.getMessage());
            return ResponseBase.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("[TAROT] cancelSession #{} failed: {}", sessionId, e.getMessage(), e);
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Extract accountId from the JWT Authentication context.
     * Throws IllegalStateException if the user is not authenticated.
     */
    private Integer extractAccountId(org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Account account) {
            return account.getCustomerId();
        }
        throw new IllegalStateException("Cannot extract accountId from authentication context: unexpected principal type " + principal.getClass().getSimpleName());
    }
}
