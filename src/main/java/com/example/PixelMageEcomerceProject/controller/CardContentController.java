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

import com.example.PixelMageEcomerceProject.dto.request.CardContentRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.CardContent;
import com.example.PixelMageEcomerceProject.service.interfaces.CardContentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/card-contents")
@RequiredArgsConstructor
@Tag(name = "Card Content Management", description = "APIs for managing card contents")
@SecurityRequirement(name = "bearerAuth")
public class CardContentController {

        private final CardContentService cardContentService;

        @PostMapping
        @Operation(summary = "Create a new card content", description = "Create a new card content")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Card content created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardContent>> createCardContent(
                        @RequestBody CardContentRequestDTO cardContentRequestDTO) {
                try {
                        CardContent createdContent = cardContentService.createCardContent(cardContentRequestDTO);
                        return ResponseBase.created(createdContent, "Card content created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create card content: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all card contents", description = "Retrieve all card contents")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card contents retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CardContent>>> getAllCardContents() {
                List<CardContent> contents = cardContentService.getAllCardContents();
                return ResponseBase.ok(contents, "Card contents retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get card content by ID", description = "Retrieve a card content by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card content found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card content not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardContent>> getCardContentById(@PathVariable Integer id) {
                return cardContentService.getCardContentById(id)
                                .map(content -> ResponseBase.ok(content, "Card content found"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Card content not found with id: " + id));
        }

        @GetMapping("/card/{cardId}")
        @Operation(summary = "Get card contents by card ID", description = "Retrieve all contents for a specific card")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card contents found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CardContent>>> getCardContentsByCardId(@PathVariable Integer cardId) {
                List<CardContent> contents = cardContentService.getCardContentsByCardId(cardId);
                return ResponseBase.ok(contents, "Card contents retrieved successfully");
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update card content", description = "Update an existing card content")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card content updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card content not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardContent>> updateCardContent(@PathVariable Integer id,
                        @RequestBody CardContentRequestDTO cardContentRequestDTO) {
                try {
                        CardContent updatedContent = cardContentService.updateCardContent(id, cardContentRequestDTO);
                        return ResponseBase.ok(updatedContent, "Card content updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete card content", description = "Delete a card content by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card content deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card content not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteCardContent(@PathVariable Integer id) {
                try {
                        cardContentService.deleteCardContent(id);
                        return ResponseBase.ok(null, "Card content deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }
}
