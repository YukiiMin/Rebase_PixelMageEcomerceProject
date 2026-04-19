package com.example.PixelMageEcomerceProject.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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
@Table(name = "PRODUCTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship: Product 1-N Inventory
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("product-inventories")
    @JsonIgnore
    private List<Inventory> inventories;


    // Relationship: Product 1-N Card
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("product-cards")
    @JsonIgnore
    private List<Card> cards;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private com.example.PixelMageEcomerceProject.enums.ProductType productType = com.example.PixelMageEcomerceProject.enums.ProductType.GACHA_PACK;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Relationship: Product -> PackCategory
    @jakarta.persistence.ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_category_id")
    private PackCategory packCategory;

    // Relationship: Product -> CardTemplate
    @jakarta.persistence.ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_template_id")
    private CardTemplate cardTemplate;
}
