package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.CardTemplateRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.CardContentResponse;
import com.example.PixelMageEcomerceProject.dto.response.CardTemplateResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.enums.ArcanaType;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
// Removed old import
import com.example.PixelMageEcomerceProject.mapper.CardTemplateMapper;
import com.example.PixelMageEcomerceProject.service.interfaces.CardContentService;
import com.example.PixelMageEcomerceProject.service.interfaces.CardTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/card-templates")
@RequiredArgsConstructor
@Tag(name = "Card Template Management", description = "Public gallery browsing + Admin/Staff CRUD for CardTemplates")
public class CardTemplateController {

    private final CardTemplateService cardTemplateService;
    private final CardContentService cardContentService;
    private final CardTemplateMapper cardTemplateMapper;

    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC ENDPOINTS (Card Gallery — no auth)
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping
    @Operation(summary = "Get all active CardTemplates (Public — Gallery listing, paginated)", description = "Supports pagination: ?page=0&size=12&sort=name,asc. FE controls page size. Results NOT cached when paginated.")
    @ApiResponse(responseCode = "200", description = "Templates retrieved", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
    public ResponseEntity<ResponseBase<Page<CardTemplateResponse.Summary>>> getAllCardTemplates(Pageable pageable) {
        Page<CardTemplateResponse.Summary> page = cardTemplateService.getAllCardTemplates(pageable)
                .map(cardTemplateMapper::toSummaryResponse);
        return ResponseBase.ok(page, "Card templates retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get CardTemplate detail by ID (Public)", description = "Returns full template details including DivineHelper. CardContent is fetched separately via /contents sub-path.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template found"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<ResponseBase<CardTemplateResponse>> getCardTemplateById(
            @PathVariable Integer id) {
        return cardTemplateService.getCardTemplateById(id)
                .map(cardTemplateMapper::toResponse)
                .map(t -> ResponseBase.ok(t, "Card template found"))
                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                        "Card template not found with id: " + id));
    }

    @GetMapping("/{id}/contents")
    @Operation(summary = "Get active CardContent for a template (Public)", description = "Shortcut to retrieve the public content blocks for a template. "
            +
            "Returns only ACTIVE blocks, sorted by displayOrder. " +
            "FE should blur/lock these for Guest users and show CTA to Login.")
    @ApiResponse(responseCode = "200", description = "Content blocks retrieved")
    public ResponseEntity<ResponseBase<List<CardContentResponse>>> getActiveContents(
            @PathVariable Integer id) {
        List<CardContentResponse> list = cardContentService.getActiveContentsByTemplateId(id);
        return ResponseBase.ok(list, "Card contents retrieved successfully");
    }

    @GetMapping("/by-name")
    @Operation(summary = "Find template by exact name (Public)")
    public ResponseEntity<ResponseBase<CardTemplateResponse>> getByName(
            @Parameter(description = "Exact card template name") @RequestParam String name) {
        return cardTemplateService.getCardTemplateByName(name)
                .map(cardTemplateMapper::toResponse)
                .map(t -> ResponseBase.ok(t, "Card template found"))
                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                        "Card template not found with name: " + name));
    }

    @GetMapping("/by-rarity/{rarity}")
    @Operation(summary = "Filter templates by rarity (Public, paginated)", description = "rarity: COMMON|RARE|LEGENDARY. Supports ?page=0&size=12&sort=name,asc")
    public ResponseEntity<ResponseBase<Page<CardTemplateResponse.Summary>>> getByRarity(
            @PathVariable CardTemplateRarity rarity, Pageable pageable) {
        Page<CardTemplateResponse.Summary> page = cardTemplateService.getAllByRarity(rarity, pageable)
                .map(cardTemplateMapper::toSummaryResponse);
        return ResponseBase.ok(page, "Templates filtered by rarity: " + rarity);
    }

    @GetMapping("/by-arcana/{arcanaType}")
    @Operation(summary = "Filter templates by arcana type (Public, paginated)", description = "arcanaType: MAJOR|MINOR. Supports ?page=0&size=12&sort=name,asc")
    public ResponseEntity<ResponseBase<Page<CardTemplateResponse.Summary>>> getByArcana(
            @PathVariable ArcanaType arcanaType, Pageable pageable) {
        Page<CardTemplateResponse.Summary> page = cardTemplateService.getAllByArcana(arcanaType, pageable)
                .map(cardTemplateMapper::toSummaryResponse);
        return ResponseBase.ok(page, "Templates filtered by arcana type: " + arcanaType);
    }

    @GetMapping("/by-framework/{frameworkId}")
    @Operation(summary = "Filter templates by CardFramework ID (Public, paginated)", description = "Returns all templates belonging to a specific framework. Supports ?page=0&size=12&sort=name,asc")
    public ResponseEntity<ResponseBase<Page<CardTemplateResponse.Summary>>> getByFramework(
            @PathVariable String frameworkId, Pageable pageable) {
        Page<CardTemplateResponse.Summary> page = cardTemplateService.getAllByFramework(frameworkId, pageable)
                .map(cardTemplateMapper::toSummaryResponse);
        return ResponseBase.ok(page, "Templates filtered by framework: " + frameworkId);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ADMIN / STAFF WRITE ENDPOINTS (JWT required)
    // ═══════════════════════════════════════════════════════════════════════

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Secured({ "ROLE_ADMIN", "ROLE_STAFF" })
    @Operation(summary = "Create a new CardTemplate (Admin/Staff)", description = "After creating a template, add CardContent blocks via POST /api/card-contents.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<ResponseBase<CardTemplateResponse>> createCardTemplate(
            @Valid @RequestBody CardTemplateRequestDTO dto) {
        try {
            CardTemplate created = cardTemplateService.createCardTemplate(dto);
            return ResponseBase.created(cardTemplateMapper.toResponse(created), "Card template created successfully");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST,
                    "Failed to create card template: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Secured({ "ROLE_ADMIN", "ROLE_STAFF" })
    @Operation(summary = "Update an existing CardTemplate (Admin/Staff)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<ResponseBase<CardTemplateResponse>> updateCardTemplate(
            @PathVariable Integer id,
            @Valid @RequestBody CardTemplateRequestDTO dto) {
        try {
            CardTemplate updated = cardTemplateService.updateCardTemplate(id, dto);
            return ResponseBase.ok(cardTemplateMapper.toResponse(updated), "Card template updated successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Soft-delete a CardTemplate (Admin only)", description = "Sets is_active=false. Template is hidden from gallery. All associated CardContent blocks also become invisible (SQLRestriction on CardTemplate).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted (soft)"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<ResponseBase<Void>> deleteCardTemplate(
            @PathVariable Integer id) {
        try {
            cardTemplateService.deleteCardTemplate(id);
            return ResponseBase.ok(null, "Card template deleted successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
