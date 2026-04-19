package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PACK_CATEGORIES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PackCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pack_category_id")
    private Integer packCategoryId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "cards_per_pack", nullable = false)
    private Integer cardsPerPack = 5;

    @Column(name = "rarity_rates", columnDefinition = "TEXT")
    private String rarityRates; // JSON string: {"COMMON":70, "RARE":25, "LEGENDARY":5}

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship: PackCategory M-N CardTemplate (Gacha Pool)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "pack_category_card_pools",
        joinColumns = @JoinColumn(name = "pack_category_id"),
        inverseJoinColumns = @JoinColumn(name = "card_template_id")
    )
    @JsonIgnore
    private List<CardTemplate> cardPools;

    // Packs created from this category
    @OneToMany(mappedBy = "packCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Pack> packs;
    
    // Storefront Products linked to this category
    @OneToMany(mappedBy = "packCategory", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;
}
