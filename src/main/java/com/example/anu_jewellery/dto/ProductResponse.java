package com.example.anu_jewellery.dto;

import java.math.BigDecimal;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String metalType;
    private String gender;
    private String stoneType;
    private BigDecimal stoneWeight;
    private BigDecimal weight;
    private String occasion;
    private String imageUrl;

    public ProductResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMetalType() { return metalType; }
    public void setMetalType(String metalType) { this.metalType = metalType; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getStoneType() { return stoneType; }
    public void setStoneType(String stoneType) { this.stoneType = stoneType; }

    public BigDecimal getStoneWeight() { return stoneWeight; }
    public void setStoneWeight(BigDecimal stoneWeight) { this.stoneWeight = stoneWeight; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getOccasion() { return occasion; }
    public void setOccasion(String occasion) { this.occasion = occasion; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}