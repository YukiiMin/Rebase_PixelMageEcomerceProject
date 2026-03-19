package com.example.PixelMageEcomerceProject.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spreads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spread_id")
    private Integer spreadId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "VARCHAR(500)")
    private String description;

    @Column(name = "position_count", nullable = false)
    private Integer positionCount;

    @Column(name = "min_cards_required")
    private Integer minCardsRequired;

    @OneToMany(mappedBy = "spread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("spread-readingSessions")
    private List<ReadingSession> readingSessions;
}
