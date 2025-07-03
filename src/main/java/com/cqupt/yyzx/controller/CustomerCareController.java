package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.CustomerCaregiverAssignment;
import com.cqupt.yyzx.service.CustomerCareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客户护理设置控制器
 */
@RestController
@RequestMapping("/api/nursing/care")
@CrossOrigin
public class CustomerCareController {

    @Autowired
    private CustomerCareService careService;

    /**
     * 查询所有护理人员分配
     */
    @GetMapping("/assignments/all")
    public Result<List<CustomerCaregiverAssignment>> getAllAssignments() {
        try {
            List<CustomerCaregiverAssignment> assignments = careService.getAllAssignments();
            return Result.success(assignments);
        } catch (Exception e) {
            return Result.error("查询所有护理分配失败：" + e.getMessage());
        }
    }

    /**
     * 根据客户ID查询护理人员分配
     */
    @GetMapping("/customer/{customerId}/assignments")
    public Result<List<CustomerCaregiverAssignment>> getAssignmentsByCustomerId(@PathVariable Integer customerId) {
        try {
            List<CustomerCaregiverAssignment> assignments = careService.getAssignmentsByCustomerId(customerId);
            return Result.success(assignments);
        } catch (Exception e) {
            return Result.error("查询客户护理分配失败：" + e.getMessage());
        }
    }

