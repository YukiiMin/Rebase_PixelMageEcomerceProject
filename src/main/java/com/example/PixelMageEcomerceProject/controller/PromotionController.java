package com.example.PixelMageEcomerceProject.controller;

import com.example.PixelMageEcomerceProject.dto.request.PromotionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PromotionResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.service.interfaces.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion Management", description = "CRUD for Promotions (Admin/Staff)")
@SecurityRequirement(name = "bearerAuth")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Get all promotions (Admin/Staff)")
    public ResponseEntity<ResponseBase<List<PromotionResponse>>> getAllPromotions() {
        return ResponseBase.ok(promotionService.getAllPromotions(), "Promotions retrieved successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Get promotion by ID")
    public ResponseEntity<ResponseBase<PromotionResponse>> getPromotionById(@PathVariable int id) {
        try {
            return ResponseBase.ok(promotionService.getPromotionById(id), "Promotion found");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Create a new promotion")
    public ResponseEntity<ResponseBase<PromotionResponse>> createPromotion(@RequestBody PromotionRequestDTO request) {
        try {
            return ResponseBase.created(promotionService.createPromotion(request), "Promotion created");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Update a promotion")
    public ResponseEntity<ResponseBase<PromotionResponse>> updatePromotion(@PathVariable int id, @RequestBody PromotionRequestDTO request) {
        try {
            return ResponseBase.ok(promotionService.updatePromotion(id, request), "Promotion updated");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a promotion (Admin only)")
    public ResponseEntity<ResponseBase<Void>> deletePromotion(@PathVariable int id) {
        try {
            promotionService.deletePromotion(id);
            return ResponseBase.ok(null, "Promotion deleted");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{promotionId}/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Assign a promotion to an order")
    public ResponseEntity<ResponseBase<PromotionResponse>> setOrderPromotion(@PathVariable int orderId, @PathVariable int promotionId) {
        try {
            return ResponseBase.ok(promotionService.setOrderPromotion(orderId, promotionId), "Promotion assigned to order");
        } catch (RuntimeException e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
