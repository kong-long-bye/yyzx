package com.cqupt.yyzx.entity;



import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 床位实体类
 */
public class Bed {
    private Integer id;
    private String bedNumber;
    private Integer roomId;
    private String bedType;
    private String status; // 空闲、占用、维修、预留
    private BigDecimal dailyPrice;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String roomNumber; // 房间号
    private Integer floorNumber; // 楼层
    private String customerName; // 客户姓名

    // 构造函数
    public Bed() {}

    public Bed(String bedNumber, Integer roomId) {
        this.bedNumber = bedNumber;
        this.roomId = roomId;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getBedType() {
        return bedType;
    }

    public void setBedType(String bedType) {
        this.bedType = bedType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getDailyPrice() {
        return dailyPrice;
    }

    public void setDailyPrice(BigDecimal dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "Bed{" +
                "id=" + id +
                ", bedNumber='" + bedNumber + '\'' +
                ", roomId=" + roomId +
                ", bedType='" + bedType + '\'' +
                ", status='" + status + '\'' +
                ", dailyPrice=" + dailyPrice +
                ", description='" + description + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", floorNumber=" + floorNumber +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}