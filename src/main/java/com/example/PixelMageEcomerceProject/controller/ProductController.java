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
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Product;
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
        public ResponseEntity<ResponseBase<Product>> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
                try {
                        Product createdProduct = productService.createProduct(productRequestDTO);
                        return ResponseBase.created(createdProduct, "Product created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create product: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all products", description = "Retrieve all products")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<Product>>> getAllProducts() {
                List<Product> products = productService.getAllProducts();
                return ResponseBase.ok(products, "Products retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Product>> getProductById(@PathVariable Integer id) {
                return productService.getProductById(id)
                                .map(product -> ResponseBase.ok(product, "Product found"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Product not found with id: " + id));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update product", description = "Update an existing product")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Product>> updateProduct(@PathVariable Integer id,
                        @RequestBody ProductRequestDTO productRequestDTO) {
                try {
                        Product updatedProduct = productService.updateProduct(id, productRequestDTO);
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
}
