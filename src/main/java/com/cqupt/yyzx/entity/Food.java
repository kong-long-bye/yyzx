package com.cqupt.yyzx.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 食品实体类
 */
public class Food {
    private Integer id;
    private String foodName;
    private String foodCategory;
    private String unit;
    private BigDecimal price; // 新增价格字段
    private BigDecimal caloriesPerUnit;
    private BigDecimal proteinContent;
    private BigDecimal fatContent;
    private BigDecimal carbContent;
    private String allergenInfo;
    private String storageRequirements;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Food() {}

    public Food(String foodName, String foodCategory) {
        this.foodName = foodName;
        this.foodCategory = foodCategory;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(String foodCategory) {
        this.foodCategory = foodCategory;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCaloriesPerUnit() {
        return caloriesPerUnit;
    }

    public void setCaloriesPerUnit(BigDecimal caloriesPerUnit) {
        this.caloriesPerUnit = caloriesPerUnit;
    }

    public BigDecimal getProteinContent() {
        return proteinContent;
    }

    public void setProteinContent(BigDecimal proteinContent) {
        this.proteinContent = proteinContent;
    }

    public BigDecimal getFatContent() {
        return fatContent;
    }

    public void setFatContent(BigDecimal fatContent) {
        this.fatContent = fatContent;
    }

    public BigDecimal getCarbContent() {
        return carbContent;
    }

    public void setCarbContent(BigDecimal carbContent) {
        this.carbContent = carbContent;
    }

    public String getAllergenInfo() {
        return allergenInfo;
    }

    public void setAllergenInfo(String allergenInfo) {
        this.allergenInfo = allergenInfo;
    }

    public String getStorageRequirements() {
        return storageRequirements;
    }

    public void setStorageRequirements(String storageRequirements) {
        this.storageRequirements = storageRequirements;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Food{" +
                "id=" + id +
                ", foodName='" + foodName + '\'' +
                ", foodCategory='" + foodCategory + '\'' +
                ", unit='" + unit + '\'' +
                ", price=" + price +
                ", caloriesPerUnit=" + caloriesPerUnit +
                ", proteinContent=" + proteinContent +
                ", fatContent=" + fatContent +
                ", carbContent=" + carbContent +
                ", allergenInfo='" + allergenInfo + '\'' +
                ", status=" + status +
                '}';
    }
}