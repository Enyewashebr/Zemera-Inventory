package com.zemera.inventory.model;

import java.time.LocalDateTime;
import java.util.List;

public class Stock {

    private Long id;
    private Integer productId;
    private String productName;
    private String category;
    private String subCategory;
    private Integer branchId;
    private Double quantity;
    private LocalDateTime lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getProductName() {
        return productName;
    }
     public void setCategory(String category) {
        this.category = category;
    }
    public String getCategory() {
        return category;
    }
     public void setSubcategory(String subCategory) {
        this.subCategory = subCategory;
    }
    public String getSubCategory() {
        return subCategory;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}



