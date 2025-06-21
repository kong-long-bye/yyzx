package com.cqupt.yyzx.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 外出记录实体类
 */
public class OutingRecord {
    private Integer id;
    private Integer customerId;
    private LocalDate outingDate;
    private LocalTime outingTime;
    private LocalTime returnTime;
    private String destination;
    private String companion;
    private String reason;
    private String approvalStatus; // 待批准、已批准、已拒绝
    private LocalTime actualReturnTime;
    private String notes;
    private Integer caregiverId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String customerName; // 客户姓名
    private String caregiverName; // 登记人姓名

    // 构造函数
    public OutingRecord() {}

    public OutingRecord(Integer customerId, LocalDate outingDate) {
        this.customerId = customerId;
        this.outingDate = outingDate;
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

    public LocalDate getOutingDate() {
        return outingDate;
    }

    public void setOutingDate(LocalDate outingDate) {
        this.outingDate = outingDate;
    }

    public LocalTime getOutingTime() {
        return outingTime;
    }

    public void setOutingTime(LocalTime outingTime) {
        this.outingTime = outingTime;
    }

    public LocalTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalTime returnTime) {
        this.returnTime = returnTime;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCompanion() {
        return companion;
    }

    public void setCompanion(String companion) {
        this.companion = companion;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public LocalTime getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(LocalTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
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
        return "OutingRecord{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", outingDate=" + outingDate +
                ", outingTime=" + outingTime +
                ", returnTime=" + returnTime +
                ", destination='" + destination + '\'' +
                ", companion='" + companion + '\'' +
                ", reason='" + reason + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", actualReturnTime=" + actualReturnTime +
                ", caregiverId=" + caregiverId +
                '}';
    }
}