package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.PackRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.dto.response.PackResponse;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.service.interfaces.PackService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/packs")
@RequiredArgsConstructor
@Tag(name = "Pack Management", description = "APIs for managing Packs (Box/Sleeve) and RNG logic")
@SecurityRequirement(name = "bearerAuth")
public class PackController {

    private final PackService packService;

    @PostMapping("/create")
    @Operation(summary = "Create a pack (Manufacturing)", description = "Run RNG logic to pick card templates inside a pack product")
    public ResponseEntity<ResponseBase<PackResponse>> createPack(@RequestBody PackRequestDTO requestDTO) {
        try {
            PackResponse pack = packService.createPack(requestDTO);
            return ResponseBase.created(pack, "Pack generated successfully via RNG");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update pack status", description = "Update the status (e.g., STOCKED, RESERVED, SOLD)")
    public ResponseEntity<ResponseBase<PackResponse>> updatePackStatus(@PathVariable Integer id,
            @RequestParam PackStatus status) {
        try {
            PackResponse pack = packService.updatePackStatus(id, status);
            return ResponseBase.ok(pack, "Pack status updated");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Get available packs", description = "Retrieve packs that are STOCKED")
    public ResponseEntity<ResponseBase<List<PackResponse>>> getAvailablePacks() {
        List<PackResponse> packs = packService.getPacksByStatus(PackStatus.STOCKED);
        return ResponseBase.ok(packs, "Available packs retrieved");
    }

    @GetMapping
    @Operation(summary = "Get all packs", description = "Retrieve all packs")
    public ResponseEntity<ResponseBase<List<PackResponse>>> getAllPacks() {
        List<PackResponse> packs = packService.getAllPacks();
        return ResponseBase.ok(packs, "Packs retrieved");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pack by ID", description = "Retrieve a specific pack detail including its cards")
    public ResponseEntity<ResponseBase<PackResponse>> getPackById(@PathVariable Integer id) {
        return packService.getPackById(id)
                .map(pack -> ResponseBase.ok(pack, "Pack found"))
                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND, "Pack not found"));
    }
}
