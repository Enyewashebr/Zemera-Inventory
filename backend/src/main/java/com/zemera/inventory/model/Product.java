package com.zemera.inventory.model;

import java.time.LocalDateTime;

public class Product {
    private Integer id;
    private String name;
    private String categoryId;
    private String subcategory;
    private String unit;
    private Double buyingPrice;
    private Double sellingPrice;
    private Double stock;
    private Boolean sellable;
    private LocalDateTime createdAt;

    // No-args constructor
    public Product() {}

    // Full constructor
    public Product(Integer id, String name, String categoryId, String subcategory,
                   String unit, Double buyingPrice, Double sellingPrice, Double stock,
                   Boolean sellable, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.subcategory = subcategory;
        this.unit = unit;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.stock = stock;
        this.sellable = sellable;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getBuyingPrice() { return buyingPrice; }
    public void setBuyingPrice(Double buyingPrice) { this.buyingPrice = buyingPrice; }

    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }

    public Double getStock() { return stock; }
    public void setStock(Double stock) { this.stock = stock; }

    public Boolean getSellable() { return sellable; }
    public void setSellable(Boolean sellable) { this.sellable = sellable; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

