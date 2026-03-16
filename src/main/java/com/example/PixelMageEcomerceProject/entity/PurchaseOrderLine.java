package com.example.PixelMageEcomerceProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
@Entity
@Table(name = "purchase_order_lines")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String poId;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonBackReference("purchaseOrder-purchaseOrderLines")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "product_id")
    @JsonBackReference("product-purchaseOrderLines")
    private Product product;

    @Column(name = "Quantity_Ordered", nullable = false)
    private int quantityOrdered;
    @Column(name = "Quantity_Received", nullable = false)
    private int quantityReceived;
    @Column(name = "Quantity_Pending_Received", nullable = false)
    private int quantityPendingReceived;
    @Column(name = "Unit_Price", nullable = false)
    private double unitPrice;
    @Column(name = "Total_Price", nullable = false)
    private double totalPrice;

    @CreationTimestamp
    @Column(name = "Expected_Date", nullable = false)
    private LocalDate expectedDate;

    @Column(name = "Note")
    private String note;

}
