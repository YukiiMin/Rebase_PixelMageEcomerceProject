package com.example.PixelMageEcomerceProject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.AdminCollectionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.CardCollection;
import com.example.PixelMageEcomerceProject.service.interfaces.CollectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/collections")
@RequiredArgsConstructor
@Tag(name = "Admin Collection Management", description = "Admin APIs for managing system collections")
@SecurityRequirement(name = "bearerAuth")
public class AdminCollectionController {

        private final CollectionService collectionService;

        @PostMapping
        @Operation(summary = "Create admin-controlled collection", description = "Create a system collection with type, time window, rewards and items")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Admin collection created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardCollection>> createAdminCollection(
                        @RequestParam Integer adminId,
                        @RequestBody AdminCollectionRequestDTO request) {
                try {
                        CardCollection collection = collectionService.createAdminCollection(adminId, request);
                        return ResponseBase.created(collection, "Admin collection created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create admin collection: " + e.getMessage());
                }
        }

        @PutMapping("/{id}/visibility")
        @Operation(summary = "Update collection visibility", description = "Toggle visibility of a system collection (including HIDDEN/ACHIEVEMENT types)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Collection visibility updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardCollection>> updateVisibility(
                        @PathVariable Integer id,
                        @RequestParam Boolean isVisible) {
                try {
                        CardCollection collection = collectionService.updateCollectionVisibility(id, isVisible);
                        return ResponseBase.ok(collection, "Collection visibility updated successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to update collection visibility: " + e.getMessage());
                }
        }
}
