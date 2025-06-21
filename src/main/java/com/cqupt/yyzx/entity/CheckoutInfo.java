package com.cqupt.yyzx.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 退住信息实体类
 */
public class CheckoutInfo {
    private Integer id;
    private String checkoutType;
    private String checkoutReason;
    private LocalDate checkoutDate;
    private String remarks;
    private LocalDateTime createdAt;

    // 构造函数
    public CheckoutInfo() {}

    public CheckoutInfo(String checkoutType, LocalDate checkoutDate) {
        this.checkoutType = checkoutType;
        this.checkoutDate = checkoutDate;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCheckoutType() {
        return checkoutType;
    }

    public void setCheckoutType(String checkoutType) {
        this.checkoutType = checkoutType;
    }

    public String getCheckoutReason() {
        return checkoutReason;
    }

    public void setCheckoutReason(String checkoutReason) {
        this.checkoutReason = checkoutReason;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(LocalDate checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CheckoutInfo{" +
                "id=" + id +
                ", checkoutType='" + checkoutType + '\'' +
                ", checkoutReason='" + checkoutReason + '\'' +
                ", checkoutDate=" + checkoutDate +
                ", remarks='" + remarks + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}