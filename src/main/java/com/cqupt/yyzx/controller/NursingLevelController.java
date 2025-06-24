package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.NursingAgreement;
import com.cqupt.yyzx.service.NursingLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 护理级别控制器
 */
@RestController
@RequestMapping("/api/nursing/level")
@CrossOrigin
public class NursingLevelController {

    @Autowired
    private NursingLevelService levelService;

    /**
     * 获取护理等级选项
     */
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getNursingLevelOptions() {
        try {
            List<Map<String, Object>> options = levelService.getNursingLevelOptions();
            return Result.success(options);
        } catch (Exception e) {
            return Result.error("查询护理等级选项失败：" + e.getMessage());
        }
    }

    /**
     * 根据客户ID查询有效的护理协议
     */
    @GetMapping("/customer/{customerId}/active")
    public Result<NursingAgreement> getActiveAgreementByCustomerId(@PathVariable Integer customerId) {
        try {
            NursingAgreement agreement = levelService.getActiveAgreementByCustomerId(customerId);
            return Result.success(agreement);
        } catch (Exception e) {
            return Result.error("查询客户护理协议失败：" + e.getMessage());
        }
    }

    /**
     * 根据客户ID查询所有护理协议
     */
    @GetMapping("/customer/{customerId}/all")
    public Result<List<NursingAgreement>> getAgreementsByCustomerId(@PathVariable Integer customerId) {
        try {
            List<NursingAgreement> agreements = levelService.getAgreementsByCustomerId(customerId);
            return Result.success(agreements);
        } catch (Exception e) {
            return Result.error("查询客户护理协议失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询护理协议详细信息
     */
    @GetMapping("/{id}")
    public Result<NursingAgreement> getAgreementById(@PathVariable Integer id) {
        try {
            NursingAgreement agreement = levelService.getAgreementById(id);
            if (agreement != null) {
                return Result.success(agreement);
            } else {
                return Result.error("护理协议不存在");
            }
        } catch (Exception e) {
            return Result.error("查询护理协议信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建护理协议
     */
    @PostMapping("/create")
    public Result<String> createAgreement(@RequestBody NursingAgreement agreement) {
        try {
            // 基本验证
            if (agreement.getCustomerId() == null) {
                return Result.error("客户不能为空");
            }
            if (agreement.getLevelName() == null || agreement.getLevelName().trim().isEmpty()) {
                return Result.error("护理等级名称不能为空");
            }
            if (agreement.getLevelCode() == null || agreement.getLevelCode().trim().isEmpty()) {
                return Result.error("护理等级编码不能为空");
            }
            if (agreement.getMonthlyFee() == null) {
                return Result.error("月费用不能为空");
            }

            boolean success = levelService.createAgreement(agreement);
            if (success) {
                return Result.success("创建护理协议成功");
            } else {
                return Result.error("创建护理协议失败");
            }
        } catch (Exception e) {
            return Result.error("创建护理协议失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理协议信息
     */
    @PutMapping("/update")
    public Result<String> updateAgreement(@RequestBody NursingAgreement agreement) {
        try {
            if (agreement.getId() == null) {
                return Result.error("护理协议ID不能为空");
            }

            boolean success = levelService.updateAgreement(agreement);
            if (success) {
                return Result.success("更新护理协议成功");
            } else {
                return Result.error("更新护理协议失败");
            }
        } catch (Exception e) {
            return Result.error("更新护理协议失败：" + e.getMessage());
        }
    }

    /**
     * 删除护理协议
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteAgreement(@PathVariable Integer id) {
        try {
            boolean success = levelService.deleteAgreement(id);
            if (success) {
                return Result.success("删除护理协议成功");
            } else {
                return Result.error("删除护理协议失败");
            }
        } catch (Exception e) {
            return Result.error("删除护理协议失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理协议状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateAgreementStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            boolean success = levelService.updateAgreementStatus(id, status);
            if (success) {
                return Result.success("更新协议状态成功");
            } else {
                return Result.error("更新协议状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新协议状态失败：" + e.getMessage());
        }
    }

    /**
     * 终止客户的所有有效协议
     */
    @PutMapping("/customer/{customerId}/terminate")
    public Result<String> terminateCustomerAgreements(@PathVariable Integer customerId) {
        try {
            boolean success = levelService.terminateCustomerAgreements(customerId);
            if (success) {
                return Result.success("终止客户协议成功");
            } else {
                return Result.error("终止客户协议失败");
            }
        } catch (Exception e) {
            return Result.error("终止客户协议失败：" + e.getMessage());
        }
    }

    /**
     * 查询即将到期的协议
     */
    @GetMapping("/expiring")
    public Result<List<NursingAgreement>> getExpiringAgreements(@RequestParam(required = false) Integer days) {
        try {
            List<NursingAgreement> agreements = levelService.getExpiringAgreements(days);
            return Result.success(agreements);
        } catch (Exception e) {
            return Result.error("查询即将到期协议失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理协议统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAgreementStats() {
        try {
            Map<String, Object> stats = levelService.getAgreementStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取协议统计失败：" + e.getMessage());
        }
    }

    /**
     * 护理等级升级
     */
    @PostMapping("/upgrade")
    public Result<String> upgradeNursingLevel(@RequestBody Map<String, Object> upgradeData) {
        try {
            Integer customerId = (Integer) upgradeData.get("customerId");
            String newLevelCode = (String) upgradeData.get("newLevelCode");
            String newLevelName = (String) upgradeData.get("newLevelName");
            BigDecimal newMonthlyFee = new BigDecimal(upgradeData.get("newMonthlyFee").toString());
            String serviceContent = (String) upgradeData.get("serviceContent");
            String reason = (String) upgradeData.get("reason");

            if (customerId == null || newLevelCode == null || newLevelName == null || newMonthlyFee == null) {
                return Result.error("必填参数不能为空");
            }

            boolean success = levelService.upgradeNursingLevel(customerId, newLevelCode, newLevelName,
                    newMonthlyFee, serviceContent, reason);
            if (success) {
                return Result.success("护理等级升级成功");
            } else {
                return Result.error("护理等级升级失败");
            }
        } catch (Exception e) {
            return Result.error("护理等级升级失败：" + e.getMessage());
        }
    }

    /**
     * 护理等级降级
     */
    @PostMapping("/downgrade")
    public Result<String> downgradeNursingLevel(@RequestBody Map<String, Object> downgradeData) {
        try {
            Integer customerId = (Integer) downgradeData.get("customerId");
            String newLevelCode = (String) downgradeData.get("newLevelCode");
            String newLevelName = (String) downgradeData.get("newLevelName");
            BigDecimal newMonthlyFee = new BigDecimal(downgradeData.get("newMonthlyFee").toString());
            String serviceContent = (String) downgradeData.get("serviceContent");
            String reason = (String) downgradeData.get("reason");

            if (customerId == null || newLevelCode == null || newLevelName == null || newMonthlyFee == null) {
                return Result.error("必填参数不能为空");
            }

            boolean success = levelService.downgradeNursingLevel(customerId, newLevelCode, newLevelName,
                    newMonthlyFee, serviceContent, reason);
            if (success) {
                return Result.success("护理等级降级成功");
            } else {
                return Result.error("护理等级降级失败");
            }
        } catch (Exception e) {
            return Result.error("护理等级降级失败：" + e.getMessage());
        }
    }

    /**
     * 暂停护理服务
     */
    @PostMapping("/suspend")
    public Result<String> suspendNursingService(@RequestBody Map<String, Object> suspendData) {
        try {
            Integer customerId = (Integer) suspendData.get("customerId");
            String reason = (String) suspendData.get("reason");

            if (customerId == null) {
                return Result.error("客户ID不能为空");
            }

            boolean success = levelService.suspendNursingService(customerId, reason);
            if (success) {
                return Result.success("暂停护理服务成功");
            } else {
                return Result.error("暂停护理服务失败");
            }
        } catch (Exception e) {
            return Result.error("暂停护理服务失败：" + e.getMessage());
        }
    }

    /**
     * 恢复护理服务
     */
    @PostMapping("/resume")
    public Result<String> resumeNursingService(@RequestParam Integer customerId) {
        try {
            boolean success = levelService.resumeNursingService(customerId);
            if (success) {
                return Result.success("恢复护理服务成功");
            } else {
                return Result.error("恢复护理服务失败");
            }
        } catch (Exception e) {
            return Result.error("恢复护理服务失败：" + e.getMessage());
        }
    }

    /**
     * 续签护理协议
     */
    @PostMapping("/renew")
    public Result<String> renewAgreement(@RequestBody Map<String, Object> renewData) {
        try {
            Integer customerId = (Integer) renewData.get("customerId");
            LocalDate newEndDate = LocalDate.parse((String) renewData.get("newEndDate"));
            BigDecimal newMonthlyFee = null;

            if (renewData.get("newMonthlyFee") != null) {
                newMonthlyFee = new BigDecimal(renewData.get("newMonthlyFee").toString());
            }

            if (customerId == null || newEndDate == null) {
                return Result.error("客户ID和结束日期不能为空");
            }

            boolean success = levelService.renewAgreement(customerId, newEndDate, newMonthlyFee);
            if (success) {
                return Result.success("续签协议成功");
            } else {
                return Result.error("续签协议失败");
            }
        } catch (Exception e) {
            return Result.error("续签协议失败：" + e.getMessage());
        }
    }

    /**
     * 护理等级变更历史
     */
    @GetMapping("/customer/{customerId}/history")
    public Result<List<NursingAgreement>> getNursingLevelHistory(@PathVariable Integer customerId) {
        try {
            List<NursingAgreement> history = levelService.getNursingLevelHistory(customerId);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("查询护理等级历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理等级分布统计
     */
    @GetMapping("/distribution")
    public Result<Map<String, Object>> getNursingLevelDistribution() {
        try {
            Map<String, Object> distribution = levelService.getNursingLevelDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error("获取护理等级分布失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理等级详情
     */
    @GetMapping("/detail/{levelCode}")
    public Result<Map<String, Object>> getNursingLevelDetail(@PathVariable String levelCode) {
        try {
            Map<String, Object> detail = levelService.getNursingLevelDetail(levelCode);
            if (detail != null) {
                return Result.success(detail);
            } else {
                return Result.error("护理等级不存在");
            }
        } catch (Exception e) {
            return Result.error("查询护理等级详情失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新协议状态
     */
    @PutMapping("/batch-status")
    public Result<String> batchUpdateAgreementStatus(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) data.get("ids");
            String status = (String) data.get("status");

            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的协议");
            }

            boolean success = levelService.batchUpdateAgreementStatus(ids, status);
            if (success) {
                return Result.success("批量更新状态成功");
            } else {
                return Result.error("批量更新状态失败");
            }
        } catch (Exception e) {
            return Result.error("批量更新状态失败：" + e.getMessage());
        }
    }
}