    /**
     * 根据护理人员ID查询客户分配
     */
    @GetMapping("/caregiver/{caregiverId}/assignments")
    public Result<List<CustomerCaregiverAssignment>> getAssignmentsByCaregiverId(@PathVariable Integer caregiverId) {
        try {
            List<CustomerCaregiverAssignment> assignments = careService.getAssignmentsByCaregiverId(caregiverId);
            return Result.success(assignments);
        } catch (Exception e) {
            return Result.error("查询护理人员分配失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询分配详细信息
     */
    @GetMapping("/assignment/{id}")
    public Result<CustomerCaregiverAssignment> getAssignmentById(@PathVariable Integer id) {
        try {
            CustomerCaregiverAssignment assignment = careService.getAssignmentById(id);
            if (assignment != null) {
                return Result.success(assignment);
            } else {
                return Result.error("护理分配不存在");
            }
        } catch (Exception e) {
            return Result.error("查询护理分配信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建护理人员分配
     */
    @PostMapping("/assignment/create")
    public Result<String> createAssignment(@RequestBody CustomerCaregiverAssignment assignment) {
        try {
            // 基本验证
            if (assignment.getCustomerId() == null) {
                return Result.error("客户不能为空");
            }
            if (assignment.getCaregiverId() == null) {
                return Result.error("护理人员不能为空");
            }

            boolean success = careService.createAssignment(assignment);
            if (success) {
                return Result.success("创建护理分配成功");
            } else {
                return Result.error("创建护理分配失败");
            }
        } catch (Exception e) {
            return Result.error("创建护理分配失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理人员分配
     */
    @PutMapping("/assignment/update")
    public Result<String> updateAssignment(@RequestBody CustomerCaregiverAssignment assignment) {
        try {
            if (assignment.getId() == null) {
                return Result.error("分配ID不能为空");
            }

            boolean success = careService.updateAssignment(assignment);
            if (success) {
                return Result.success("更新护理分配成功");
            } else {
                return Result.error("更新护理分配失败");
            }
        } catch (Exception e) {
            return Result.error("更新护理分配失败：" + e.getMessage());
        }
    }

    /**
     * 删除护理人员分配
     */
    @DeleteMapping("/assignment/{id}")
    public Result<String> deleteAssignment(@PathVariable Integer id) {
        try {
            boolean success = careService.deleteAssignment(id);
            if (success) {
                return Result.success("删除护理分配成功");
            } else {
                return Result.error("删除护理分配失败");
            }
        } catch (Exception e) {
            return Result.error("删除护理分配失败：" + e.getMessage());
        }
    }

    /**
     * 更新分配状态
     */
    @PutMapping("/assignment/{id}/status")
    public Result<String> updateAssignmentStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            boolean success = careService.updateAssignmentStatus(id, status);
            if (success) {
                return Result.success("更新分配状态成功");
            } else {
                return Result.error("更新分配状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新分配状态失败：" + e.getMessage());
        }
    }

    /**
     * 终止客户的所有护理人员分配
     */
    @PutMapping("/customer/{customerId}/terminate")
    public Result<String> terminateCustomerAssignments(@PathVariable Integer customerId) {
        try {
            boolean success = careService.terminateCustomerAssignments(customerId);
            if (success) {
                return Result.success("终止客户分配成功");
            } else {
                return Result.error("终止客户分配失败");
            }
        } catch (Exception e) {
            return Result.error("终止客户分配失败：" + e.getMessage());
        }
    }

    /**
     * 查询客户的主要护理人员
     */
    @GetMapping("/customer/{customerId}/primary-caregiver")
    public Result<CustomerCaregiverAssignment> getPrimaryCaregiverByCustomerId(@PathVariable Integer customerId) {
        try {
            CustomerCaregiverAssignment assignment = careService.getPrimaryCaregiverByCustomerId(customerId);
            return Result.success(assignment);
        } catch (Exception e) {
            return Result.error("查询主要护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 设置主要护理人员
     */
    @PostMapping("/customer/{customerId}/primary-caregiver")
    public Result<String> setPrimaryCaregiver(@PathVariable Integer customerId, @RequestParam Integer caregiverId) {
        try {
            boolean success = careService.setPrimaryCaregiver(customerId, caregiverId);
            if (success) {
                return Result.success("设置主要护理人员成功");
            } else {
                return Result.error("设置主要护理人员失败");
            }
        } catch (Exception e) {
            return Result.error("设置主要护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 取消主要护理人员设置
     */
    @DeleteMapping("/customer/{customerId}/primary-caregiver")
    public Result<String> unsetPrimaryCaregiver(@PathVariable Integer customerId) {
        try {
            boolean success = careService.unsetPrimaryCaregiver(customerId);
            if (success) {
                return Result.success("取消主要护理人员成功");
            } else {
                return Result.error("取消主要护理人员失败");
            }
        } catch (Exception e) {
            return Result.error("取消主要护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 批量分配护理人员
     */
    @PostMapping("/customer/{customerId}/batch-assign")
    public Result<String> batchAssignCaregivers(@PathVariable Integer customerId, @RequestBody Map<String, Object> assignData) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> caregiverIds = (List<Integer>) assignData.get("caregiverIds");
            Integer primaryCaregiverId = (Integer) assignData.get("primaryCaregiverId");

            if (caregiverIds == null || caregiverIds.isEmpty()) {
                return Result.error("护理人员列表不能为空");
            }

            boolean success = careService.batchAssignCaregivers(customerId, caregiverIds, primaryCaregiverId);
            if (success) {
                return Result.success("批量分配护理人员成功");
            } else {
                return Result.error("批量分配护理人员失败");
            }
        } catch (Exception e) {
            return Result.error("批量分配护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 更换护理人员
     */
    @PostMapping("/customer/{customerId}/replace-caregivers")
    public Result<String> replaceCaregivers(@PathVariable Integer customerId, @RequestBody Map<String, Object> replaceData) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> newCaregiverIds = (List<Integer>) replaceData.get("newCaregiverIds");
            Integer newPrimaryCaregiverId = (Integer) replaceData.get("newPrimaryCaregiverId");

            if (newCaregiverIds == null || newCaregiverIds.isEmpty()) {
                return Result.error("新护理人员列表不能为空");
            }

            boolean success = careService.replaceCaregivers(customerId, newCaregiverIds, newPrimaryCaregiverId);
            if (success) {
                return Result.success("更换护理人员成功");
            } else {
                return Result.error("更换护理人员失败");
            }
        } catch (Exception e) {
            return Result.error("更换护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理人员工作负载统计
     */
    @GetMapping("/caregiver/workload-stats")
    public Result<List<Map<String, Object>>> getCaregiverWorkloadStats() {
        try {
            List<Map<String, Object>> stats = careService.getCaregiverWorkloadStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取工作负载统计失败：" + e.getMessage());
        }
    }

    /**
     * 查询分配历史记录
     */
    @GetMapping("/customer/{customerId}/assignment-history")
    public Result<List<CustomerCaregiverAssignment>> getAssignmentHistory(@PathVariable Integer customerId,
                                                                          @RequestParam(required = false) Integer limit) {
        try {
            List<CustomerCaregiverAssignment> history = careService.getAssignmentHistory(customerId, limit);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("查询分配历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用护理人员列表
     */
    @GetMapping("/available-caregivers")
    public Result<List<Map<String, Object>>> getAvailableCaregivers() {
        try {
            List<Map<String, Object>> caregivers = careService.getAvailableCaregivers();
            return Result.success(caregivers);
        } catch (Exception e) {
            return Result.error("获取可用护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 护理人员分配统计
     */
    @GetMapping("/assignment-stats")
    public Result<Map<String, Object>> getAssignmentStats() {
        try {
            Map<String, Object> stats = careService.getAssignmentStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取分配统计失败：" + e.getMessage());
        }
    }

    /**
     * 护理质量评估
     */
    @GetMapping("/quality-assessment")
    public Result<Map<String, Object>> getCareQualityAssessment(@RequestParam(required = false) Integer customerId,
                                                                @RequestParam(required = false) Integer caregiverId) {
        try {
            if (customerId == null && caregiverId == null) {
                return Result.error("客户ID或护理人员ID至少提供一个");
            }

            Map<String, Object> assessment = careService.getCareQualityAssessment(customerId, caregiverId);
            return Result.success(assessment);
        } catch (Exception e) {
            return Result.error("获取质量评估失败：" + e.getMessage());
        }
    }

    /**
     * 智能护理人员推荐
     */
    @GetMapping("/recommend-caregivers")
    public Result<List<Map<String, Object>>> recommendCaregivers(@RequestParam Integer customerId,
                                                                 @RequestParam(required = false) String nursingLevel) {
        try {
            List<Map<String, Object>> recommendations = careService.recommendCaregivers(customerId, nursingLevel);
            return Result.success(recommendations);
        } catch (Exception e) {
            return Result.error("获取护理人员推荐失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新分配状态
     */
    @PutMapping("/assignment/batch-status")
    public Result<String> batchUpdateAssignmentStatus(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) data.get("ids");
            Integer status = (Integer) data.get("status");

            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的分配");
            }

            boolean success = careService.batchUpdateAssignmentStatus(ids, status);
            if (success) {
                return Result.success("批量更新状态成功");
            } else {
                return Result.error("批量更新状态失败");
            }
        } catch (Exception e) {
            return Result.error("批量更新状态失败：" + e.getMessage());
        }
    }

    /**
     * 检查护理人员是否可以分配
     */
    @GetMapping("/caregiver/{caregiverId}/can-assign")
    public Result<Boolean> canAssignCaregiver(@PathVariable Integer caregiverId,
                                              @RequestParam(required = false) Integer maxCustomers) {
        try {
            boolean canAssign = careService.canAssignCaregiver(caregiverId, maxCustomers);
            return Result.success(canAssign);
        } catch (Exception e) {
            return Result.error("检查护理人员分配状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理团队配置
     */
    @GetMapping("/customer/{customerId}/team-config")
    public Result<Map<String, Object>> getCareTeamConfiguration(@PathVariable Integer customerId) {
        try {
            Map<String, Object> config = careService.getCareTeamConfiguration(customerId);
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取护理团队配置失败：" + e.getMessage());
        }
    }
}