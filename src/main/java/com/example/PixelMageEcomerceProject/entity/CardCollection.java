package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.PixelMageEcomerceProject.enums.CollectionType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "COLLECTIONS")
@SQLRestriction("is_active = true")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id")
    private Integer collectionId;

    @Column(name = "collection_name", nullable = false, length = 255)
    private String collectionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false, referencedColumnName = "customer_id")
    @JsonBackReference("account-collections")
    private Account account;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", length = 20)
    private CollectionType collectionType = CollectionType.STANDARD;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Column(name = "reward_type", length = 50)
    private String rewardType;

    @Column(name = "reward_data", columnDefinition = "TEXT")
    private String rewardData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id")
    private Account createdByAdmin;

    @Column(name = "source", length = 10, nullable = false)
    private String source = "SYSTEM";

    // Relationship: Collection 1-N CollectionItem
    @OneToMany(mappedBy = "cardCollection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("collection-collectionItems")
    private List<CollectionItem> collectionItems;
}
