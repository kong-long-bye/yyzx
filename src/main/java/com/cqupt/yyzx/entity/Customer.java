package com.cqupt.yyzx.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户实体类
 */
public class Customer {
    private Integer id;
    private String name;
    private String idCard;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String emergencyContact;
    private String emergencyPhone;
    private String address;
    private String medicalHistory;
    private String allergies;
    private Integer bedId;
    private LocalDate admissionDate;
    private Integer checkoutInfoId;
    private String auditStatus; // 待审核、已通过、已拒绝
    private Integer status; // 1-在住，0-已退住
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String bedNumber; // 床位号
    private String roomNumber; // 房间号
    private Integer floorNumber; // 楼层
    private String nursingLevel; // 护理等级
    private String primaryCaregiver; // 主要护理人员

    // 构造函数
    public Customer() {}

    public Customer(String name, String idCard) {
        this.name = name;
        this.idCard = idCard;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public Integer getBedId() {
        return bedId;
    }

    public void setBedId(Integer bedId) {
        this.bedId = bedId;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public Integer getCheckoutInfoId() {
        return checkoutInfoId;
    }

    public void setCheckoutInfoId(Integer checkoutInfoId) {
        this.checkoutInfoId = checkoutInfoId;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
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

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getNursingLevel() {
        return nursingLevel;
    }

    public void setNursingLevel(String nursingLevel) {
        this.nursingLevel = nursingLevel;
    }

    public String getPrimaryCaregiver() {
        return primaryCaregiver;
    }

    public void setPrimaryCaregiver(String primaryCaregiver) {
        this.primaryCaregiver = primaryCaregiver;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", idCard='" + idCard + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", phone='" + phone + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                ", emergencyPhone='" + emergencyPhone + '\'' +
                ", bedId=" + bedId +
                ", admissionDate=" + admissionDate +
                ", auditStatus='" + auditStatus + '\'' +
                ", status=" + status +
                '}';
    }
}