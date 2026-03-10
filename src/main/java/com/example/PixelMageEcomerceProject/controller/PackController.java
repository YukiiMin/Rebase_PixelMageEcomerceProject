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
import com.example.PixelMageEcomerceProject.entity.Pack;
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
    public ResponseEntity<ResponseBase> createPack(@RequestBody PackRequestDTO requestDTO) {
        try {
            Pack pack = packService.createPack(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseBase(HttpStatus.CREATED.value(), "Pack generated successfully via RNG", pack));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseBase(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update pack status", description = "Update the status (e.g., STOCKED, RESERVED, SOLD)")
    public ResponseEntity<ResponseBase> updatePackStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            Pack pack = packService.updatePackStatus(id, status);
            return ResponseEntity.ok(new ResponseBase(HttpStatus.OK.value(), "Pack status updated", pack));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseBase(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Get available packs", description = "Retrieve packs that are STOCKED")
    public ResponseEntity<ResponseBase> getAvailablePacks() {
        List<Pack> packs = packService.getPacksByStatus("STOCKED");
        return ResponseEntity.ok(new ResponseBase(HttpStatus.OK.value(), "Available packs retrieved", packs));
    }

    @GetMapping
    @Operation(summary = "Get all packs", description = "Retrieve all packs")
    public ResponseEntity<ResponseBase> getAllPacks() {
        List<Pack> packs = packService.getAllPacks();
        return ResponseEntity.ok(new ResponseBase(HttpStatus.OK.value(), "Packs retrieved", packs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pack by ID", description = "Retrieve a specific pack detail including its cards")
    public ResponseEntity<ResponseBase> getPackById(@PathVariable Integer id) {
        return packService.getPackById(id)
                .map(pack -> ResponseEntity.ok(new ResponseBase(HttpStatus.OK.value(), "Pack found", pack)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseBase(HttpStatus.NOT_FOUND.value(), "Pack not found", null)));
    }
}
