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

import com.example.PixelMageEcomerceProject.dto.request.SupplierRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Supplier;
import com.example.PixelMageEcomerceProject.service.interfaces.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier Management", description = "APIs for managing suppliers")
@SecurityRequirement(name = "bearerAuth")
public class SupplierController {

        private final SupplierService supplierService;

        @PostMapping
        @Operation(summary = "Create a new supplier", description = "Create a new supplier with name, contact person, email, phone and address")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Supplier created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Email already exists", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Supplier>> createSupplier(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Supplier details to create", required = true, content = @Content(schema = @Schema(implementation = SupplierRequestDTO.class))) @RequestBody SupplierRequestDTO supplierRequestDTO) {
                try {
                        Supplier createdSupplier = supplierService.createSupplier(supplierRequestDTO);
                        return ResponseBase.created(createdSupplier, "Supplier created successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create supplier: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all suppliers", description = "Retrieve a list of all suppliers in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<Supplier>>> getAllSuppliers() {
                List<Supplier> suppliers = supplierService.getAllSuppliers();
                return ResponseBase.ok(suppliers, "Suppliers retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get supplier by ID", description = "Retrieve supplier details by supplier ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Supplier retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Supplier>> getSupplierById(
                        @Parameter(description = "Supplier ID", required = true) @PathVariable Integer id) {
                return supplierService.getSupplierById(id)
                                .map(supplier -> ResponseBase.ok(supplier, "Supplier retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Supplier not found with id: " + id));
        }

        @GetMapping("/email/{email}")
        @Operation(summary = "Get supplier by email", description = "Retrieve supplier details by email address")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Supplier retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Supplier>> getSupplierByEmail(
                        @Parameter(description = "Email address", required = true) @PathVariable String email) {
                return supplierService.getSupplierByEmail(email)
                                .map(supplier -> ResponseBase.ok(supplier, "Supplier retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Supplier not found with email: " + email));
        }

        @GetMapping("/name/{name}")
        @Operation(summary = "Get supplier by name", description = "Retrieve supplier details by name")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Supplier retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Supplier>> getSupplierByName(
                        @Parameter(description = "Supplier name", required = true) @PathVariable String name) {
                return supplierService.getSupplierByName(name)
                                .map(supplier -> ResponseBase.ok(supplier, "Supplier retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Supplier not found with name: " + name));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update supplier", description = "Update existing supplier information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Supplier updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or email already exists", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Supplier>> updateSupplier(
                        @Parameter(description = "Supplier ID", required = true) @PathVariable Integer id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated supplier details", required = true, content = @Content(schema = @Schema(implementation = SupplierRequestDTO.class))) @RequestBody SupplierRequestDTO supplierRequestDTO) {
                try {
                        Supplier updatedSupplier = supplierService.updateSupplier(id, supplierRequestDTO);
                        return ResponseBase.ok(updatedSupplier, "Supplier updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to update supplier: " + e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete supplier", description = "Delete a supplier by supplier ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Supplier deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteSupplier(
                        @Parameter(description = "Supplier ID", required = true) @PathVariable Integer id) {
                try {
                        supplierService.deleteSupplier(id);
                        return ResponseBase.success("Supplier deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Failed to delete supplier: " + e.getMessage());
                }
        }

        @GetMapping("/exists/{email}")
        @Operation(summary = "Check if email exists", description = "Check if an email address is already registered in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email check completed", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Boolean>> checkEmailExists(
                        @Parameter(description = "Email address to check", required = true) @PathVariable String email) {
                boolean exists = supplierService.existsByEmail(email);
                return ResponseBase.ok(exists, "Email check completed");
        }
}
