package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.NursingRecord;
import com.cqupt.yyzx.service.NursingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 护理记录控制器
 */
@RestController
@RequestMapping("/api/nursing/record")
@CrossOrigin
public class NursingRecordController {

    @Autowired
    private NursingRecordService recordService;

    /**
     * 分页查询护理记录列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getRecordList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String executionStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer caregiverId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = recordService.getRecordList(
                    searchKeyword, executionStatus, startDate, endDate,
                    customerId, projectId, caregiverId, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询护理记录列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询护理记录详细信息
     */
    @GetMapping("/{id}")
    public Result<NursingRecord> getRecordById(@PathVariable Integer id) {
        try {
            NursingRecord record = recordService.getRecordById(id);
            if (record != null) {
                return Result.success(record);
            } else {
                return Result.error("护理记录不存在");
            }
        } catch (Exception e) {
            return Result.error("查询护理记录信息失败：" + e.getMessage());
        }
    }

    /**
     * 添加护理记录
     */
    @PostMapping("/add")
    public Result<String> addRecord(@RequestBody NursingRecord record) {
        try {
            // 基本验证
            if (record.getCustomerId() == null) {
                return Result.error("客户不能为空");
            }
            if (record.getProjectId() == null) {
                return Result.error("护理项目不能为空");
            }
            if (record.getCaregiverId() == null) {
                return Result.error("护理人员不能为空");
            }

            boolean success = recordService.addRecord(record);
            if (success) {
                return Result.success("添加护理记录成功");
            } else {
                return Result.error("添加护理记录失败");
            }
        } catch (Exception e) {
            return Result.error("添加护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理记录信息
     */
    @PutMapping("/update")
    public Result<String> updateRecord(@RequestBody NursingRecord record) {
        try {
            if (record.getId() == null) {
                return Result.error("护理记录ID不能为空");
            }

            boolean success = recordService.updateRecord(record);
            if (success) {
                return Result.success("更新护理记录成功");
            } else {
                return Result.error("更新护理记录失败");
            }
        } catch (Exception e) {
            return Result.error("更新护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除护理记录
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRecord(@PathVariable Integer id) {
        try {
            boolean success = recordService.deleteRecord(id);
            if (success) {
                return Result.success("删除护理记录成功");
            } else {
                return Result.error("删除护理记录失败");
            }
        } catch (Exception e) {
            return Result.error("删除护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除护理记录
     */
    @DeleteMapping("/batch")
    public Result<String> deleteRecords(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的护理记录");
            }

            boolean success = recordService.deleteRecords(ids);
            if (success) {
                return Result.success("批量删除护理记录成功");
            } else {
                return Result.error("批量删除护理记录失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据客户ID查询护理记录
     */
    @GetMapping("/customer/{customerId}")
    public Result<List<NursingRecord>> getRecordsByCustomerId(@PathVariable Integer customerId,
                                                              @RequestParam(required = false) Integer limit) {
        try {
            List<NursingRecord> records = recordService.getRecordsByCustomerId(customerId, limit);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询客户护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据护理人员ID查询护理记录
     */
    @GetMapping("/caregiver/{caregiverId}")
    public Result<List<NursingRecord>> getRecordsByCaregiverId(
            @PathVariable Integer caregiverId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<NursingRecord> records = recordService.getRecordsByCaregiverId(caregiverId, startDate, endDate);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询护理人员记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据项目ID查询护理记录
     */
    @GetMapping("/project/{projectId}")
    public Result<List<NursingRecord>> getRecordsByProjectId(
            @PathVariable Integer projectId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<NursingRecord> records = recordService.getRecordsByProjectId(projectId, startDate, endDate);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询项目记录失败：" + e.getMessage());
        }
    }

    /**
     * 查询今日护理记录
     */
    @GetMapping("/today")
    public Result<List<NursingRecord>> getTodayRecords(@RequestParam(required = false) Integer caregiverId) {
        try {
            List<NursingRecord> records = recordService.getTodayRecords(caregiverId);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询今日记录失败：" + e.getMessage());
        }
    }

    /**
     * 查询未完成的护理记录
     */
    @GetMapping("/uncompleted")
    public Result<List<NursingRecord>> getUncompletedRecords() {
        try {
            List<NursingRecord> records = recordService.getUncompletedRecords();
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询未完成记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理记录统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getRecordStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> stats = recordService.getRecordStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取记录统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理人员工作量统计
     */
    @GetMapping("/caregiver-workload")
    public Result<List<Map<String, Object>>> getCaregiverWorkloadStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<Map<String, Object>> stats = recordService.getCaregiverWorkloadStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取工作量统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取项目执行统计
     */
    @GetMapping("/project-execution")
    public Result<List<Map<String, Object>>> getProjectExecutionStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<Map<String, Object>> stats = recordService.getProjectExecutionStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取项目统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户护理统计
     */
    @GetMapping("/customer-care/{customerId}")
    public Result<Map<String, Object>> getCustomerCareStats(
            @PathVariable Integer customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> stats = recordService.getCustomerCareStats(customerId, startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取客户护理统计失败：" + e.getMessage());
        }
    }

    /**
     * 批量添加护理记录
     */
    @PostMapping("/batch-add")
    public Result<String> batchAddRecords(@RequestBody List<NursingRecord> records) {
        try {
            if (records == null || records.isEmpty()) {
                return Result.error("护理记录列表不能为空");
            }

            boolean success = recordService.batchAddRecords(records);
            if (success) {
                return Result.success("批量添加护理记录成功");
            } else {
                return Result.error("批量添加护理记录失败");
            }
        } catch (Exception e) {
            return Result.error("批量添加护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理记录状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateRecordStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            boolean success = recordService.updateRecordStatus(id, status);
            if (success) {
                return Result.success("更新记录状态成功");
            } else {
                return Result.error("更新记录状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新记录状态失败：" + e.getMessage());
        }
    }

    /**
     * 快速记录护理
     */
    @PostMapping("/quick-record")
    public Result<String> quickRecord(@RequestBody Map<String, Object> data) {
        try {
            Integer customerId = (Integer) data.get("customerId");
            Integer projectId = (Integer) data.get("projectId");
            Integer caregiverId = (Integer) data.get("caregiverId");
            String notes = (String) data.get("notes");

            if (customerId == null || projectId == null || caregiverId == null) {
                return Result.error("客户、项目和护理人员不能为空");
            }

            boolean success = recordService.quickRecord(customerId, projectId, caregiverId, notes);
            if (success) {
                return Result.success("快速记录成功");
            } else {
                return Result.error("快速记录失败");
            }
        } catch (Exception e) {
            return Result.error("快速记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理质量评估数据
     */
    @GetMapping("/quality-data")
    public Result<Map<String, Object>> getNursingQualityData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> data = recordService.getNursingQualityData(startDate, endDate);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取质量数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取执行状态选项
     */
    @GetMapping("/status-options")
    public Result<List<String>> getExecutionStatusOptions() {
        try {
            List<String> options = recordService.getExecutionStatusOptions();
            return Result.success(options);
        } catch (Exception e) {
            return Result.error("获取状态选项失败：" + e.getMessage());
        }
    }
}