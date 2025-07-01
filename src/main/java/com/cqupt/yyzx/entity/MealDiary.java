package com.cqupt.yyzx.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 膳食日记实体类
 */
public class MealDiary {
    private Integer id;
    private Integer customerId;
    private LocalDate mealDate;
    private String mealType;
    private Integer foodId;
    private BigDecimal quantity;
    private String notes;
    private Integer caregiverId;
    private LocalDateTime createdAt;

    // 关联字段
    private String customerName;
    private String foodName;
    private String foodUnit;
    private BigDecimal foodPrice;
    private BigDecimal foodCalories;
    private BigDecimal foodProtein;
    private BigDecimal foodFat;
    private BigDecimal foodCarbs;
    private String caregiverName;

    // 计算字段
    private BigDecimal totalPrice;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbs;

    // 构造函数
    public MealDiary() {}

    public MealDiary(Integer customerId, Integer foodId) {
        this.customerId = customerId;
        this.foodId = foodId;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(Integer caregiverId) {
        this.caregiverId = caregiverId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodUnit() {
        return foodUnit;
    }

    public void setFoodUnit(String foodUnit) {
        this.foodUnit = foodUnit;
    }

    public BigDecimal getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(BigDecimal foodPrice) {
        this.foodPrice = foodPrice;
    }

    public BigDecimal getFoodCalories() {
        return foodCalories;
    }

    public void setFoodCalories(BigDecimal foodCalories) {
        this.foodCalories = foodCalories;
    }

    public BigDecimal getFoodProtein() {
        return foodProtein;
    }

    public void setFoodProtein(BigDecimal foodProtein) {
        this.foodProtein = foodProtein;
    }

    public BigDecimal getFoodFat() {
        return foodFat;
    }

    public void setFoodFat(BigDecimal foodFat) {
        this.foodFat = foodFat;
    }

    public BigDecimal getFoodCarbs() {
        return foodCarbs;
    }

    public void setFoodCarbs(BigDecimal foodCarbs) {
        this.foodCarbs = foodCarbs;
    }

    public String getCaregiverName() {
        return caregiverName;
    }

    public void setCaregiverName(String caregiverName) {
        this.caregiverName = caregiverName;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(BigDecimal totalCalories) {
        this.totalCalories = totalCalories;
    }

    public BigDecimal getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(BigDecimal totalProtein) {
        this.totalProtein = totalProtein;
    }

    public BigDecimal getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(BigDecimal totalFat) {
        this.totalFat = totalFat;
    }

    public BigDecimal getTotalCarbs() {
        return totalCarbs;
    }

    public void setTotalCarbs(BigDecimal totalCarbs) {
        this.totalCarbs = totalCarbs;
    }

    @Override
    public String toString() {
        return "MealDiary{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", mealDate=" + mealDate +
                ", mealType='" + mealType + '\'' +
                ", foodId=" + foodId +
                ", quantity=" + quantity +
                ", customerName='" + customerName + '\'' +
                ", foodName='" + foodName + '\'' +
                ", totalPrice=" + totalPrice +
                ", totalCalories=" + totalCalories +
                '}';
    }
}