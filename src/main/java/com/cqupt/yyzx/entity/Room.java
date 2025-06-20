package com.cqupt.yyzx.entity;



import java.time.LocalDateTime;

/**
 * 房间实体类
 */
public class Room {
    private Integer id;
    private String roomNumber;
    private Integer floorNumber;
    private String roomType;
    private Integer maxBeds;
    private String description;
    private Integer status; // 1-正常，0-停用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 统计字段
    private Integer occupiedBeds; // 已占用床位数
    private Integer availableBeds; // 可用床位数

    // 构造函数
    public Room() {}

    public Room(String roomNumber, Integer floorNumber) {
        this.roomNumber = roomNumber;
        this.floorNumber = floorNumber;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getMaxBeds() {
        return maxBeds;
    }

    public void setMaxBeds(Integer maxBeds) {
        this.maxBeds = maxBeds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public Integer getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(Integer availableBeds) {
        this.availableBeds = availableBeds;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomNumber='" + roomNumber + '\'' +
                ", floorNumber=" + floorNumber +
                ", roomType='" + roomType + '\'' +
                ", maxBeds=" + maxBeds +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", occupiedBeds=" + occupiedBeds +
                ", availableBeds=" + availableBeds +
                '}';
    }
}