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

import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Item Management", description = "APIs for managing order items")
@SecurityRequirement(name = "bearerAuth")
public class OrderItemController {

        private final OrderItemService orderItemService;

        @PostMapping
        @Operation(summary = "Create a new order item", description = "Create a new order item")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Order item created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<OrderItem>> createOrderItem(
                        @RequestBody OrderItemRequestDTO orderItemRequestDTO) {
                try {
                        OrderItem createdOrderItem = orderItemService.createOrderItem(orderItemRequestDTO);
                        return ResponseBase.created(createdOrderItem, "Order item created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create order item: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all order items", description = "Retrieve all order items")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order items retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<OrderItem>>> getAllOrderItems() {
                List<OrderItem> orderItems = orderItemService.getAllOrderItems();
                return ResponseBase.ok(orderItems, "Order items retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get order item by ID", description = "Retrieve an order item by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order item found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Order item not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<OrderItem>> getOrderItemById(@PathVariable Integer id) {
                return orderItemService.getOrderItemById(id)
                                .map(orderItem -> ResponseBase.ok(orderItem, "Order item found"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Order item not found with id: " + id));
        }

        @GetMapping("/order/{orderId}")
        @Operation(summary = "Get order items by order ID", description = "Retrieve all items for a specific order")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order items found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<OrderItem>>> getOrderItemsByOrderId(@PathVariable Integer orderId) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
                return ResponseBase.ok(orderItems, "Order items retrieved successfully");
        }

        @GetMapping("/pack/{packId}")
        @Operation(summary = "Get order items by pack ID", description = "Retrieve all order items for a specific pack")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order items found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<OrderItem>>> getOrderItemsByPackId(@PathVariable Integer packId) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByPackId(packId);
                return ResponseBase.ok(orderItems, "Order items retrieved successfully");
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update order item", description = "Update an existing order item")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order item updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Order item not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<OrderItem>> updateOrderItem(@PathVariable Integer id,
                        @RequestBody OrderItemRequestDTO orderItemRequestDTO) {
                try {
                        OrderItem updatedOrderItem = orderItemService.updateOrderItem(id, orderItemRequestDTO);
                        return ResponseBase.ok(updatedOrderItem, "Order item updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete order item", description = "Delete an order item by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order item deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Order item not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteOrderItem(@PathVariable Integer id) {
                try {
                        orderItemService.deleteOrderItem(id);
                        return ResponseBase.ok(null, "Order item deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }
}
