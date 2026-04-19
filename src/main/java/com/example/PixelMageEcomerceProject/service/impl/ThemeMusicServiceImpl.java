package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.PixelMageEcomerceProject.dto.ThemeMusicDto;
import com.example.PixelMageEcomerceProject.entity.ThemeMusic;
import com.example.PixelMageEcomerceProject.repository.ThemeMusicRepository;
import com.example.PixelMageEcomerceProject.service.ThemeMusicService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThemeMusicServiceImpl implements ThemeMusicService {

    private final ThemeMusicRepository repo;
    private final Cloudinary cloudinary;

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private ThemeMusicDto.Response toResponse(ThemeMusic m) {
        return ThemeMusicDto.Response.builder()
                .musicId(m.getMusicId())
                .title(m.getTitle())
                .artist(m.getArtist())
                .url(m.getUrl())
                .active(m.getActive())
                .createdAt(m.getCreatedAt())
                .build();
    }

    // ─── Service Methods ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public ThemeMusicDto.Response upload(MultipartFile file, String title, String artist) {
        try {
            log.info("[ThemeMusic] Uploading: {} ({})", title, file.getOriginalFilename());

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",  // Cloudinary uses "video" type for audio files
                            "folder", "pixelmage/theme-music",
                            "use_filename", true,
                            "unique_filename", true,
                            "access_mode", "public"
                    )
            );

            String publicId = (String) result.get("public_id");
            String url = (String) result.get("secure_url");

            ThemeMusic music = ThemeMusic.builder()
                    .title(title)
                    .artist(artist)
                    .cloudinaryPublicId(publicId)
                    .url(url)
                    .active(false)
                    .build();

            ThemeMusic saved = repo.save(music);
            log.info("[ThemeMusic] Uploaded & saved: id={}, url={}", saved.getMusicId(), url);
            return toResponse(saved);

        } catch (Exception e) {
            log.error("[ThemeMusic] Upload failed", e);
            throw new RuntimeException("Không thể upload nhạc lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ThemeMusicDto.Response> findAll() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ThemeMusicDto.Response findActive() {
        return repo.findByActiveTrue()
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public ThemeMusicDto.Response activate(Long musicId) {
        ThemeMusic music = repo.findById(musicId)
                .orElseThrow(() -> new RuntimeException("ThemeMusic không tồn tại: " + musicId));

        // Deactivate tất cả trước
        repo.deactivateAll();

        // Activate cái được chọn
        music.setActive(true);
        ThemeMusic saved = repo.save(music);
        log.info("[ThemeMusic] Activated: id={}, title={}", saved.getMusicId(), saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long musicId) {
        ThemeMusic music = repo.findById(musicId)
                .orElseThrow(() -> new RuntimeException("ThemeMusic không tồn tại: " + musicId));

        // Xoá khỏi Cloudinary
        try {
            cloudinary.uploader().destroy(
                    music.getCloudinaryPublicId(),
                    ObjectUtils.asMap("resource_type", "video")
            );
            log.info("[ThemeMusic] Deleted from Cloudinary: {}", music.getCloudinaryPublicId());
        } catch (Exception e) {
            log.warn("[ThemeMusic] Cloudinary deletion failed (still removing from DB): {}", e.getMessage());
        }

        repo.delete(music);
        log.info("[ThemeMusic] Deleted from DB: id={}", musicId);
    }
}
