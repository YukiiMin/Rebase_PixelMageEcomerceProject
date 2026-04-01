package com.example.PixelMageEcomerceProject.controller;

import com.example.PixelMageEcomerceProject.service.interfaces.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
@Tag(name = "Admin Upload", description = "Endpoints for uploading assets to Cloudinary")
public class AdminUploadController {

    private final CloudinaryService cloudinaryService;

    // TODO: update entities logic (CardTemplate, Product, Account) 
    // Usually here we upload image and then we save the returned URL to DB.
    // For now, I will just provide simple upload endpoints returning the URL, 
    // or you can pass ID and we save it. Let's just return the URL, and FE will submit it.

    @PostMapping("/image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Upload image to Cloudinary", description = "Uploads an image to the specified folder and returns the CDN URL")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        try {
            String url = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }
}
