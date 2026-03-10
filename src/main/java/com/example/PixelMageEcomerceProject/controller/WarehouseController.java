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

import com.example.PixelMageEcomerceProject.dto.request.WarehouseRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Warehouse;
import com.example.PixelMageEcomerceProject.service.interfaces.WarehouseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "APIs for managing warehouses")
@SecurityRequirement(name = "bearerAuth")
public class WarehouseController {

        private final WarehouseService warehouseService;

        @PostMapping
        @Operation(summary = "Create a new warehouse", description = "Create a new warehouse")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Warehouse created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Warehouse>> createWarehouse(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Warehouse details to create", required = true, content = @Content(schema = @Schema(implementation = WarehouseRequestDTO.class))) @RequestBody WarehouseRequestDTO warehouseRequestDTO) {
                try {
                        Warehouse createdWarehouse = warehouseService.createWarehouse(warehouseRequestDTO);
                        return ResponseBase.created(createdWarehouse, "Warehouse created successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create warehouse: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all warehouses", description = "Retrieve all warehouses")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved warehouses", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<Warehouse>>> getAllWarehouses() {
                List<Warehouse> warehouses = warehouseService.getAllWarehouses();
                return ResponseBase.ok(warehouses, "Warehouses retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get warehouse by ID", description = "Retrieve a warehouse by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved warehouse", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Warehouse not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Warehouse>> getWarehouseById(@PathVariable Integer id) {
                return warehouseService.getWarehouseById(id)
                                .map(warehouse -> ResponseBase.ok(warehouse, "Warehouse retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Warehouse not found with id: " + id));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update warehouse", description = "Update an existing warehouse")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Warehouse updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Warehouse not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Warehouse>> updateWarehouse(
                        @PathVariable Integer id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Warehouse details to update", required = true, content = @Content(schema = @Schema(implementation = WarehouseRequestDTO.class))) @RequestBody WarehouseRequestDTO warehouseRequestDTO) {
                try {
                        Warehouse updatedWarehouse = warehouseService.updateWarehouse(id, warehouseRequestDTO);
                        return ResponseBase.ok(updatedWarehouse, "Warehouse updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to update warehouse: " + e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete warehouse", description = "Delete a warehouse by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Warehouse deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Warehouse not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteWarehouse(@PathVariable Integer id) {
                try {
                        warehouseService.deleteWarehouse(id);
                        return ResponseBase.success("Warehouse deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to delete warehouse: " + e.getMessage());
                }
        }
}
