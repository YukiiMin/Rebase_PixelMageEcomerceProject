package com.example.PixelMageEcomerceProject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.example.PixelMageEcomerceProject.dto.request.SetStoryRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.SetStory;
import com.example.PixelMageEcomerceProject.service.interfaces.SetStoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Set Story Management", description = "APIs for managing and viewing Set Stories")
@SecurityRequirement(name = "bearerAuth")
public class SetStoryController {

        private final SetStoryService setStoryService;

        @GetMapping("/stories")
        @Operation(summary = "Get all stories with lock status", description = "Return list of all stories and whether each one is unlocked for the given user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Stories retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<Map<String, Object>>>> getStories(@RequestParam Integer userId) {
                List<SetStory> allStories = setStoryService.getAllStories();
                List<SetStory> unlocked = setStoryService.getUnlockedStories(userId);

                Map<Integer, Boolean> unlockedMap = new HashMap<>();
                for (SetStory story : unlocked) {
                        unlockedMap.put(story.getStoryId(), true);
                }

                List<Map<String, Object>> result = allStories.stream().map(story -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("storyId", story.getStoryId());
                        dto.put("title", story.getTitle());
                        dto.put("coverImagePath", story.getCoverImagePath());
                        dto.put("isActive", story.getIsActive());
                        dto.put("unlocked", unlockedMap.getOrDefault(story.getStoryId(), false));
                        return dto;
                }).toList();

                return ResponseBase.ok(result, "Stories retrieved successfully");
        }

        @GetMapping("/stories/{id}")
        @Operation(summary = "Get story detail", description = "Return story content only if the story has been unlocked for the given user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Story retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "403", description = "Story is locked", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Story not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<SetStory>> getStoryDetail(@PathVariable Integer id,
                        @RequestParam Integer userId) {
                SetStory story;
                try {
                        story = setStoryService.getStoryById(id);
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Story not found with id: " + id);
                }

                boolean unlocked = setStoryService.getUnlockedStories(userId).stream()
                                .anyMatch(s -> s.getStoryId().equals(id));

                if (!unlocked) {
                        return ResponseBase.error(HttpStatus.FORBIDDEN, "Story is locked for this user");
                }

                return ResponseBase.ok(story, "Story retrieved successfully");
        }

        @PostMapping("/admin/stories")
        @Operation(summary = "Create a new Set Story", description = "Admin API to create a Set Story definition")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Story created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<SetStory>> createStory(@RequestBody SetStoryRequestDTO request) {
                try {
                        SetStory story = new SetStory();
                        story.setTitle(request.getTitle());
                        story.setContent(request.getContent());
                        story.setRequiredTemplateIds(request.getRequiredTemplateIds());
                        story.setCoverImagePath(request.getCoverImagePath());
                        story.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);

                        SetStory saved = setStoryService.createStory(story);
                        return ResponseBase.created(saved, "Story created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to create story: " + e.getMessage());
                }
        }

        @PutMapping("/admin/stories/{id}")
        @Operation(summary = "Update Set Story", description = "Admin API to update Set Story definition")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Story updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Story not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<SetStory>> updateStory(@PathVariable Integer id,
                        @RequestBody SetStoryRequestDTO request) {
                try {
                        SetStory existing = setStoryService.getStoryById(id);
                        existing.setTitle(request.getTitle() != null ? request.getTitle() : existing.getTitle());
                        existing.setContent(
                                        request.getContent() != null ? request.getContent() : existing.getContent());
                        existing.setRequiredTemplateIds(request.getRequiredTemplateIds() != null
                                        ? request.getRequiredTemplateIds()
                                        : existing.getRequiredTemplateIds());
                        existing.setCoverImagePath(request.getCoverImagePath() != null
                                        ? request.getCoverImagePath()
                                        : existing.getCoverImagePath());
                        if (request.getIsActive() != null) {
                                existing.setIsActive(request.getIsActive());
                        }

                        SetStory saved = setStoryService.updateStory(existing);
                        return ResponseBase.ok(saved, "Story updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Failed to update story: " + e.getMessage());
                }
        }

        @DeleteMapping("/admin/stories/{id}")
        @Operation(summary = "Delete Set Story", description = "Admin API to delete Set Story")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Story deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Story not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteStory(@PathVariable Integer id) {
                try {
                        setStoryService.deleteStory(id);
                        return ResponseBase.success("Story deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Failed to delete story: " + e.getMessage());
                }
        }
}
