package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.CardContentRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.CardContentResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.service.interfaces.CardContentService;

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
@RequestMapping("/api/card-contents")
@RequiredArgsConstructor
@Tag(name = "Card Content Management", description = "CRUD for CardContent blocks attached to CardTemplates")
public class CardContentController {

    private final CardContentService cardContentService;

    // ═══════════════════════════════════════════════════════════════════════
    //  PUBLIC ENDPOINTS (no auth required — FE decides what to blur/lock)
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/template/{templateId}")
    @Operation(
        summary = "Get active contents by CardTemplate (Public)",
        description = "Returns all ACTIVE content blocks for a given CardTemplate, ordered by displayOrder. " +
                      "Auth NOT required — FE controls Guest lock/blur behaviour client-side."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content blocks retrieved",
                     content = @Content(schema = @Schema(implementation = ResponseBase.class))),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<ResponseBase<List<CardContentResponse>>> getActiveContents(
            @Parameter(description = "CardTemplate ID") @PathVariable Integer templateId) {
        List<CardContentResponse> list = cardContentService.getActiveContentsByTemplateId(templateId);
        return ResponseBase.ok(list, "Card contents retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single content block by ID (Public)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content item found"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<ResponseBase<CardContentResponse>> getById(
            @PathVariable Integer id) {
        return cardContentService.getCardContentById(id)
                .map(c -> ResponseBase.ok(c, "Card content found"))
                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                    "CardContent not found with id: " + id));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ADMIN / STAFF ENDPOINTS (JWT required)
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/admin/template/{templateId}")
    @SecurityRequirement(name = "bearerAuth")
    @Secured({"ROLE_ADMIN", "ROLE_STAFF"})
    @Operation(
        summary = "Get ALL contents by template — including hidden (Admin/Staff)",
        description = "Returns both active and inactive content blocks for a template. Requires authentication."
    )
    public ResponseEntity<ResponseBase<List<CardContentResponse>>> getAllContentsForAdmin(
            @PathVariable Integer templateId) {
        List<CardContentResponse> list = cardContentService.getAllContentsByTemplateId(templateId);
        return ResponseBase.ok(list, "All card contents retrieved successfully");
    }

    @GetMapping("/admin")
    @SecurityRequirement(name = "bearerAuth")
    @Secured({"ROLE_ADMIN", "ROLE_STAFF"})
    @Operation(summary = "Get all CardContent across all templates (Admin overview)")
    public ResponseEntity<ResponseBase<List<CardContentResponse>>> getAllCardContents() {
        return ResponseBase.ok(cardContentService.getAllCardContents(),
                               "All card contents retrieved");
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Secured({"ROLE_ADMIN", "ROLE_STAFF"})
    @Operation(
        summary = "Create a new content block (Admin/Staff)",
        description = "Add a content block to a CardTemplate. contentType: STORY | IMAGE | VIDEO | GIF | LINK"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "CardTemplate not found")
    })
    public ResponseEntity<ResponseBase<CardContentResponse>> create(
            @Valid @RequestBody CardContentRequestDTO dto) {
        try {
            CardContentResponse created = cardContentService.createCardContent(dto);
            return ResponseBase.created(created, "Card content created successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Secured({"ROLE_ADMIN", "ROLE_STAFF"})
    @Operation(summary = "Update a content block (Admin/Staff)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated successfully"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<ResponseBase<CardContentResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody CardContentRequestDTO dto) {
        try {
            return ResponseBase.ok(cardContentService.updateCardContent(id, dto),
                                   "Card content updated successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PatchMapping("/{id}/toggle-active")
    @SecurityRequirement(name = "bearerAuth")
    @Secured({"ROLE_ADMIN", "ROLE_STAFF"})
    @Operation(
        summary = "Toggle visibility of a content block (Admin/Staff)",
        description = "Set isActive=true to show or isActive=false to hide a content block from the public gallery."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Toggled successfully"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<ResponseBase<CardContentResponse>> toggleActive(
            @PathVariable Integer id,
            @Parameter(description = "true = visible, false = hidden")
            @RequestParam boolean isActive) {
        try {
            return ResponseBase.ok(cardContentService.toggleActive(id, isActive),
                                   "Visibility updated to: " + isActive);
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Hard delete a content block (Admin only)",
        description = "Permanently removes the content block. Prefer toggle-active to hide instead of deleting."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<ResponseBase<Void>> delete(@PathVariable Integer id) {
        try {
            cardContentService.deleteCardContent(id);
            return ResponseBase.ok(null, "Card content deleted successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
