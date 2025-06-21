package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.Customer;
import com.cqupt.yyzx.entity.OutingRecord;
import com.cqupt.yyzx.entity.CheckoutInfo;
import com.cqupt.yyzx.entity.NursingAgreement;
import com.cqupt.yyzx.service.CustomerService;
import com.cqupt.yyzx.service.OutingRecordService;
import com.cqupt.yyzx.service.CheckoutInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 客户管理控制器
 */
@RestController
@RequestMapping("/api/customer")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OutingRecordService outingRecordService;

    @Autowired
    private CheckoutInfoService checkoutInfoService;

    // ==================== 客户管理基础功能 ====================

    /**
     * 分页查询客户列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getCustomerList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = customerService.getCustomerList(searchKeyword, auditStatus, status, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询客户列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询客户详细信息
     */
    @GetMapping("/{id}")
    public Result<Customer> getCustomerById(@PathVariable Integer id) {
        try {
            Customer customer = customerService.getCustomerById(id);
            if (customer != null) {
                return Result.success(customer);
            } else {
                return Result.error("客户不存在");
            }
        } catch (Exception e) {
            return Result.error("查询客户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新客户信息
     */
    @PutMapping("/update")
    public Result<String> updateCustomer(@RequestBody Customer customer) {
        try {
            if (customer.getId() == null) {
                return Result.error("客户ID不能为空");
            }

            boolean success = customerService.updateCustomer(customer);
            if (success) {
                return Result.success("更新客户信息成功");
            } else {
                return Result.error("身份证号已存在");
            }
        } catch (Exception e) {
            return Result.error("更新客户信息失败：" + e.getMessage());
        }
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCustomer(@PathVariable Integer id) {
        try {
            boolean success = customerService.deleteCustomer(id);
            if (success) {
                return Result.success("删除客户成功");
            } else {
                return Result.error("删除客户失败");
            }
        } catch (Exception e) {
            return Result.error("删除客户失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除客户
     */
    @DeleteMapping("/batch")
    public Result<String> deleteCustomers(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的客户");
            }

            boolean success = customerService.deleteCustomers(ids);
            if (success) {
                return Result.success("批量删除客户成功");
            } else {
                return Result.error("批量删除客户失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除客户失败：" + e.getMessage());
        }
    }

    /**
     * 更新客户审核状态
     */
    @PutMapping("/{id}/audit-status")
    public Result<String> updateCustomerAuditStatus(@PathVariable Integer id, @RequestParam String auditStatus) {
        try {
            boolean success = customerService.updateCustomerAuditStatus(id, auditStatus);
            if (success) {
                return Result.success("更新审核状态成功");
            } else {
                return Result.error("更新审核状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新审核状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getCustomerStats() {
        try {
            Map<String, Object> stats = customerService.getCustomerStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取客户统计失败：" + e.getMessage());
        }
    }

    /**
     * 检查身份证号是否可用
     */
    @GetMapping("/check-idcard")
    public Result<Boolean> checkIdCard(@RequestParam String idCard,
                                       @RequestParam(required = false) Integer excludeId) {
        try {
            boolean available = customerService.isIdCardAvailable(idCard, excludeId);
            return Result.success(available);
        } catch (Exception e) {
            return Result.error("检查身份证号失败：" + e.getMessage());
        }
    }

    // ==================== 入住登记功能 ====================

    /**
     * 客户入住登记
     */
    @PostMapping("/checkin")
    public Result<String> checkinCustomer(@RequestBody Map<String, Object> checkinData) {
        try {
            // 解析客户信息
            Customer customer = parseCustomerFromMap(checkinData);

            // 解析护理协议信息
            NursingAgreement agreement = parseAgreementFromMap(checkinData);

            // 解析护理人员分配
            @SuppressWarnings("unchecked")
            List<Integer> caregiverIds = (List<Integer>) checkinData.get("caregiverIds");

            boolean success = customerService.checkinCustomer(customer, agreement, caregiverIds);
            if (success) {
                return Result.success("入住登记成功");
            } else {
                return Result.error("入住登记失败");
            }
        } catch (Exception e) {
            return Result.error("入住登记失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用床位列表
     */
    @GetMapping("/available-beds")
    public Result<List<Map<String, Object>>> getAvailableBeds() {
        try {
            List<Map<String, Object>> beds = customerService.getAvailableBeds();
            return Result.success(beds);
        } catch (Exception e) {
            return Result.error("查询可用床位失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理人员列表
     */
    @GetMapping("/caregivers")
    public Result<List<Map<String, Object>>> getCaregiversList() {
        try {
            List<Map<String, Object>> caregivers = customerService.getCaregiversList();
            return Result.success(caregivers);
        } catch (Exception e) {
            return Result.error("查询护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 查询待审核的客户列表
     */
    @GetMapping("/pending-audit")
    public Result<List<Customer>> getPendingAuditCustomers() {
        try {
            List<Customer> customers = customerService.getPendingAuditCustomers();
            return Result.success(customers);
        } catch (Exception e) {
            return Result.error("查询待审核客户失败：" + e.getMessage());
        }
    }

    // ==================== 外出登记功能 ====================

    /**
     * 分页查询外出记录列表
     */
    @GetMapping("/outing/list")
    public Result<Map<String, Object>> getOutingRecordList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate outingDate,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = outingRecordService.getOutingRecordList(
                    searchKeyword, approvalStatus, outingDate, customerId, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询外出记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询外出记录详细信息
     */
    @GetMapping("/outing/{id}")
    public Result<OutingRecord> getOutingRecordById(@PathVariable Integer id) {
        try {
            OutingRecord record = outingRecordService.getOutingRecordById(id);
            if (record != null) {
                return Result.success(record);
            } else {
                return Result.error("外出记录不存在");
            }
        } catch (Exception e) {
            return Result.error("查询外出记录失败：" + e.getMessage());
        }
    }

    /**
     * 添加外出记录
     */
    @PostMapping("/outing/add")
    public Result<String> addOutingRecord(@RequestBody OutingRecord outingRecord) {
        try {
            // 基本验证
            if (outingRecord.getCustomerId() == null) {
                return Result.error("客户不能为空");
            }
            if (outingRecord.getOutingTime() == null) {
                return Result.error("外出时间不能为空");
            }

            boolean success = outingRecordService.addOutingRecord(outingRecord);
            if (success) {
                return Result.success("添加外出记录成功");
            } else {
                return Result.error("添加外出记录失败");
            }
        } catch (Exception e) {
            return Result.error("添加外出记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新外出记录信息
     */
    @PutMapping("/outing/update")
    public Result<String> updateOutingRecord(@RequestBody OutingRecord outingRecord) {
        try {
            if (outingRecord.getId() == null) {
                return Result.error("外出记录ID不能为空");
            }

            boolean success = outingRecordService.updateOutingRecord(outingRecord);
            if (success) {
                return Result.success("更新外出记录成功");
            } else {
                return Result.error("更新外出记录失败");
            }
        } catch (Exception e) {
            return Result.error("更新外出记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除外出记录
     */
    @DeleteMapping("/outing/{id}")
    public Result<String> deleteOutingRecord(@PathVariable Integer id) {
        try {
            boolean success = outingRecordService.deleteOutingRecord(id);
            if (success) {
                return Result.success("删除外出记录成功");
            } else {
                return Result.error("删除外出记录失败");
            }
        } catch (Exception e) {
            return Result.error("删除外出记录失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除外出记录
     */
    @DeleteMapping("/outing/batch")
    public Result<String> deleteOutingRecords(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的外出记录");
            }

            boolean success = outingRecordService.deleteOutingRecords(ids);
            if (success) {
                return Result.success("批量删除外出记录成功");
            } else {
                return Result.error("批量删除外出记录失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除外出记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新外出记录审批状态
     */
    @PutMapping("/outing/{id}/approval-status")
    public Result<String> updateOutingRecordApprovalStatus(@PathVariable Integer id,
                                                           @RequestParam String approvalStatus) {
        try {
            boolean success = outingRecordService.updateOutingRecordApprovalStatus(id, approvalStatus);
            if (success) {
                return Result.success("更新审批状态成功");
            } else {
                return Result.error("更新审批状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新审批状态失败：" + e.getMessage());
        }
    }

    /**
     * 登记返回时间
     */
    @PutMapping("/outing/{id}/return")
    public Result<String> updateActualReturnTime(@PathVariable Integer id,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime actualReturnTime) {
        try {
            if (actualReturnTime == null) {
                actualReturnTime = LocalTime.now();
            }

            boolean success = outingRecordService.updateActualReturnTime(id, actualReturnTime);
            if (success) {
                return Result.success("登记返回时间成功");
            } else {
                return Result.error("登记返回时间失败");
            }
        } catch (Exception e) {
            return Result.error("登记返回时间失败：" + e.getMessage());
        }
    }

    /**
     * 快速返回登记
     */
    @PutMapping("/outing/{id}/quick-return")
    public Result<String> quickReturn(@PathVariable Integer id) {
        try {
            boolean success = outingRecordService.quickReturn(id);
            if (success) {
                return Result.success("快速返回登记成功");
            } else {
                return Result.error("快速返回登记失败");
            }
        } catch (Exception e) {
            return Result.error("快速返回登记失败：" + e.getMessage());
        }
    }

    /**
     * 查询未返回的外出记录
     */
    @GetMapping("/outing/unreturned")
    public Result<List<OutingRecord>> getUnreturnedOutingRecords() {
        try {
            List<OutingRecord> records = outingRecordService.getUnreturnedOutingRecords();
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询未返回记录失败：" + e.getMessage());
        }
    }

    /**
     * 查询待审批的外出申请
     */
    @GetMapping("/outing/pending-approval")
    public Result<List<OutingRecord>> getPendingApprovalOutingRecords() {
        try {
            List<OutingRecord> records = outingRecordService.getPendingApprovalOutingRecords();
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询待审批记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取外出记录统计信息
     */
    @GetMapping("/outing/stats")
    public Result<Map<String, Object>> getOutingRecordStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> stats = outingRecordService.getOutingRecordStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取外出统计失败：" + e.getMessage());
        }
    }

    /**
     * 批量审批外出申请
     */
    @PutMapping("/outing/batch-approve")
    public Result<String> batchApproveOutingRecords(@RequestBody Map<String, Object> approvalData) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) approvalData.get("ids");
            String approvalStatus = (String) approvalData.get("approvalStatus");

            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要审批的记录");
            }

            boolean success = outingRecordService.batchApproveOutingRecords(ids, approvalStatus);
            if (success) {
                return Result.success("批量审批成功");
            } else {
                return Result.error("批量审批失败");
            }
        } catch (Exception e) {
            return Result.error("批量审批失败：" + e.getMessage());
        }
    }

    // ==================== 退住登记功能 ====================

    /**
     * 分页查询退住信息列表
     */
    @GetMapping("/checkout/list")
    public Result<Map<String, Object>> getCheckoutInfoList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String checkoutType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = checkoutInfoService.getCheckoutInfoList(
                    searchKeyword, checkoutType, startDate, endDate, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询退住信息失败：" + e.getMessage());
        }
    }

    /**
     * 客户退住登记
     */
    @PostMapping("/checkout")
    public Result<String> checkoutCustomer(@RequestBody Map<String, Object> checkoutData) {
        try {
            Integer customerId = (Integer) checkoutData.get("customerId");
            if (customerId == null) {
                return Result.error("客户ID不能为空");
            }

            CheckoutInfo checkoutInfo = parseCheckoutInfoFromMap(checkoutData);

            boolean success = checkoutInfoService.checkoutCustomer(customerId, checkoutInfo);
            if (success) {
                return Result.success("退住登记成功");
            } else {
                return Result.error("退住登记失败");
            }
        } catch (Exception e) {
            return Result.error("退住登记失败：" + e.getMessage());
        }
    }

    /**
     * 获取退住统计信息
     */
    @GetMapping("/checkout/stats")
    public Result<Map<String, Object>> getCheckoutStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> stats = checkoutInfoService.getCheckoutStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取退住统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取退住类型选项
     */
    @GetMapping("/checkout/types")
    public Result<List<String>> getCheckoutTypes() {
        try {
            List<String> types = checkoutInfoService.getCheckoutTypes();
            return Result.success(types);
        } catch (Exception e) {
            return Result.error("获取退住类型失败：" + e.getMessage());
        }
    }

    /**
     * 批量退住操作
     */
    @PostMapping("/checkout/batch")
    public Result<String> batchCheckoutCustomers(@RequestBody Map<String, Object> checkoutData) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> customerIds = (List<Integer>) checkoutData.get("customerIds");
            if (customerIds == null || customerIds.isEmpty()) {
                return Result.error("请选择要退住的客户");
            }

            CheckoutInfo checkoutInfo = parseCheckoutInfoFromMap(checkoutData);

            boolean success = checkoutInfoService.batchCheckoutCustomers(customerIds, checkoutInfo);
            if (success) {
                return Result.success("批量退住成功");
            } else {
                return Result.error("批量退住失败");
            }
        } catch (Exception e) {
            return Result.error("批量退住失败：" + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 从Map中解析Customer对象
     */
    private Customer parseCustomerFromMap(Map<String, Object> data) {
        Customer customer = new Customer();
        customer.setName((String) data.get("name"));
        customer.setIdCard((String) data.get("idCard"));
        customer.setGender((String) data.get("gender"));

        if (data.get("birthDate") != null) {
            customer.setBirthDate(LocalDate.parse((String) data.get("birthDate")));
        }

        customer.setPhone((String) data.get("phone"));
        customer.setEmergencyContact((String) data.get("emergencyContact"));
        customer.setEmergencyPhone((String) data.get("emergencyPhone"));
        customer.setAddress((String) data.get("address"));
        customer.setMedicalHistory((String) data.get("medicalHistory"));
        customer.setAllergies((String) data.get("allergies"));

        if (data.get("bedId") != null) {
            customer.setBedId((Integer) data.get("bedId"));
        }

        if (data.get("admissionDate") != null) {
            customer.setAdmissionDate(LocalDate.parse((String) data.get("admissionDate")));
        }

        return customer;
    }

    /**
     * 从Map中解析NursingAgreement对象
     */
    private NursingAgreement parseAgreementFromMap(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> agreementData = (Map<String, Object>) data.get("agreement");
        if (agreementData == null) {
            return null;
        }

        NursingAgreement agreement = new NursingAgreement();
        agreement.setLevelName((String) agreementData.get("levelName"));
        agreement.setLevelCode((String) agreementData.get("levelCode"));

        if (agreementData.get("monthlyFee") != null) {
            agreement.setMonthlyFee(new java.math.BigDecimal(agreementData.get("monthlyFee").toString()));
        }

        agreement.setServiceContent((String) agreementData.get("serviceContent"));

        if (agreementData.get("startDate") != null) {
            agreement.setStartDate(LocalDate.parse((String) agreementData.get("startDate")));
        }

        return agreement;
    }

    /**
     * 从Map中解析CheckoutInfo对象
     */
    private CheckoutInfo parseCheckoutInfoFromMap(Map<String, Object> data) {
        CheckoutInfo checkoutInfo = new CheckoutInfo();
        checkoutInfo.setCheckoutType((String) data.get("checkoutType"));
        checkoutInfo.setCheckoutReason((String) data.get("checkoutReason"));

        if (data.get("checkoutDate") != null) {
            checkoutInfo.setCheckoutDate(LocalDate.parse((String) data.get("checkoutDate")));
        }

        checkoutInfo.setRemarks((String) data.get("remarks"));

        return checkoutInfo;
    }
}