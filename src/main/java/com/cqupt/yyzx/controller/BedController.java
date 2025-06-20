package com.cqupt.yyzx.controller;


import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.Bed;
import com.cqupt.yyzx.entity.Room;
import com.cqupt.yyzx.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 床位管理控制器
 */
@RestController
@RequestMapping("/api/bed")
@CrossOrigin
public class BedController {

    @Autowired
    private BedService bedService;

    /**
     * 分页查询床位列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getBedList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floorNumber,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = bedService.getBedList(searchKeyword, status, floorNumber, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询床位列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询床位详细信息
     */
    @GetMapping("/{id}")
    public Result<Bed> getBedById(@PathVariable Integer id) {
        try {
            Bed bed = bedService.getBedById(id);
            if (bed != null) {
                return Result.success(bed);
            } else {
                return Result.error("床位不存在");
            }
        } catch (Exception e) {
            return Result.error("查询床位信息失败：" + e.getMessage());
        }
    }

    /**
     * 添加床位
     */
    @PostMapping("/add")
    public Result<String> addBed(@RequestBody Bed bed) {
        try {
            // 基本验证
            if (bed.getBedNumber() == null || bed.getBedNumber().trim().isEmpty()) {
                return Result.error("床位编号不能为空");
            }
            if (bed.getRoomId() == null) {
                return Result.error("房间不能为空");
            }

            boolean success = bedService.addBed(bed);
            if (success) {
                return Result.success("添加床位成功");
            } else {
                return Result.error("床位编号已存在");
            }
        } catch (Exception e) {
            return Result.error("添加床位失败：" + e.getMessage());
        }
    }

    /**
     * 更新床位信息
     */
    @PutMapping("/update")
    public Result<String> updateBed(@RequestBody Bed bed) {
        try {
            // 基本验证
            if (bed.getId() == null) {
                return Result.error("床位ID不能为空");
            }
            if (bed.getBedNumber() == null || bed.getBedNumber().trim().isEmpty()) {
                return Result.error("床位编号不能为空");
            }
            if (bed.getRoomId() == null) {
                return Result.error("房间不能为空");
            }

            boolean success = bedService.updateBed(bed);
            if (success) {
                return Result.success("更新床位成功");
            } else {
                return Result.error("床位编号已存在");
            }
        } catch (Exception e) {
            return Result.error("更新床位失败：" + e.getMessage());
        }
    }

    /**
     * 删除床位
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteBed(@PathVariable Integer id) {
        try {
            boolean success = bedService.deleteBed(id);
            if (success) {
                return Result.success("删除床位成功");
            } else {
                return Result.error("删除床位失败");
            }
        } catch (Exception e) {
            return Result.error("删除床位失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除床位
     */
    @DeleteMapping("/batch")
    public Result<String> deleteBeds(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的床位");
            }

            boolean success = bedService.deleteBeds(ids);
            if (success) {
                return Result.success("批量删除床位成功");
            } else {
                return Result.error("批量删除床位失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除床位失败：" + e.getMessage());
        }
    }

    /**
     * 更新床位状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateBedStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            boolean success = bedService.updateBedStatus(id, status);
            if (success) {
                return Result.success("更新床位状态成功");
            } else {
                return Result.error("更新床位状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新床位状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有房间信息
     */
    @GetMapping("/rooms")
    public Result<List<Room>> getAllRooms() {
        try {
            List<Room> rooms = bedService.getAllRooms();
            return Result.success(rooms);
        } catch (Exception e) {
            return Result.error("查询房间信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据房间ID查询床位列表
     */
    @GetMapping("/room/{roomId}")
    public Result<List<Bed>> getBedsByRoomId(@PathVariable Integer roomId) {
        try {
            List<Bed> beds = bedService.getBedsByRoomId(roomId);
            return Result.success(beds);
        } catch (Exception e) {
            return Result.error("查询房间床位失败：" + e.getMessage());
        }
    }

    /**
     * 获取床位状态统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getBedStats() {
        try {
            Map<String, Object> stats = bedService.getBedStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取床位统计失败：" + e.getMessage());
        }
    }

    /**
     * 检查床位号是否可用
     */
    @GetMapping("/check-number")
    public Result<Boolean> checkBedNumber(@RequestParam String bedNumber,
                                          @RequestParam(required = false) Integer excludeId) {
        try {
            boolean available = bedService.isBedNumberAvailable(bedNumber, excludeId);
            return Result.success(available);
        } catch (Exception e) {
            return Result.error("检查床位号失败：" + e.getMessage());
        }
    }
}