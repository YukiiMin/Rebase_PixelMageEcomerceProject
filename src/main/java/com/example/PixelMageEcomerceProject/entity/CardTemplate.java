package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_templates")
@SQLRestriction("is_active = true")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_template_id")
    private Integer cardTemplateId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "design_path", length = 255)
    private String designPath;

    @Column(name = "arcana_type", length = 50)
    private String arcanaType; // Major, Minor

    @Column(name = "suit", length = 50)
    private String suit; // Wands, Cups, Swords, Pentacles

    @Column(name = "card_number")
    private Integer cardNumber;

    @Column(name = "rarity", length = 50)
    private String rarity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "framework_id")
    @com.fasterxml.jackson.annotation.JsonBackReference("framework-cardTemplates")
    private CardFramework cardFramework;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship: CardTemplate 1-N Card
    @OneToMany(mappedBy = "cardTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("cardTemplate-cards")
    private List<Card> cards;

    // Relationship: CardTemplate 1-N CardPriceTier
    @OneToMany(mappedBy = "cardTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("cardTemplate-cardPriceTiers")
    private List<CardPriceTier> cardPriceTiers;

    // Relationship: CardTemplate 1-1 DivineHelper
    @OneToOne(mappedBy = "cardTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("cardTemplate-divineHelper")
    private DivineHelper divineHelper;
}
