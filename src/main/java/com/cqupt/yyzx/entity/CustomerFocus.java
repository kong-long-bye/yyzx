package com.cqupt.yyzx.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户关注实体类
 */
public class CustomerFocus {
    private Integer id;
    private Integer customerId;
    private Integer caregiverId;
    private String focusLevel; // 高、中、低
    private String focusReason;
    private String focusContent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status; // 1-关注中，0-已结束
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String customerName; // 客户姓名
    private String caregiverName; // 护理人员姓名
    private String bedNumber; // 床位号
    private String roomNumber; // 房间号

    // 构造函数
    public CustomerFocus() {}

    public CustomerFocus(Integer customerId, Integer caregiverId) {
        this.customerId = customerId;
        this.caregiverId = caregiverId;
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

    public Integer getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(Integer caregiverId) {
        this.caregiverId = caregiverId;
    }

    public String getFocusLevel() {
        return focusLevel;
    }

    public void setFocusLevel(String focusLevel) {
        this.focusLevel = focusLevel;
    }

    public String getFocusReason() {
        return focusReason;
    }

    public void setFocusReason(String focusReason) {
        this.focusReason = focusReason;
    }

    public String getFocusContent() {
        return focusContent;
    }

    public void setFocusContent(String focusContent) {
        this.focusContent = focusContent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCaregiverName() {
        return caregiverName;
    }

    public void setCaregiverName(String caregiverName) {
        this.caregiverName = caregiverName;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public String toString() {
        return "CustomerFocus{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", caregiverId=" + caregiverId +
                ", focusLevel='" + focusLevel + '\'' +
                ", focusReason='" + focusReason + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", customerName='" + customerName + '\'' +
                ", caregiverName='" + caregiverName + '\'' +
                '}';
    }
}