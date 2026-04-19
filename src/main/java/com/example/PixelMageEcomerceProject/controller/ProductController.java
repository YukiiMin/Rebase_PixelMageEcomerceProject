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

import com.example.PixelMageEcomerceProject.dto.request.ProductRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ProductResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.service.interfaces.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

        private final ProductService productService;

        @PostMapping
        @Operation(summary = "Create a new product", description = "Create a new product")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<ProductResponse>> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
                try {
                        ProductResponse createdProduct = productService.createProduct(productRequestDTO);
                        return ResponseBase.created(createdProduct, "Product created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create product: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all products (Admin)", description = "Retrieve all products including hidden ones — Admin only")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<ProductResponse>>> getAllProducts() {
                List<ProductResponse> products = productService.getAllProducts();
                return ResponseBase.ok(products, "Products retrieved successfully");
        }

        @GetMapping("/public")
        @Operation(summary = "Get public products", description = "Retrieve only visible and active products for customers — no auth required")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Public products retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<ProductResponse>>> getPublicProducts() {
                List<ProductResponse> products = productService.getPublicProducts();
                return ResponseBase.ok(products, "Public products retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<ProductResponse>> getProductById(@PathVariable Integer id) {
                try {
                        ProductResponse product = productService.getProductById(id);
                        return ResponseBase.ok(product, "Product found");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
                }
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update product", description = "Update an existing product")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<ProductResponse>> updateProduct(@PathVariable Integer id,
                        @RequestBody ProductRequestDTO productRequestDTO) {
                try {
                        ProductResponse updatedProduct = productService.updateProduct(id, productRequestDTO);
                        return ResponseBase.ok(updatedProduct, "Product updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete product", description = "Delete a product by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteProduct(@PathVariable Integer id) {
                try {
                        productService.deleteProduct(id);
                        return ResponseBase.ok(null, "Product deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @PutMapping("/{id}/toggle-visibility")
        @Operation(summary = "Toggle product visibility", description = "Toggle product visibility")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product visibility toggled successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<ProductResponse>> toggleVisibility(@PathVariable Integer id) {
                try {
                        ProductResponse updatedProduct = productService.toggleVisibility(id);
                        return ResponseBase.ok(updatedProduct, "Product visibility toggled successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @PutMapping("/{id}/toggle-active")
        @Operation(summary = "Toggle product active status", description = "Toggle product active status (Soft delete / Restore)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product active status toggled successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<ProductResponse>> toggleActive(@PathVariable Integer id) {
                try {
                        ProductResponse updatedProduct = productService.toggleActive(id);
                        return ResponseBase.ok(updatedProduct, "Product active status toggled successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }
}
