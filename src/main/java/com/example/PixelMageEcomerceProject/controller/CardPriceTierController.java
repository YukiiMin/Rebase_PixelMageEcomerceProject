package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.CardPriceTierRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.CardPriceTier;
import com.example.PixelMageEcomerceProject.service.interfaces.CardPriceTierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/card-price-tiers")
@RequiredArgsConstructor
@Tag(name = "Card Price Tier Management", description = "APIs for managing card price tiers")
@SecurityRequirement(name = "bearerAuth")
public class CardPriceTierController {

        private final CardPriceTierService cardPriceTierService;

        @PostMapping
        @Operation(summary = "Create a new card price tier", description = "Create a new card price tier")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Card price tier created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardPriceTier>> createCardPriceTier(
                        @RequestBody CardPriceTierRequestDTO cardPriceTierRequestDTO) {
                try {
                        CardPriceTier createdTier = cardPriceTierService.createCardPriceTier(cardPriceTierRequestDTO);
                        return ResponseBase.created(createdTier, "Card price tier created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create card price tier: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all card price tiers", description = "Retrieve all card price tiers")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card price tiers retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CardPriceTier>>> getAllCardPriceTiers() {
                List<CardPriceTier> tiers = cardPriceTierService.getAllCardPriceTiers();
                return ResponseBase.ok(tiers, "Card price tiers retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get card price tier by ID", description = "Retrieve a card price tier by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card price tier found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card price tier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardPriceTier>> getCardPriceTierById(@PathVariable Integer id) {
                return cardPriceTierService.getCardPriceTierById(id)
                                .map(tier -> ResponseBase.ok(tier, "Card price tier found"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Card price tier not found with id: " + id));
        }

        @GetMapping("/template/{templateId}")
        @Operation(summary = "Get card price tiers by template ID", description = "Retrieve all price tiers for a card template")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card price tiers found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CardPriceTier>>> getCardPriceTiersByTemplateId(
                        @PathVariable Integer templateId) {
                List<CardPriceTier> tiers = cardPriceTierService.getCardPriceTiersByTemplateId(templateId);
                return ResponseBase.ok(tiers, "Card price tiers retrieved successfully");
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update card price tier", description = "Update an existing card price tier")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card price tier updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card price tier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardPriceTier>> updateCardPriceTier(@PathVariable Integer id,
                        @RequestBody CardPriceTierRequestDTO cardPriceTierRequestDTO) {
                try {
                        CardPriceTier updatedTier = cardPriceTierService.updateCardPriceTier(id,
                                        cardPriceTierRequestDTO);
                        return ResponseBase.ok(updatedTier, "Card price tier updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete card price tier", description = "Delete a card price tier by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card price tier deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card price tier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteCardPriceTier(@PathVariable Integer id) {
                try {
                        cardPriceTierService.deleteCardPriceTier(id);
                        return ResponseBase.ok(null, "Card price tier deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }
}
