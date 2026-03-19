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

import com.example.PixelMageEcomerceProject.dto.request.InventoryRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Inventory;
import com.example.PixelMageEcomerceProject.service.interfaces.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing inventory")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

        private final InventoryService inventoryService;

        @PostMapping("/{productId}")
        @Operation(summary = "Create inventory record", description = "Create a new inventory record")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Inventory created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Inventory>> createInventory(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Inventory details to create", required = true, content = @Content(schema = @Schema(implementation = InventoryRequestDTO.class))) @RequestBody InventoryRequestDTO inventoryRequestDTO,
                        @PathVariable Integer productId) {
                try {
                        Inventory createdInventory = inventoryService.createInventory(inventoryRequestDTO, productId);
                        return ResponseBase.created(createdInventory, "Inventory created successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create inventory: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all inventory", description = "Retrieve all inventory records")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<Inventory>>> getAllInventory() {
                List<Inventory> inventories = inventoryService.getAllInventories();
                return ResponseBase.ok(inventories, "Inventory retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get inventory by ID", description = "Retrieve an inventory record by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Inventory not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Inventory>> getInventoryById(@PathVariable Integer id) {
                return inventoryService.getInventoryById(id)
                                .map(inventory -> ResponseBase.ok(inventory, "Inventory retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Inventory not found with id: " + id));
        }



        @PutMapping("/{id}")
        @Operation(summary = "Update inventory", description = "Update an existing inventory record")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Inventory updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Inventory not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Inventory>> updateInventory(
                        @PathVariable Integer id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Inventory details to update", required = true, content = @Content(schema = @Schema(implementation = InventoryRequestDTO.class))) @RequestBody InventoryRequestDTO inventoryRequestDTO) {
                try {
                        Inventory updatedInventory = inventoryService.updateInventory(id, inventoryRequestDTO);
                        return ResponseBase.ok(updatedInventory, "Inventory updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to update inventory: " + e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete inventory", description = "Delete an inventory record by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Inventory deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Inventory not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteInventory(@PathVariable Integer id) {
                try {
                        inventoryService.deleteInventory(id);
                        return ResponseBase.ok(null, "Inventory deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to delete inventory: " + e.getMessage());
                }
        }
}
