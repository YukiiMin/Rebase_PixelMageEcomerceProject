package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USER_STORY_UNLOCKS", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "story_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStoryUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unlock_id")
    private Integer unlockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private SetStory story;

    @CreationTimestamp
    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private LocalDateTime unlockedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

