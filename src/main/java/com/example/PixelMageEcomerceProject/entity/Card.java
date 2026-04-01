package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "CARDS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Integer cardId;

    @Column(name = "nfc_uid", unique = true)
    private String nfcUid;

    @Column(name = "software_uuid", unique = true)
    private String softwareUuid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "card_template_id", nullable = false, referencedColumnName = "card_template_id")
    @JsonBackReference("cardTemplate-cards")
    private CardTemplate cardTemplate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "product_id")
    @JsonBackReference("product-cards")
    private Product product;

    @Column(name = "custom_text", columnDefinition = "TEXT")
    private String customText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardProductStatus status = CardProductStatus.PENDING_BIND;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "production_batch", length = 100)
    private String productionBatch;

    @Column(name = "card_condition", length = 20)
    private String cardCondition = "NEW";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_account_id", referencedColumnName = "customer_id")
    private Account owner;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    // Relationship: Card 1-N CardContent
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("card-cardContents")
    @JsonIgnore
    private List<CardContent> cardContents;

}
