package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.entity.UserCollectionProgress;
import com.example.PixelMageEcomerceProject.service.interfaces.UserCollectionProgressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/collections/progress")
@RequiredArgsConstructor
@Tag(name = "Collection Progress", description = "APIs for tracking user collection completion")
public class UserCollectionProgressController {

    private final UserCollectionProgressService progressService;

    @GetMapping
    @Operation(summary = "Get user collection progress", description = "Retrieve completion status for all collections for a user")
    public ResponseEntity<List<UserCollectionProgress>> getUserProgress(@RequestParam Integer customerId) {
        return ResponseEntity.ok(progressService.getUserProgress(customerId));
    }

    @GetMapping("/detail")
    @Operation(summary = "Get specific collection progress", description = "Retrieve completion status for a specific collection")
    public ResponseEntity<UserCollectionProgress> getCollectionProgress(
            @RequestParam Integer customerId,
            @RequestParam Integer collectionId) {
        return progressService.getCollectionProgress(customerId, collectionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
