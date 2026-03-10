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

import com.example.PixelMageEcomerceProject.dto.request.PurchaseOrderLineRequest;
import com.example.PixelMageEcomerceProject.dto.request.PurchaseOrderRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.PurchaseOrder;
import com.example.PixelMageEcomerceProject.service.interfaces.PurchaseOrderLineService;
import com.example.PixelMageEcomerceProject.service.interfaces.PurchaseOrderService;

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
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Order Management", description = "APIs for managing purchase orders")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseOrderController {

        private final PurchaseOrderService purchaseOrderService;

        private final PurchaseOrderLineService purchaseOrderLineService;

        @PostMapping
        @Operation(summary = "Create a new purchase order", description = "Create a new purchase order with warehouse, supplier, PO number, status, order date and expected delivery")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Purchase order created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - PO number already exists or supplier not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PurchaseOrder>> createPurchaseOrder(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Purchase order details to create", required = true, content = @Content(schema = @Schema(implementation = PurchaseOrderRequestDTO.class))) @RequestBody PurchaseOrderRequestDTO purchaseOrderRequestDTO) {
                try {
                        PurchaseOrder createdPO = purchaseOrderService.createPurchaseOrder(purchaseOrderRequestDTO);
                        return ResponseBase.created(createdPO, "Purchase order created successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create purchase order: " + e.getMessage());
                }
        }

        @GetMapping
        @Operation(summary = "Get all purchase orders", description = "Retrieve a list of all purchase orders in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<PurchaseOrder>>> getAllPurchaseOrders() {
                List<PurchaseOrder> purchaseOrders = purchaseOrderService.getAllPurchaseOrders();
                return ResponseBase.ok(purchaseOrders, "Purchase orders retrieved successfully");
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get purchase order by ID", description = "Retrieve purchase order details by PO ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Purchase order retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Purchase order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PurchaseOrder>> getPurchaseOrderById(
                        @Parameter(description = "Purchase Order ID", required = true) @PathVariable Integer id) {
                return purchaseOrderService.getPurchaseOrderById(id)
                                .map(po -> ResponseBase.ok(po, "Purchase order retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Purchase order not found with ID: " + id));
        }

        @GetMapping("/po-number/{poNumber}")
        @Operation(summary = "Get purchase order by PO number", description = "Retrieve purchase order details by PO number")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Purchase order retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Purchase order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PurchaseOrder>> getPurchaseOrderByPoNumber(
                        @Parameter(description = "PO Number", required = true) @PathVariable String poNumber) {
                return purchaseOrderService.getPurchaseOrderByPoNumber(poNumber)
                                .map(po -> ResponseBase.ok(po, "Purchase order retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Purchase order not found with PO number: " + poNumber));
        }

        @GetMapping("/status/{status}")
        @Operation(summary = "Get purchase orders by status", description = "Retrieve purchase orders by status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<PurchaseOrder>>> getPurchaseOrdersByStatus(
                        @Parameter(description = "Status", required = true) @PathVariable String status) {
                List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrdersByStatus(status);
                return ResponseBase.ok(purchaseOrders, "Purchase orders retrieved successfully");
        }

        @GetMapping("/supplier/{supplierId}")
        @Operation(summary = "Get purchase orders by supplier ID", description = "Retrieve purchase orders by supplier ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<PurchaseOrder>>> getPurchaseOrdersBySupplierId(
                        @Parameter(description = "Supplier ID", required = true) @PathVariable Integer supplierId) {
                List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrdersBySupplierId(supplierId);
                return ResponseBase.ok(purchaseOrders, "Purchase orders retrieved successfully");
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update purchase order", description = "Update existing purchase order information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Purchase order updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or PO number already exists", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Purchase order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PurchaseOrder>> updatePurchaseOrder(
                        @Parameter(description = "Purchase Order ID", required = true) @PathVariable Integer id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated purchase order details", required = true, content = @Content(schema = @Schema(implementation = PurchaseOrderRequestDTO.class))) @RequestBody PurchaseOrderRequestDTO purchaseOrderRequestDTO) {
                try {
                        PurchaseOrder updatedPO = purchaseOrderService.updatePurchaseOrder(id, purchaseOrderRequestDTO);
                        return ResponseBase.ok(updatedPO, "Purchase order updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to update purchase order: " + e.getMessage());
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete purchase order", description = "Delete a purchase order by PO ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Purchase order deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Purchase order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deletePurchaseOrder(
                        @Parameter(description = "Purchase Order ID", required = true) @PathVariable Integer id) {
                try {
                        purchaseOrderService.deletePurchaseOrder(id);
                        return ResponseBase.success("Purchase order deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND,
                                        "Failed to delete purchase order: " + e.getMessage());
                }
        }

        @GetMapping("/exists/{poNumber}")
        @Operation(summary = "Check if PO number exists", description = "Check if a PO number is already registered in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PO number check completed", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Boolean>> checkPoNumberExists(
                        @Parameter(description = "PO number to check", required = true) @PathVariable String poNumber) {
                boolean exists = purchaseOrderService.existsByPoNumber(poNumber);
                return ResponseBase.ok(exists, "PO number check completed");
        }

        @PostMapping("/{poId}/lines")
        @Operation(summary = "Add purchase order line to a purchase order", description = "Add a new purchase order line to an existing purchase order")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Purchase order line added successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Invalid data", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Purchase order not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Object>> addPurchaseOrderLine(@PathVariable int poId,
                        @RequestBody PurchaseOrderLineRequest request) {
                try {
                        var line = purchaseOrderLineService.createPurchaseOrderLine(request, poId);
                        return ResponseBase.created(line, "Purchase order line added successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to add purchase order line: " + e.getMessage());
                }
        }

        @PutMapping("/{poId}/lines/{lineId}")
        @Operation(summary = "Update purchase order line", description = "Update an existing purchase order line")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Purchase order line updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Invalid data", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Purchase order or line not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PurchaseOrder>> updatePurchaseOrderLine(@PathVariable int poId,
                        @PathVariable String lineId) {
                try {
                        PurchaseOrder updateStatusPo = purchaseOrderService.receivedPurchaseOrder(poId, lineId);
                        return ResponseBase.ok(updateStatusPo, "Received full product successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Received not enough product: " + e.getMessage());
                }
        }
}
