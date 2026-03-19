package com.example.PixelMageEcomerceProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reading_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_card_id")
    private Integer readingCardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonBackReference("readingSession-readingCards") 
    private ReadingSession readingSession;

    @ManyToOne(fetch = FetchType.EAGER) // Need to fetch template info immediately
    @JoinColumn(name = "card_template_id", nullable = false)
    private CardTemplate cardTemplate;

    @Column(name = "position_index", nullable = false)
    private Integer positionIndex; // e.g. 0 -> Past, 1 -> Present

    @Column(name = "position_name", length = 100)
    private String positionName; // E.g., "Quá Khứ" or "Hiện Tại"

    @Column(name = "is_reversed", nullable = false)
    @Builder.Default
    private Boolean isReversed = false;
}
