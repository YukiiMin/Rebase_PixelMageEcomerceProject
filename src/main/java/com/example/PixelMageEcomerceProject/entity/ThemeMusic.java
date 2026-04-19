package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theme_music")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "music_id")
    private Long musicId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "artist", length = 200)
    private String artist;

    /** Cloudinary public_id for deletion */
    @Column(name = "cloudinary_public_id", nullable = false, length = 500)
    private String cloudinaryPublicId;

    /** Direct Cloudinary URL (mp3/ogg) */
    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
