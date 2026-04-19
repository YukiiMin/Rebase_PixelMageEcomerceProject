package com.example.PixelMageEcomerceProject.controller;

import com.example.PixelMageEcomerceProject.dto.request.PackCategoryRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PackCategoryResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.service.interfaces.PackCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pack-categories")
@RequiredArgsConstructor
@Tag(name = "Pack Category Management", description = "APIs for managing pack categories")
@SecurityRequirement(name = "bearerAuth")
public class PackCategoryController {

    private final PackCategoryService packCategoryService;

    @PostMapping
    @Operation(summary = "Create a new pack category", description = "Create a new pack category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pack Category created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
    })
    public ResponseEntity<ResponseBase<PackCategoryResponse>> createPackCategory(@RequestBody PackCategoryRequestDTO requestDTO) {
        try {
            PackCategoryResponse created = packCategoryService.createPackCategory(requestDTO);
            return ResponseBase.created(created, "Pack Category created successfully");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to create pack category: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all pack categories", description = "Retrieve all pack categories")
    public ResponseEntity<ResponseBase<List<PackCategoryResponse>>> getAllPackCategories() {
        List<PackCategoryResponse> categories = packCategoryService.getAllPackCategories();
        return ResponseBase.ok(categories, "Pack categories retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pack category by ID", description = "Retrieve a pack category by its ID")
    public ResponseEntity<ResponseBase<PackCategoryResponse>> getPackCategoryById(@PathVariable Integer id) {
        try {
            PackCategoryResponse category = packCategoryService.getPackCategoryById(id);
            return ResponseBase.ok(category, "Pack category found");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, "Pack category not found with id: " + id);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update pack category", description = "Update an existing pack category")
    public ResponseEntity<ResponseBase<PackCategoryResponse>> updatePackCategory(@PathVariable Integer id,
                                                                                 @RequestBody PackCategoryRequestDTO requestDTO) {
        try {
            PackCategoryResponse updated = packCategoryService.updatePackCategory(id, requestDTO);
            return ResponseBase.ok(updated, "Pack category updated successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pack category", description = "Delete a pack category by ID")
    public ResponseEntity<ResponseBase<Void>> deletePackCategory(@PathVariable Integer id) {
        try {
            packCategoryService.deletePackCategory(id);
            return ResponseBase.ok(null, "Pack category deleted successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle pack category active status", description = "Toggle pack category active status")
    public ResponseEntity<ResponseBase<PackCategoryResponse>> toggleActive(@PathVariable Integer id) {
        try {
            PackCategoryResponse updated = packCategoryService.toggleActive(id);
            return ResponseBase.ok(updated, "Pack category active status toggled successfully");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
