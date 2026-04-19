package com.example.PixelMageEcomerceProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PACK_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pack_detail_id")
    private Integer packDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_id", nullable = false)
    @JsonBackReference("pack-packDetails")
    private Pack pack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    @JsonIgnore
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_template_id")
    @JsonIgnore
    private CardTemplate cardTemplate;

    @Column(name = "position_index")
    private Integer positionIndex;
}
