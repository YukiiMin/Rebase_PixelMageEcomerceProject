package com.example.PixelMageEcomerceProject.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.service.interfaces.NFCScanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/nfc")
@RequiredArgsConstructor
@Tag(name = "NFC Management", description = "APIs for scanning and linking NFC cards")
@SecurityRequirement(name = "bearerAuth")
public class NFCScanController {

    private final NFCScanService nfcScanService;

    @PostMapping("/scan")
    @Operation(summary = "Scan NFC card", description = "Scan an NFC card and return action (LINK_PROMPT or VIEW_CONTENT)")
    public ResponseEntity<ResponseBase<Map<String, Object>>> scanNFC(@RequestParam String nfcUid,
            @RequestParam Integer userId) {
        try {
            Map<String, Object> result = nfcScanService.scanNFC(nfcUid, userId);
            return ResponseBase.ok(result, "Scan successful");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/link")
    @Operation(summary = "Link NFC card", description = "Link a scanned NFC card to a user account")
    public ResponseEntity<ResponseBase<Map<String, Object>>> linkCard(@RequestParam String nfcUid,
            @RequestParam Integer userId) {
        try {
            Map<String, Object> result = nfcScanService.linkCard(nfcUid, userId);
            return ResponseBase.ok(result, "Card linked successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/unlink")
    @Operation(summary = "Unlink NFC card", description = "Unlink an NFC card from your account")
    public ResponseEntity<ResponseBase<Map<String, Object>>> unlinkCard(@RequestParam String nfcUid,
            @RequestParam Integer userId) {
        try {
            Map<String, Object> result = nfcScanService.unlinkCard(nfcUid, userId);
            return ResponseBase.ok(result, "Card unlinked successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
