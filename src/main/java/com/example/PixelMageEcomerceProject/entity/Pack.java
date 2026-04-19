package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.example.PixelMageEcomerceProject.enums.PackStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(
    name = "Pack.withDetails",
    attributeNodes = {
        @NamedAttributeNode("packCategory"),
        @NamedAttributeNode("packDetails")
    }
)
@Table(name = "PACKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Pack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pack_id")
    private Integer packId;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_category_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference("pack-category-packs")
    private PackCategory packCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PackStatus status = PackStatus.STOCKED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_account_id")
    private Account createdBy; // Staff who created pack

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PackDetail> packDetails;
}
