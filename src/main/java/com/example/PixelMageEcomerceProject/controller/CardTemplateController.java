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

import com.example.PixelMageEcomerceProject.dto.request.CardTemplateRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.service.interfaces.CardTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/card-templates")
@RequiredArgsConstructor
@Tag(name = "Card Template Management", description = "APIs for managing card templates")
@SecurityRequirement(name = "bearerAuth")
public class CardTemplateController {

        private final CardTemplateService cardTemplateService;

        @PostMapping
        @Operation(summary = "Create a new card template", description = "Create a new card template")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Card template created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardTemplate>> createCardTemplate(
                        @RequestBody CardTemplateRequestDTO cardTemplateRequestDTO) {
                try {
                        CardTemplate createdTemplate = cardTemplateService.createCardTemplate(cardTemplateRequestDTO);
                        return ResponseBase.created(createdTemplate, "Card template created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create card template: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all card templates", description = "Retrieve all card templates")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card templates retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<CardTemplate>>> getAllCardTemplates() {
                List<CardTemplate> templates = cardTemplateService.getAllCardTemplates();
                return ResponseBase.ok(templates, "Card templates retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get card template by ID", description = "Retrieve a card template by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card template found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card template not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardTemplate>> getCardTemplateById(@PathVariable Integer id) {
                return cardTemplateService.getCardTemplateById(id)
                                .map(template -> ResponseBase.ok(template, "Card template found"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Card template not found with id: " + id));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update card template", description = "Update an existing card template")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card template updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card template not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<CardTemplate>> updateCardTemplate(@PathVariable Integer id,
                        @RequestBody CardTemplateRequestDTO cardTemplateRequestDTO) {
                try {
                        CardTemplate updatedTemplate = cardTemplateService.updateCardTemplate(id,
                                        cardTemplateRequestDTO);
                        return ResponseBase.ok(updatedTemplate, "Card template updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete card template", description = "Delete a card template by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card template deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Card template not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteCardTemplate(@PathVariable Integer id) {
                try {
                        cardTemplateService.deleteCardTemplate(id);
                        return ResponseBase.ok(null, "Card template deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }
}
