package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.Customer;
import com.cqupt.yyzx.entity.CustomerFocus;
import com.cqupt.yyzx.service.HealthManagerService;
import com.cqupt.yyzx.service.CustomerFocusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 健康管家控制器
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin
public class HealthManagerController {

    @Autowired
    private HealthManagerService healthManagerService;

    @Autowired
    private CustomerFocusService customerFocusService;

    // ==================== 服务对象管理 (SERVICE_ASSIGNMENT) ====================

    /**
     * 获取护理人员负责的客户列表（服务对象）
     */
    @GetMapping("/assignment/customers")
    public Result<Map<String, Object>> getServiceCustomerList(
            @RequestParam Integer caregiverId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = healthManagerService.getServiceCustomerList(
                    caregiverId, searchKeyword, auditStatus, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询服务对象列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理人员的工作概览
     */
    @GetMapping("/assignment/overview")
    public Result<Map<String, Object>> getCaregiverWorkOverview(@RequestParam Integer caregiverId) {
        try {
            Map<String, Object> overview = healthManagerService.getCaregiverWorkOverview(caregiverId);
            return Result.success(overview);
        } catch (Exception e) {
            return Result.error("获取工作概览失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户护理详情
     */
    @GetMapping("/assignment/customer/{customerId}/detail")
    public Result<Map<String, Object>> getCustomerCareDetail(@PathVariable Integer customerId,
                                                             @RequestParam Integer caregiverId) {
        try {
            Map<String, Object> detail = healthManagerService.getCustomerCareDetail(customerId, caregiverId);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取客户护理详情失败：" + e.getMessage());
        }
    }

    /**
     * 申请服务新客户
     */
    @PostMapping("/assignment/apply")
    public Result<String> applyForNewCustomer(@RequestBody Map<String, Object> applyData) {
        try {
            Integer caregiverId = (Integer) applyData.get("caregiverId");
            Integer customerId = (Integer) applyData.get("customerId");
            String reason = (String) applyData.get("reason");

            if (caregiverId == null || customerId == null) {
                return Result.error("护理人员ID和客户ID不能为空");
            }

            boolean success = healthManagerService.applyForNewCustomer(caregiverId, customerId, reason);
            if (success) {
                return Result.success("申请服务客户成功");
            } else {
                return Result.error("申请服务客户失败");
            }
        } catch (Exception e) {
            return Result.error("申请服务客户失败：" + e.getMessage());
        }
    }

    /**
     * 移交客户给其他护理人员
     */
    @PostMapping("/assignment/transfer")
    public Result<String> transferCustomer(@RequestBody Map<String, Object> transferData) {
        try {
            Integer customerId = (Integer) transferData.get("customerId");
            Integer fromCaregiverId = (Integer) transferData.get("fromCaregiverId");
            Integer toCaregiverId = (Integer) transferData.get("toCaregiverId");
            String reason = (String) transferData.get("reason");

            if (customerId == null || fromCaregiverId == null || toCaregiverId == null) {
                return Result.error("必填参数不能为空");
            }

            boolean success = healthManagerService.transferCustomer(customerId, fromCaregiverId, toCaregiverId, reason);
            if (success) {
                return Result.success("移交客户成功");
            } else {
                return Result.error("移交客户失败");
            }
        } catch (Exception e) {
            return Result.error("移交客户失败：" + e.getMessage());
        }
    }

    /**
     * 申请成为客户的主要护理人员
     */
    @PostMapping("/assignment/apply-primary")
    public Result<String> applyForPrimaryCaregiver(@RequestBody Map<String, Object> applyData) {
        try {
            Integer customerId = (Integer) applyData.get("customerId");
            Integer caregiverId = (Integer) applyData.get("caregiverId");

            if (customerId == null || caregiverId == null) {
                return Result.error("客户ID和护理人员ID不能为空");
            }

            boolean success = healthManagerService.applyForPrimaryCaregiver(customerId, caregiverId);
            if (success) {
                return Result.success("申请主要护理人员成功");
            } else {
                return Result.error("申请主要护理人员失败");
            }
        } catch (Exception e) {
            return Result.error("申请主要护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 获取可申请的客户列表
     */
    @GetMapping("/assignment/available-customers")
    public Result<List<Customer>> getAvailableCustomersForApplication(@RequestParam Integer caregiverId) {
        try {
            List<Customer> customers = healthManagerService.getAvailableCustomersForApplication(caregiverId);
            return Result.success(customers);
        } catch (Exception e) {
            return Result.error("获取可申请客户列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理人员工作统计
     */
    @GetMapping("/assignment/work-stats")
    public Result<Map<String, Object>> getCaregiverWorkStats(
            @RequestParam Integer caregiverId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> stats = healthManagerService.getCaregiverWorkStats(caregiverId, startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取工作统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户关怀建议
     */
    @GetMapping("/assignment/care-suggestions")
    public Result<Map<String, Object>> getCustomerCareSuggestions(@RequestParam Integer customerId,
                                                                  @RequestParam Integer caregiverId) {
        try {
            Map<String, Object> suggestions = healthManagerService.getCustomerCareSuggestions(customerId, caregiverId);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.error("获取关怀建议失败：" + e.getMessage());
        }
    }

    /**
     * 获取团队协作信息
     */
    @GetMapping("/assignment/team-collaboration/{customerId}")
    public Result<Map<String, Object>> getTeamCollaborationInfo(@PathVariable Integer customerId) {
        try {
            Map<String, Object> teamInfo = healthManagerService.getTeamCollaborationInfo(customerId);
            return Result.success(teamInfo);
        } catch (Exception e) {
            return Result.error("获取团队协作信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取工作提醒
     */
    @GetMapping("/assignment/work-reminders")
    public Result<Map<String, Object>> getWorkReminders(@RequestParam Integer caregiverId) {
        try {
            Map<String, Object> reminders = healthManagerService.getWorkReminders(caregiverId);
            return Result.success(reminders);
        } catch (Exception e) {
            return Result.error("获取工作提醒失败：" + e.getMessage());
        }
    }

    // ==================== 服务关注管理 (SERVICE_FOCUS) ====================

    /**
     * 分页查询客户关注列表
     */
    @GetMapping("/focus/list")
    public Result<Map<String, Object>> getFocusList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String focusLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer caregiverId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = customerFocusService.getFocusList(
                    searchKeyword, focusLevel, status, caregiverId, startDate, endDate, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询客户关注列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询客户关注详细信息
     */
    @GetMapping("/focus/{id}")
    public Result<CustomerFocus> getFocusById(@PathVariable Integer id) {
        try {
            CustomerFocus focus = customerFocusService.getFocusById(id);
            if (focus != null) {
                return Result.success(focus);
            } else {
                return Result.error("客户关注记录不存在");
            }
        } catch (Exception e) {
            return Result.error("查询客户关注信息失败：" + e.getMessage());
        }
    }

    /**
     * 添加客户关注
     */
    @PostMapping("/focus/add")
    public Result<String> addFocus(@RequestBody CustomerFocus focus) {
        try {
            // 基本验证
            if (focus.getCustomerId() == null) {
                return Result.error("客户不能为空");
            }
            if (focus.getCaregiverId() == null) {
                return Result.error("护理人员不能为空");
            }
            if (focus.getFocusReason() == null || focus.getFocusReason().trim().isEmpty()) {
                return Result.error("关注原因不能为空");
            }

            boolean success = customerFocusService.addFocus(focus);
            if (success) {
                return Result.success("添加客户关注成功");
            } else {
                return Result.error("添加客户关注失败");
            }
        } catch (Exception e) {
            return Result.error("添加客户关注失败：" + e.getMessage());
        }
    }

    /**
     * 更新客户关注信息
     */
    @PutMapping("/focus/update")
    public Result<String> updateFocus(@RequestBody CustomerFocus focus) {
        try {
            if (focus.getId() == null) {
                return Result.error("关注记录ID不能为空");
            }

            boolean success = customerFocusService.updateFocus(focus);
            if (success) {
                return Result.success("更新客户关注成功");
            } else {
                return Result.error("更新客户关注失败");
            }
        } catch (Exception e) {
            return Result.error("更新客户关注失败：" + e.getMessage());
        }
    }

    /**
     * 删除客户关注
     */
    @DeleteMapping("/focus/{id}")
    public Result<String> deleteFocus(@PathVariable Integer id) {
        try {
            boolean success = customerFocusService.deleteFocus(id);
            if (success) {
                return Result.success("删除客户关注成功");
            } else {
                return Result.error("删除客户关注失败");
            }
        } catch (Exception e) {
            return Result.error("删除客户关注失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除客户关注
     */
    @DeleteMapping("/focus/batch")
    public Result<String> deleteFocuses(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的关注记录");
            }

            boolean success = customerFocusService.deleteFocuses(ids);
            if (success) {
                return Result.success("批量删除客户关注成功");
            } else {
                return Result.error("批量删除客户关注失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除客户关注失败：" + e.getMessage());
        }
    }

    /**
     * 更新客户关注状态
     */
    @PutMapping("/focus/{id}/status")
    public Result<String> updateFocusStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            boolean success = customerFocusService.updateFocusStatus(id, status);
            if (success) {
                return Result.success("更新关注状态成功");
            } else {
                return Result.error("更新关注状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新关注状态失败：" + e.getMessage());
        }
    }

    /**
     * 根据护理人员ID查询关注客户列表
     */
    @GetMapping("/focus/caregiver/{caregiverId}")
    public Result<List<CustomerFocus>> getFocusesByCaregiverId(@PathVariable Integer caregiverId,
                                                               @RequestParam(required = false) Integer status) {
        try {
            List<CustomerFocus> focuses = customerFocusService.getFocusesByCaregiverId(caregiverId, status);
            return Result.success(focuses);
        } catch (Exception e) {
            return Result.error("查询护理人员关注客户失败：" + e.getMessage());
        }
    }

    /**
     * 根据客户ID查询关注记录
     */
    @GetMapping("/focus/customer/{customerId}")
    public Result<List<CustomerFocus>> getFocusesByCustomerId(@PathVariable Integer customerId) {
        try {
            List<CustomerFocus> focuses = customerFocusService.getFocusesByCustomerId(customerId);
            return Result.success(focuses);
        } catch (Exception e) {
            return Result.error("查询客户关注记录失败：" + e.getMessage());
        }
    }

    /**
     * 检查客户是否已被关注
     */
    @GetMapping("/focus/check")
    public Result<Boolean> isCustomerFocused(@RequestParam Integer customerId, @RequestParam Integer caregiverId) {
        try {
            boolean focused = customerFocusService.isCustomerFocused(customerId, caregiverId);
            return Result.success(focused);
        } catch (Exception e) {
            return Result.error("检查客户关注状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取关注统计信息
     */
    @GetMapping("/focus/stats")
    public Result<Map<String, Object>> getFocusStats(
            @RequestParam Integer caregiverId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> stats = customerFocusService.getFocusStats(caregiverId, startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取关注统计失败：" + e.getMessage());
        }
    }

    /**
     * 查询即将到期的关注
     */
    @GetMapping("/focus/expiring")
    public Result<List<CustomerFocus>> getExpiringFocuses(@RequestParam(required = false) Integer days,
                                                          @RequestParam Integer caregiverId) {
        try {
            List<CustomerFocus> focuses = customerFocusService.getExpiringFocuses(days, caregiverId);
            return Result.success(focuses);
        } catch (Exception e) {
            return Result.error("查询即将到期关注失败：" + e.getMessage());
        }
    }

    /**
     * 查询高关注级别客户
     */
    @GetMapping("/focus/high-priority")
    public Result<List<CustomerFocus>> getHighPriorityFocuses(@RequestParam Integer caregiverId) {
        try {
            List<CustomerFocus> focuses = customerFocusService.getHighPriorityFocuses(caregiverId);
            return Result.success(focuses);
        } catch (Exception e) {
            return Result.error("查询高关注级别客户失败：" + e.getMessage());
        }
    }

    /**
     * 根据关注级别统计
     */
    @GetMapping("/focus/stats-by-level")
    public Result<Map<String, Object>> getFocusStatsByLevel(@RequestParam Integer caregiverId) {
        try {
            Map<String, Object> stats = customerFocusService.getFocusStatsByLevel(caregiverId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取关注级别统计失败：" + e.getMessage());
        }
    }

    /**
     * 设置关注到期
     */
    @PostMapping("/focus/{id}/expire")
    public Result<String> setFocusExpire(@PathVariable Integer id,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            boolean success = customerFocusService.setFocusExpire(id, endDate);
            if (success) {
                return Result.success("设置关注到期成功");
            } else {
                return Result.error("设置关注到期失败");
            }
        } catch (Exception e) {
            return Result.error("设置关注到期失败：" + e.getMessage());
        }
    }

    /**
     * 延长关注期限
     */
    @PostMapping("/focus/{id}/extend")
    public Result<String> extendFocus(@PathVariable Integer id,
                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate newEndDate) {
        try {
            boolean success = customerFocusService.extendFocus(id, newEndDate);
            if (success) {
                return Result.success("延长关注期限成功");
            } else {
                return Result.error("延长关注期限失败");
            }
        } catch (Exception e) {
            return Result.error("延长关注期限失败：" + e.getMessage());
        }
    }

    /**
     * 升级关注级别
     */
    @PostMapping("/focus/{id}/upgrade")
    public Result<String> upgradeFocusLevel(@PathVariable Integer id, @RequestBody Map<String, String> upgradeData) {
        try {
            String newLevel = upgradeData.get("newLevel");
            String reason = upgradeData.get("reason");

            if (newLevel == null || newLevel.trim().isEmpty()) {
                return Result.error("新关注级别不能为空");
            }

            boolean success = customerFocusService.upgradeFocusLevel(id, newLevel, reason);
            if (success) {
                return Result.success("升级关注级别成功");
            } else {
                return Result.error("升级关注级别失败");
            }
        } catch (Exception e) {
            return Result.error("升级关注级别失败：" + e.getMessage());
        }
    }

    /**
     * 获取关注级别选项
     */
    @GetMapping("/focus/level-options")
    public Result<List<String>> getFocusLevelOptions() {
        try {
            List<String> options = customerFocusService.getFocusLevelOptions();
            return Result.success(options);
        } catch (Exception e) {
            return Result.error("获取关注级别选项失败：" + e.getMessage());
        }
    }

    /**
     * 获取关注原因模板
     */
    @GetMapping("/focus/reason-templates")
    public Result<List<String>> getFocusReasonTemplates() {
        try {
            List<String> templates = customerFocusService.getFocusReasonTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取关注原因模板失败：" + e.getMessage());
        }
    }

    /**
     * 智能关注建议
     */
    @GetMapping("/focus/recommendations")
    public Result<Map<String, Object>> getFocusRecommendations(@RequestParam Integer caregiverId) {
        try {
            Map<String, Object> recommendations = customerFocusService.getFocusRecommendations(caregiverId);
            return Result.success(recommendations);
        } catch (Exception e) {
            return Result.error("获取关注建议失败：" + e.getMessage());
        }
    }

    /**
     * 关注提醒检查
     */
    @GetMapping("/focus/alerts")
    public Result<Map<String, Object>> checkFocusAlerts(@RequestParam Integer caregiverId) {
        try {
            Map<String, Object> alerts = customerFocusService.checkFocusAlerts(caregiverId);
            return Result.success(alerts);
        } catch (Exception e) {
            return Result.error("检查关注提醒失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新关注状态
     */
    @PutMapping("/focus/batch-status")
    public Result<String> batchUpdateFocusStatus(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) data.get("ids");
            Integer status = (Integer) data.get("status");

            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的关注记录");
            }

            boolean success = customerFocusService.batchUpdateFocusStatus(ids, status);
            if (success) {
                return Result.success("批量更新关注状态成功");
            } else {
                return Result.error("批量更新关注状态失败");
            }
        } catch (Exception e) {
            return Result.error("批量更新关注状态失败：" + e.getMessage());
        }
    }
}