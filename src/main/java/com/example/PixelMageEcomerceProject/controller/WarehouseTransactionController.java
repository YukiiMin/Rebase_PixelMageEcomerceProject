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

import com.example.PixelMageEcomerceProject.dto.request.WarehouseTransactionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.WarehouseTransaction;
import com.example.PixelMageEcomerceProject.service.interfaces.WarehouseTransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouse-transactions")
@RequiredArgsConstructor
@Tag(name = "Warehouse Transaction Management", description = "APIs for managing warehouse transactions and updating inventory")
@SecurityRequirement(name = "bearerAuth")
public class WarehouseTransactionController {

        private final WarehouseTransactionService transactionService;

        @PostMapping
        @Operation(summary = "Create warehouse transaction", description = "Create a new warehouse transaction and automatically update inventory")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Transaction created successfully and inventory updated", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<WarehouseTransaction>> createTransaction(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Transaction details to create", required = true, content = @Content(schema = @Schema(implementation = WarehouseTransactionRequestDTO.class))) @RequestBody WarehouseTransactionRequestDTO transactionRequestDTO) {
                try {
                        WarehouseTransaction createdTransaction = transactionService
                                        .createTransaction(transactionRequestDTO);
                        return ResponseBase.created(createdTransaction,
                                        "Transaction created successfully and inventory updated");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create transaction: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all transactions", description = "Retrieve all warehouse transactions")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<WarehouseTransaction>>> getAllTransactions() {
                List<WarehouseTransaction> transactions = transactionService.getAllTransactions();
                return ResponseBase.ok(transactions, "Transactions retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get transaction by ID", description = "Retrieve a warehouse transaction by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<WarehouseTransaction>> getTransactionById(@PathVariable Integer id) {
                return transactionService.getTransactionById(id)
                                .map(transaction -> ResponseBase.ok(transaction, "Transaction retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Transaction not found with id: " + id));
        }

        @GetMapping("/warehouse/{warehouseId}")
        @Operation(summary = "Get transactions by warehouse", description = "Retrieve all transactions for a specific warehouse")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<WarehouseTransaction>>> getTransactionsByWarehouseId(
                        @PathVariable Integer warehouseId) {
                List<WarehouseTransaction> transactions = transactionService.getTransactionsByWarehouseId(warehouseId);
                return ResponseBase.ok(transactions, "Transactions retrieved successfully");
        }

        @GetMapping("/product/{productId}")
        @Operation(summary = "Get transactions by product", description = "Retrieve all transactions for a specific product")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<WarehouseTransaction>>> getTransactionsByProductId(
                        @PathVariable Integer productId) {
                List<WarehouseTransaction> transactions = transactionService.getTransactionsByProductId(productId);
                return ResponseBase.ok(transactions, "Transactions retrieved successfully");
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update transaction", description = "Update an existing warehouse transaction")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transaction updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<WarehouseTransaction>> updateTransaction(
                        @PathVariable Integer id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Transaction details to update", required = true, content = @Content(schema = @Schema(implementation = WarehouseTransactionRequestDTO.class))) @RequestBody WarehouseTransactionRequestDTO transactionRequestDTO) {
                try {
                        WarehouseTransaction updatedTransaction = transactionService.updateTransaction(id,
                                        transactionRequestDTO);
                        return ResponseBase.ok(updatedTransaction, "Transaction updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to update transaction: " + e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete transaction", description = "Delete a warehouse transaction by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transaction deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteTransaction(@PathVariable Integer id) {
                try {
                        transactionService.deleteTransaction(id);
                        return ResponseBase.success("Transaction deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to delete transaction: " + e.getMessage());
                }
        }
}
