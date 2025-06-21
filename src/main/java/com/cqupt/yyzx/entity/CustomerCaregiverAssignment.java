package com.cqupt.yyzx.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户护理人员分配实体类
 */
public class CustomerCaregiverAssignment {
    private Integer id;
    private Integer customerId;
    private Integer caregiverId;
    private LocalDate assignmentDate;
    private LocalDate endDate;
    private Integer primaryCaregiver; // 是否主要护理人：1-是，0-否
    private Integer status; // 状态：1-有效，0-无效
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String customerName; // 客户姓名
    private String caregiverName; // 护理人员姓名

    // 构造函数
    public CustomerCaregiverAssignment() {}

    public CustomerCaregiverAssignment(Integer customerId, Integer caregiverId) {
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

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getPrimaryCaregiver() {
        return primaryCaregiver;
    }

    public void setPrimaryCaregiver(Integer primaryCaregiver) {
        this.primaryCaregiver = primaryCaregiver;
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

    @Override
    public String toString() {
        return "CustomerCaregiverAssignment{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", caregiverId=" + caregiverId +
                ", assignmentDate=" + assignmentDate +
                ", endDate=" + endDate +
                ", primaryCaregiver=" + primaryCaregiver +
                ", status=" + status +
                '}';
    }
}