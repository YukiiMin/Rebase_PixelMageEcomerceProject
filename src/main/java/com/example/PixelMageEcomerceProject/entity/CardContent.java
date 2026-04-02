package com.example.PixelMageEcomerceProject.entity;

import com.example.PixelMageEcomerceProject.enums.ContentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Integer contentId;

    // ── Relationship: CardContent N-1 CardTemplate ──────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_template_id", nullable = false, referencedColumnName = "card_template_id")
    @JsonBackReference("cardTemplate-cardContents")
    private CardTemplate cardTemplate;

    // ── Content Fields ──────────────────────────────────────────────────────
    /**
     * Heading for the content block, e.g. "Short Story", "Lịch sử hình thành".
     * Optional — FE renders as section title.
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * Type of content: STORY | IMAGE | VIDEO | GIF | LINK
     * Stored as String (EnumType.STRING) for readability in DB.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 20, nullable = false)
    private ContentType contentType;

    /**
     * Actual content:
     * - STORY  → raw narrative/lore text
     * - IMAGE  → Cloudinary image URL
     * - VIDEO  → Cloudinary video URL
     * - GIF    → Cloudinary GIF URL
     * - LINK   → external URL with optional label
     */
    @Column(name = "content_data", columnDefinition = "TEXT", nullable = false)
    private String contentData;

    /**
     * Display order for content blocks within a CardTemplate (1-based).
     * FE should sort ascending by this field.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 1;

    /**
     * Soft-delete / Admin visibility toggle.
     * false = hidden from public gallery; true = visible.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
