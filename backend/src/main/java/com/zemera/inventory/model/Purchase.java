package com.zemera.inventory.model;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class Purchase {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalCost;
    private LocalDate purchaseDate;
     private String productName;
    private String status;
    private Long approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer branchId;

    // No-args constructor
    public Purchase() {}

    // Full constructor
    public Purchase(Long id, Long productId, Integer quantity, Double unitPrice, Double totalCost,
                    LocalDate purchaseDate, String productName, String status, Long approvedBy,
                    LocalDateTime createdAt, LocalDateTime updatedAt, Integer branchId) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalCost = totalCost;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.approvedBy = approvedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.productName = productName;
        this.branchId = branchId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }


    public Integer getBranchId() {return branchId;}

    public void setBranchId(Integer branchId) {this.branchId = branchId;}

    public String getProductName() {
    return productName;
}

public void setProductName(String productName) {
    this.productName = productName;
}

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
