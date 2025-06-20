package com.cqupt.yyzx.service;


import com.cqupt.yyzx.entity.Bed;
import com.cqupt.yyzx.entity.Room;
import com.cqupt.yyzx.mapper.BedMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 床位业务逻辑服务
 */
@Service
public class BedService {

    @Autowired
    private BedMapper bedMapper;

    /**
     * 分页查询床位列表
     */
    public Map<String, Object> getBedList(String searchKeyword, String status, Integer floorNumber,
                                          Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<Bed> beds = bedMapper.selectBedListWithDetails(searchKeyword, status, floorNumber, offset, size);
        Integer total = bedMapper.countBeds(searchKeyword, status, floorNumber);

        Map<String, Object> result = new HashMap<>();
        result.put("beds", beds);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询床位详细信息
     */
    public Bed getBedById(Integer id) {
        return bedMapper.selectBedById(id);
    }

    /**
     * 添加床位
     */
    @Transactional
    public boolean addBed(Bed bed) {
        // 检查床位号是否已存在
        if (bedMapper.checkBedNumberExists(bed.getBedNumber(), null) > 0) {
            return false;
        }

        // 设置默认值
        if (bed.getBedType() == null || bed.getBedType().trim().isEmpty()) {
            bed.setBedType("普通床");
        }
        if (bed.getStatus() == null || bed.getStatus().trim().isEmpty()) {
            bed.setStatus("空闲");
        }
        if (bed.getDailyPrice() == null) {
            bed.setDailyPrice(new BigDecimal("150.00"));
        }

        return bedMapper.insertBed(bed) > 0;
    }

    /**
     * 更新床位信息
     */
    @Transactional
    public boolean updateBed(Bed bed) {
        // 检查床位号是否已存在（排除当前床位）
        if (bedMapper.checkBedNumberExists(bed.getBedNumber(), bed.getId()) > 0) {
            return false;
        }

        return bedMapper.updateBed(bed) > 0;
    }

    /**
     * 删除床位
     */
    @Transactional
    public boolean deleteBed(Integer id) {
        // 检查床位是否被占用
        Bed bed = bedMapper.selectBedById(id);
        if (bed != null && "占用".equals(bed.getStatus())) {
            throw new RuntimeException("床位已被占用，无法删除");
        }

        return bedMapper.deleteBed(id) > 0;
    }

    /**
     * 批量删除床位
     */
    @Transactional
    public boolean deleteBeds(List<Integer> ids) {
        // 检查是否有床位被占用
        for (Integer id : ids) {
            Bed bed = bedMapper.selectBedById(id);
            if (bed != null && "占用".equals(bed.getStatus())) {
                throw new RuntimeException("床位 " + bed.getBedNumber() + " 已被占用，无法删除");
            }
        }

        return bedMapper.deleteBeds(ids) > 0;
    }

    /**
     * 更新床位状态
     */
    @Transactional
    public boolean updateBedStatus(Integer id, String status) {
        return bedMapper.updateBedStatus(id, status) > 0;
    }

    /**
     * 获取所有房间信息
     */
    public List<Room> getAllRooms() {
        return bedMapper.selectAllRooms();
    }

    /**
     * 根据房间ID查询床位列表
     */
    public List<Bed> getBedsByRoomId(Integer roomId) {
        return bedMapper.selectBedsByRoomId(roomId);
    }

    /**
     * 获取床位状态统计
     */
    public Map<String, Object> getBedStats() {
        List<Map<String, Object>> statusStats = bedMapper.getBedStatusStats();

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int occupied = 0;
        int available = 0;
        int maintenance = 0;
        int reserved = 0;

        for (Map<String, Object> stat : statusStats) {
            String status = (String) stat.get("status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            total += countInt;

            switch (status) {
                case "占用":
                    occupied = countInt;
                    break;
                case "空闲":
                    available = countInt;
                    break;
                case "维修":
                    maintenance = countInt;
                    break;
                case "预留":
                    reserved = countInt;
                    break;
            }
        }

        result.put("total", total);
        result.put("occupied", occupied);
        result.put("available", available);
        result.put("maintenance", maintenance);
        result.put("reserved", reserved);

        return result;
    }

    /**
     * 检查床位号是否可用
     */
    public boolean isBedNumberAvailable(String bedNumber, Integer excludeId) {
        return bedMapper.checkBedNumberExists(bedNumber, excludeId) == 0;
    }
}