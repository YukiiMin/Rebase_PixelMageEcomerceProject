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

import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

        private final OrderService orderService;

        @PostMapping
        @Operation(summary = "Create a new order", description = "Create a new order")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<OrderResponse>> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
                try {
                        OrderResponse createdOrder = orderService.createOrder(orderRequestDTO);
                        return ResponseBase.created(createdOrder, "Order created successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to create order: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all orders", description = "Retrieve all orders")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<OrderResponse>>> getAllOrders() {
                List<OrderResponse> orders = orderService.getAllOrders();
                return ResponseBase.ok(orders, "Orders retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get order by ID", description = "Retrieve an order by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<OrderResponse>> getOrderById(@PathVariable Integer id) {
                OrderResponse order = orderService.getOrderById(id);
                if (order != null) {
                    return ResponseBase.ok(order, "Order found");
                }
                return ResponseBase.error(HttpStatus.NOT_FOUND, "Order not found with id: " + id);
        }

        @GetMapping("/customer/{customerId}")
        @Operation(summary = "Get orders by customer ID", description = "Retrieve all orders for a specific customer")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Orders found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<OrderResponse>>> getOrdersByCustomerId(@PathVariable Integer customerId) {
                List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
                return ResponseBase.ok(orders, "Orders retrieved successfully");
        }

        @GetMapping("/status/{status}")
        @Operation(summary = "Get orders by status", description = "Retrieve all orders with a specific status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Orders found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
                List<OrderResponse> orders = orderService.getOrdersByStatus(status);
                return ResponseBase.ok(orders, "Orders retrieved successfully");
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update order", description = "Update an existing order")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<OrderResponse>> updateOrder(@PathVariable Integer id,
                        @RequestBody OrderRequestDTO orderRequestDTO) {
                try {
                        OrderResponse updatedOrder = orderService.updateOrder(id, orderRequestDTO);
                        return ResponseBase.ok(updatedOrder, "Order updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete order", description = "Delete an order by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteOrder(@PathVariable Integer id) {
                try {
                        orderService.deleteOrder(id);
                        return ResponseBase.ok(null, "Order deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, e.getMessage());
                }
        }

        @PutMapping("/{id}/cancel")
        @Operation(summary = "Cancel order", description = "Cancel an order and release reserved packs")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Order cancelled successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<OrderResponse>> cancelOrder(@PathVariable Integer id) {
                try {
                        OrderResponse cancelledOrder = orderService.cancelOrder(id);
                        return ResponseBase.ok(cancelledOrder, "Order cancelled successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Failed to cancel order: " + e.getMessage());
                }
        }
}
