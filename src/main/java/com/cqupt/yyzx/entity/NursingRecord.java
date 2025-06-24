package com.cqupt.yyzx.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 护理记录实体类
 */
public class NursingRecord {
    private Integer id;
    private Integer customerId;
    private Integer projectId;
    private Integer caregiverId;
    private LocalDate executionDate;
    private LocalTime executionTime;
    private Integer duration; // 实际时长(分钟)
    private String executionStatus; // 已完成、部分完成、未执行、异常
    private String notes;
    private LocalDateTime createdAt;

    // 关联字段
    private String customerName; // 客户姓名
    private String projectName; // 项目名称
    private String projectCategory; // 项目分类
    private String caregiverName; // 护理人员姓名
    private Integer standardDuration; // 标准时长

    // 构造函数
    public NursingRecord() {}

    public NursingRecord(Integer customerId, Integer projectId, Integer caregiverId) {
        this.customerId = customerId;
        this.projectId = projectId;
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

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(Integer caregiverId) {
        this.caregiverId = caregiverId;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public LocalTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalTime executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectCategory() {
        return projectCategory;
    }

    public void setProjectCategory(String projectCategory) {
        this.projectCategory = projectCategory;
    }

    public String getCaregiverName() {
        return caregiverName;
    }

    public void setCaregiverName(String caregiverName) {
        this.caregiverName = caregiverName;
    }

    public Integer getStandardDuration() {
        return standardDuration;
    }

    public void setStandardDuration(Integer standardDuration) {
        this.standardDuration = standardDuration;
    }

    @Override
    public String toString() {
        return "NursingRecord{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", projectId=" + projectId +
                ", caregiverId=" + caregiverId +
                ", executionDate=" + executionDate +
                ", executionTime=" + executionTime +
                ", duration=" + duration +
                ", executionStatus='" + executionStatus + '\'' +
                ", notes='" + notes + '\'' +
                ", customerName='" + customerName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", caregiverName='" + caregiverName + '\'' +
                '}';
    }
}