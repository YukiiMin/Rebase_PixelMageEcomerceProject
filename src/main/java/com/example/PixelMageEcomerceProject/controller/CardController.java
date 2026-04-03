package com.example.PixelMageEcomerceProject.controller;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.CardRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.service.interfaces.CardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "APIs for managing cards")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

        private final CardService cardService;

        @PostMapping("/create")
        @Operation(summary = "Create a new card product (PENDING_BIND)", description = "Create a new card product without NFC bind")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Card created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Card>> createCard(@RequestBody CardRequestDTO cardRequestDTO) {
                try {
                        Card createdCard = cardService.createCardProduct(cardRequestDTO);
                        return ResponseBase.created(createdCard, "Card created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to create card: " + e.getMessage());
                }
        }

        @PostMapping("/bind")
        @Operation(summary = "Bind NFC to a card", description = "Assigns an NFC UID to a card and changes status to READY")
        public ResponseEntity<ResponseBase<Card>> bindNFC(@RequestParam Integer cardId, @RequestParam String nfcUid) {
                try {
                        Card card = cardService.bindNFC(cardId, nfcUid);
                        return ResponseBase.ok(card, "NFC bound successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.CONFLICT, e.getMessage());
                }
        }

        @PutMapping("/{id}/status")
        @Operation(summary = "Update card status", description = "Override card status directly")
        public ResponseEntity<ResponseBase<Card>> updateStatus(@PathVariable Integer id,
                        @RequestParam String newStatus) {
                try {
                        Card card = cardService.updateStatus(id, newStatus);
                        return ResponseBase.ok(card, "Status updated successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST, e.getMessage());
                }
        }

        @GetMapping("/list")
        @Operation(summary = "Get all cards (paginated)",
                   description = "Supports pagination: ?page=0&size=20&sort=cardId,desc. FE controls page size.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cards retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Page<Card>>> getAllCards(Pageable pageable) {
                Page<Card> cards = cardService.getAllCards(pageable);
                return ResponseBase.ok(cards, "Cards retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get card by ID", description = "Retrieve a card by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Card>> getCardById(@PathVariable Integer id) {
                return cardService.getCardById(id)
                                .map(card -> ResponseBase.ok(card, "Card found"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Card not found with id: " + id));
        }

        @GetMapping("/nfc/{nfcUid}")
        @Operation(summary = "Get card by NFC UID", description = "Retrieve a card by its NFC UID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Card>> getCardByNfcUid(@PathVariable String nfcUid) {
                return cardService.getCardByNfcUid(nfcUid)
                                .map(card -> ResponseBase.ok(card, "Card found"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Card not found with NFC UID: " + nfcUid));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update card", description = "Update an existing card")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Card>> updateCard(@PathVariable Integer id,
                        @RequestBody CardRequestDTO cardRequestDTO) {
                try {
                        Card updatedCard = cardService.updateCard(id, cardRequestDTO);
                        return ResponseBase.ok(updatedCard, "Card updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete card", description = "Delete a card by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteCard(@PathVariable Integer id) {
                try {
                        cardService.deleteCard(id);
                        return ResponseBase.ok(null, "Card deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }
}
