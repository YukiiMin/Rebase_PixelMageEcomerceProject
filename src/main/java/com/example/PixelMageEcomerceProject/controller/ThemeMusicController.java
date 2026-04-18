package com.example.PixelMageEcomerceProject.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.PixelMageEcomerceProject.dto.ThemeMusicDto;
import com.example.PixelMageEcomerceProject.service.ThemeMusicService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/theme-music")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ThemeMusic", description = "Quản lý nhạc theme hệ thống (Admin) & Public endpoint active track")
public class ThemeMusicController {

    private final ThemeMusicService service;

    // ── PUBLIC: Lấy bài nhạc đang active (User & anonymous cần biết URL) ─────
    @GetMapping("/active")
    @Operation(summary = "Lấy bài nhạc theme đang phát (public)")
    public ResponseEntity<?> getActive() {
        ThemeMusicDto.Response active = service.findActive();
        if (active == null) {
            return ResponseEntity.ok(Map.of("active", false, "message", "Chưa có nhạc theme được thiết lập"));
        }
        return ResponseEntity.ok(active);
    }

    // ── ADMIN: Xem danh sách toàn bộ nhạc ────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "Danh sách tất cả bài nhạc theme (Admin/Staff)")
    public ResponseEntity<List<ThemeMusicDto.Response>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // ── ADMIN: Upload nhạc mới lên Cloudinary ────────────────────────────────
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "Upload bài nhạc lên Cloudinary (Admin)")
    public ResponseEntity<ThemeMusicDto.Response> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "artist", required = false) String artist) {

        log.info("[ThemeMusic] Upload request: title={}, artist={}, size={}KB",
                title, artist, file.getSize() / 1024);
        return ResponseEntity.ok(service.upload(file, title, artist));
    }

    // ── ADMIN: Kích hoạt một bài (deactivate tất cả trước) ───────────────────
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "Kích hoạt bài nhạc thành theme đang phát (Admin)")
    public ResponseEntity<ThemeMusicDto.Response> activate(@PathVariable("id") Long musicId) {
        log.info("[ThemeMusic] Activate: id={}", musicId);
        return ResponseEntity.ok(service.activate(musicId));
    }

    // ── ADMIN: Xoá bài khỏi DB + Cloudinary ─────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "Xoá bài nhạc (Admin) — xoá cả Cloudinary")
    public ResponseEntity<Void> delete(@PathVariable("id") Long musicId) {
        log.info("[ThemeMusic] Delete: id={}", musicId);
        service.delete(musicId);
        return ResponseEntity.noContent().build();
    }
}
