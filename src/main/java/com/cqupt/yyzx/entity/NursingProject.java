package com.cqupt.yyzx.entity;

import java.time.LocalDateTime;

/**
 * 护理项目实体类
 */
public class NursingProject {
    private Integer id;
    private String projectName;
    private String projectCategory;
    private String description;
    private Integer standardDuration; // 标准时长(分钟)
    private Integer caregiverId;
    private String frequency; // 执行频率
    private Integer status; // 状态：1-启用，0-停用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String caregiverName; // 护理人员姓名

    // 构造函数
    public NursingProject() {}

    public NursingProject(String projectName, String projectCategory) {
        this.projectName = projectName;
        this.projectCategory = projectCategory;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStandardDuration() {
        return standardDuration;
    }

    public void setStandardDuration(Integer standardDuration) {
        this.standardDuration = standardDuration;
    }

    public Integer getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(Integer caregiverId) {
        this.caregiverId = caregiverId;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
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

    public String getCaregiverName() {
        return caregiverName;
    }

    public void setCaregiverName(String caregiverName) {
        this.caregiverName = caregiverName;
    }

    @Override
    public String toString() {
        return "NursingProject{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", projectCategory='" + projectCategory + '\'' +
                ", description='" + description + '\'' +
                ", standardDuration=" + standardDuration +
                ", caregiverId=" + caregiverId +
                ", frequency='" + frequency + '\'' +
                ", status=" + status +
                ", caregiverName='" + caregiverName + '\'' +
                '}';
    }
}