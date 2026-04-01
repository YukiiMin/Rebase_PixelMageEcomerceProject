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

import com.example.PixelMageEcomerceProject.dto.request.CollectionItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.CollectionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.CollectionItemResponse;
import com.example.PixelMageEcomerceProject.dto.response.CollectionResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.service.interfaces.CollectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
@Tag(name = "Collection Management", description = "APIs for managing card collections (bộ sưu tập thẻ)")
@SecurityRequirement(name = "bearerAuth")
public class CollectionController {

        private final CollectionService collectionService;

        // ==================== Collection CRUD ====================

        @PostMapping("/{customerId}")
        @Operation(summary = "Create a new collection", description = "Create a new card collection for a customer")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Collection created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CollectionResponse>> createCollection(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId,
                        @RequestBody CollectionRequestDTO request) {
                try {
                        CollectionResponse collection = collectionService.createCollection(customerId, request);
                        return ResponseBase.created(collection, "Collection created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create collection: " + e.getMessage());
                }
        }

        @PutMapping("/{customerId}/{collectionId}")
        @Operation(summary = "Update a collection", description = "Update collection name, description, or visibility")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Collection updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CollectionResponse>> updateCollection(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId,
                        @Parameter(description = "Collection ID") @PathVariable Integer collectionId,
                        @RequestBody CollectionRequestDTO request) {
                try {
                        CollectionResponse collection = collectionService.updateCollection(customerId, collectionId,
                                        request);
                        return ResponseBase.ok(collection, "Collection updated successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to update collection: " + e.getMessage());
                }
        }

        @DeleteMapping("/{customerId}/{collectionId}")
        @Operation(summary = "Delete a collection", description = "Delete a collection and all its items")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Collection deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteCollection(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId,
                        @Parameter(description = "Collection ID") @PathVariable Integer collectionId) {
                try {
                        collectionService.deleteCollection(customerId, collectionId);
                        return ResponseBase.ok(null, "Collection deleted successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to delete collection: " + e.getMessage());
                }
        }

        @GetMapping("/{collectionId}")
        @Operation(summary = "Get collection by ID", description = "Retrieve a specific collection with its items")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Collection found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Collection not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CollectionResponse>> getCollectionById(
                        @Parameter(description = "Collection ID") @PathVariable Integer collectionId) {
                return collectionService.getCollectionById(collectionId)
                                .map(collection -> ResponseBase.ok(collection, "Collection retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Collection not found with id: " + collectionId));
        }

        @GetMapping("/customer/{customerId}")
        @Operation(summary = "Get customer's collections", description = "Retrieve all collections for a customer")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Collections retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CollectionResponse>>> getCollectionsByCustomerId(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId) {
                List<CollectionResponse> collections = collectionService.getCollectionsByCustomerId(customerId);
                return ResponseBase.ok(collections, "Collections retrieved successfully");
        }

        @GetMapping("/public")
        @Operation(summary = "Get public collections", description = "Retrieve all public collections from all users")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Public collections retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CollectionResponse>>> getPublicCollections() {
                List<CollectionResponse> collections = collectionService.getPublicCollections();
                return ResponseBase.ok(collections, "Public collections retrieved successfully");
        }

        // ==================== Collection Items ====================

        @PostMapping("/items/{customerId}")
        @Operation(summary = "Add card to collection", description = "Add an owned card to a collection.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Card added to collection successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Card not owned or already in collection", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CollectionItemResponse>> addCardToCollection(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId,
                        @RequestBody CollectionItemRequestDTO request) {
                try {
                        CollectionItemResponse item = collectionService.addCardToCollection(customerId, request);
                        return ResponseBase.created(item, "Card added to collection successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to add card to collection: " + e.getMessage());
                }
        }

        @DeleteMapping("/items/{customerId}/{collectionId}/{cardId}")
        @Operation(summary = "Remove card from collection", description = "Remove a card from a collection")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card removed from collection successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card not found in collection", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> removeCardFromCollection(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId,
                        @Parameter(description = "Collection ID") @PathVariable Integer collectionId,
                        @Parameter(description = "Card ID") @PathVariable Integer cardId) {
                try {
                        collectionService.removeCardFromCollection(customerId, collectionId, cardId);
                        return ResponseBase.ok(null, "Card removed from collection successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Failed to remove card: " + e.getMessage());
                }
        }

        @GetMapping("/items/{collectionId}")
        @Operation(summary = "Get collection items", description = "Get all cards in a collection")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Collection items retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CollectionItemResponse>>> getCollectionItems(
                        @Parameter(description = "Collection ID") @PathVariable Integer collectionId) {
                List<CollectionItemResponse> items = collectionService.getCollectionItems(collectionId);
                return ResponseBase.ok(items, "Collection items retrieved successfully");
        }

        // ==================== Owned Cards ====================

        @GetMapping("/owned-cards/{customerId}")
        @Operation(summary = "Get owned cards", description = "Get all cards owned by a customer (purchased through COMPLETED & PAID orders)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Owned cards retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<Card>>> getOwnedCards(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId) {
                List<Card> ownedCards = collectionService.getOwnedCards(customerId);
                return ResponseBase.ok(ownedCards,
                                "Owned cards retrieved successfully (" + ownedCards.size() + " cards)");
        }
}
