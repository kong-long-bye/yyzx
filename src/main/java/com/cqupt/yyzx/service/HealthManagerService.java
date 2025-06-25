package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.Customer;
import com.cqupt.yyzx.entity.CustomerCaregiverAssignment;
import com.cqupt.yyzx.entity.NursingRecord;
import com.cqupt.yyzx.mapper.CustomerMapper;
import com.cqupt.yyzx.mapper.CustomerCaregiverAssignmentMapper;
import com.cqupt.yyzx.mapper.NursingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 健康管家业务逻辑服务
 */
@Service
public class HealthManagerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerCaregiverAssignmentMapper assignmentMapper;

    @Autowired
    private NursingRecordMapper nursingRecordMapper;

    /**
     * 获取护理人员负责的客户列表（服务对象）
     */
    public Map<String, Object> getServiceCustomerList(Integer caregiverId, String searchKeyword,
                                                      String auditStatus, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        // 先获取护理人员负责的客户ID列表
        List<CustomerCaregiverAssignment> assignments = assignmentMapper.selectAssignmentsByCaregiverId(caregiverId);
        List<Integer> customerIds = assignments.stream()
                .map(CustomerCaregiverAssignment::getCustomerId)
                .collect(Collectors.toList());

        if (customerIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("customers",  Collections.emptyList());
            result.put("total", 0);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }

        // 查询客户详细信息
        Integer offset = (page - 1) * size;
        List<Customer> customers = customerMapper.selectCustomerListWithDetails(searchKeyword, auditStatus, 1, offset, size);

        // 过滤出该护理人员负责的客户
        customers = customers.stream()
                .filter(customer -> customerIds.contains(customer.getId()))
                .collect(Collectors.toList());

        Integer total = customers.size();

        Map<String, Object> result = new HashMap<>();
        result.put("customers", customers);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 获取护理人员的工作概览
     */
    public Map<String, Object> getCaregiverWorkOverview(Integer caregiverId) {
        Map<String, Object> overview = new HashMap<>();

        // 负责的客户总数
        List<CustomerCaregiverAssignment> assignments = assignmentMapper.selectAssignmentsByCaregiverId(caregiverId);
        int totalCustomers = assignments.size();

        // 主要负责的客户数
        int primaryCustomers = (int) assignments.stream()
                .filter(assignment -> assignment.getPrimaryCaregiver() == 1)
                .count();

        // 今日护理记录数
        List<NursingRecord> todayRecords = nursingRecordMapper.selectTodayRecords(caregiverId);
        int todayRecordCount = todayRecords.size();

        // 本周护理记录数
        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        List<NursingRecord> weekRecords = nursingRecordMapper.selectRecordsByCaregiverId(caregiverId, startOfWeek, endOfWeek);
        int weekRecordCount = weekRecords.size();

        // 未完成的护理记录数
        List<NursingRecord> uncompletedRecords = nursingRecordMapper.selectUncompletedRecords();
        int uncompletedCount = (int) uncompletedRecords.stream()
                .filter(record -> record.getCaregiverId().equals(caregiverId))
                .count();

        overview.put("totalCustomers", totalCustomers);
        overview.put("primaryCustomers", primaryCustomers);
        overview.put("todayRecordCount", todayRecordCount);
        overview.put("weekRecordCount", weekRecordCount);
        overview.put("uncompletedCount", uncompletedCount);

        return overview;
    }

    /**
     * 获取客户护理详情
     */
    public Map<String, Object> getCustomerCareDetail(Integer customerId, Integer caregiverId) {
        Map<String, Object> detail = new HashMap<>();

        // 客户基本信息
        Customer customer = customerMapper.selectCustomerById(customerId);
        detail.put("customer", customer);

        // 护理分配信息
        List<CustomerCaregiverAssignment> assignments = assignmentMapper.selectAssignmentsByCustomerId(customerId);
        CustomerCaregiverAssignment currentAssignment = assignments.stream()
                .filter(assignment -> assignment.getCaregiverId().equals(caregiverId) && assignment.getStatus() == 1)
                .findFirst()
                .orElse(null);
        detail.put("assignment", currentAssignment);

        // 最近的护理记录
        List<NursingRecord> recentRecords = nursingRecordMapper.selectRecordsByCustomerId(customerId, 10);
        detail.put("recentRecords", recentRecords);

        // 护理统计
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        List<Map<String, Object>> careStats = nursingRecordMapper.getCustomerCareStats(customerId, monthStart, LocalDate.now());
        detail.put("careStats", careStats);

        return detail;
    }

    /**
     * 申请服务新客户
     */
    @Transactional
    public boolean applyForNewCustomer(Integer caregiverId, Integer customerId, String reason) {
        try {
            // 检查客户是否存在且在住
            Customer customer = customerMapper.selectCustomerById(customerId);
            if (customer == null || customer.getStatus() != 1) {
                throw new RuntimeException("客户不存在或不在住状态");
            }

            // 检查是否已经分配
            if (assignmentMapper.checkAssignmentExists(customerId, caregiverId) > 0) {
                throw new RuntimeException("该客户已分配给您");
            }

            // 创建分配申请（待审核状态可以通过status字段控制）
            CustomerCaregiverAssignment assignment = new CustomerCaregiverAssignment();
            assignment.setCustomerId(customerId);
            assignment.setCaregiverId(caregiverId);
            assignment.setAssignmentDate(LocalDate.now());
            assignment.setPrimaryCaregiver(0); // 非主要护理人员
            assignment.setStatus(1); // 直接生效，或者可以设为待审核状态

            return assignmentMapper.insertAssignment(assignment) > 0;

        } catch (Exception e) {
            throw new RuntimeException("申请服务客户失败：" + e.getMessage());
        }
    }

    /**
     * 移交客户给其他护理人员
     */
    @Transactional
    public boolean transferCustomer(Integer customerId, Integer fromCaregiverId, Integer toCaregiverId, String reason) {
        try {
            // 检查原分配是否存在
            if (assignmentMapper.checkAssignmentExists(customerId, fromCaregiverId) == 0) {
                throw new RuntimeException("您未负责该客户");
            }

            // 检查目标护理人员是否已分配
            if (assignmentMapper.checkAssignmentExists(customerId, toCaregiverId) > 0) {
                throw new RuntimeException("目标护理人员已负责该客户");
            }

            // 获取原分配信息
            List<CustomerCaregiverAssignment> assignments = assignmentMapper.selectAssignmentsByCustomerId(customerId);
            CustomerCaregiverAssignment originalAssignment = assignments.stream()
                    .filter(assignment -> assignment.getCaregiverId().equals(fromCaregiverId) && assignment.getStatus() == 1)
                    .findFirst()
                    .orElse(null);

            if (originalAssignment == null) {
                throw new RuntimeException("未找到有效的分配记录");
            }

            // 终止原分配
            assignmentMapper.updateAssignmentStatus(originalAssignment.getId(), 0);

            // 创建新分配
            CustomerCaregiverAssignment newAssignment = new CustomerCaregiverAssignment();
            newAssignment.setCustomerId(customerId);
            newAssignment.setCaregiverId(toCaregiverId);
            newAssignment.setAssignmentDate(LocalDate.now());
            newAssignment.setPrimaryCaregiver(originalAssignment.getPrimaryCaregiver()); // 保持原有主要护理人员状态
            newAssignment.setStatus(1);

            return assignmentMapper.insertAssignment(newAssignment) > 0;

        } catch (Exception e) {
            throw new RuntimeException("移交客户失败：" + e.getMessage());
        }
    }

    /**
     * 申请成为客户的主要护理人员
     */
    @Transactional
    public boolean applyForPrimaryCaregiver(Integer customerId, Integer caregiverId) {
        try {
            // 检查是否已分配给该护理人员
            if (assignmentMapper.checkAssignmentExists(customerId, caregiverId) == 0) {
                throw new RuntimeException("您未负责该客户");
            }

            // 设置为主要护理人员
            return assignmentMapper.setPrimaryCaregiver(customerId, caregiverId) > 0;

        } catch (Exception e) {
            throw new RuntimeException("申请主要护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 获取可申请的客户列表
     */
    public List<Customer> getAvailableCustomersForApplication(Integer caregiverId) {
        // 获取所有在住客户
        List<Customer> allCustomers = customerMapper.selectCustomerListWithDetails(null, "已通过", 1, null, null);

        // 获取护理人员已分配的客户ID
        List<CustomerCaregiverAssignment> assignments = assignmentMapper.selectAssignmentsByCaregiverId(caregiverId);
        List<Integer> assignedCustomerIds = assignments.stream()
                .map(CustomerCaregiverAssignment::getCustomerId)
                .collect(Collectors.toList());

        // 过滤出未分配给该护理人员的客户
        return allCustomers.stream()
                .filter(customer -> !assignedCustomerIds.contains(customer.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取护理人员工作统计
     */
    public Map<String, Object> getCaregiverWorkStats(Integer caregiverId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        Map<String, Object> stats = new HashMap<>();

        // 护理记录统计
        List<NursingRecord> records = nursingRecordMapper.selectRecordsByCaregiverId(caregiverId, startDate, endDate);
        int totalRecords = records.size();
        int completedRecords = (int) records.stream()
                .filter(record -> "已完成".equals(record.getExecutionStatus()))
                .count();

        // 工作时长统计
        int totalDuration = records.stream()
                .mapToInt(record -> record.getDuration() != null ? record.getDuration() : 0)
                .sum();

        // 客户满意度（这里简化处理，实际可以通过满意度调查获取）
        double satisfactionRate = totalRecords > 0 ? (double) completedRecords / totalRecords * 100 : 0;

        stats.put("totalRecords", totalRecords);
        stats.put("completedRecords", completedRecords);
        stats.put("completionRate", totalRecords > 0 ? (double) completedRecords / totalRecords * 100 : 0);
        stats.put("totalDuration", totalDuration);
        stats.put("averageDuration", totalRecords > 0 ? totalDuration / totalRecords : 0);
        stats.put("satisfactionRate", satisfactionRate);

        return stats;
    }

    /**
     * 获取客户关怀建议
     */
    public Map<String, Object> getCustomerCareSuggestions(Integer customerId, Integer caregiverId) {
        Map<String, Object> suggestions = new HashMap<>();

        // 获取客户信息
        Customer customer = customerMapper.selectCustomerById(customerId);
        suggestions.put("customer", customer);

        // 获取最近护理记录
        List<NursingRecord> recentRecords = nursingRecordMapper.selectRecordsByCustomerId(customerId, 5);
        suggestions.put("recentRecords", recentRecords);

        // 基于护理记录生成建议
        List<String> careSuggestions = generateCareSuggestions(customer, recentRecords);
        suggestions.put("suggestions", careSuggestions);

        return suggestions;
    }

    /**
     * 生成护理建议
     */
    private List<String> generateCareSuggestions(Customer customer, List<NursingRecord> recentRecords) {
        List<String> suggestions = Arrays.asList(
                "建议定期检查客户的生命体征",
                "注意观察客户的情绪变化",
                "确保客户按时服药",
                "鼓励客户参加适度的康复活动",
                "与客户家属保持良好沟通"
        );

        // 这里可以根据客户的具体情况和护理记录生成个性化建议
        return suggestions;
    }

    /**
     * 获取团队协作信息
     */
    public Map<String, Object> getTeamCollaborationInfo(Integer customerId) {
        Map<String, Object> teamInfo = new HashMap<>();

        // 获取所有负责该客户的护理人员
        List<CustomerCaregiverAssignment> assignments = assignmentMapper.selectAssignmentsByCustomerId(customerId);
        teamInfo.put("assignments", assignments);

        // 获取主要护理人员
        CustomerCaregiverAssignment primaryCaregiver = assignmentMapper.selectPrimaryCaregiverByCustomerId(customerId);
        teamInfo.put("primaryCaregiver", primaryCaregiver);

        // 获取团队护理记录
        LocalDate weekStart = LocalDate.now().minusDays(7);
        List<NursingRecord> teamRecords = nursingRecordMapper.selectRecordsByCustomerId(customerId, null)
                .stream()
                .filter(record -> record.getExecutionDate().isAfter(weekStart))
                .collect(Collectors.toList());
        teamInfo.put("teamRecords", teamRecords);

        return teamInfo;
    }

    /**
     * 检查护理人员权限
     */
    public boolean checkCaregiverPermission(Integer customerId, Integer caregiverId) {
        return assignmentMapper.checkAssignmentExists(customerId, caregiverId) > 0;
    }

    /**
     * 获取工作提醒
     */
    public Map<String, Object> getWorkReminders(Integer caregiverId) {
        Map<String, Object> reminders = new HashMap<>();

        // 今日未完成的护理记录
        List<NursingRecord> todayUncompleted = nursingRecordMapper.selectTodayRecords(caregiverId)
                .stream()
                .filter(record -> !"已完成".equals(record.getExecutionStatus()))
                .collect(Collectors.toList());
        reminders.put("todayUncompleted", todayUncompleted);

        // 即将到期的护理协议
        List<Customer> expiringAgreements = customerMapper.selectCustomersWithExpiredAgreements(7);
        List<CustomerCaregiverAssignment> assignments = assignmentMapper.selectAssignmentsByCaregiverId(caregiverId);
        List<Integer> customerIds = assignments.stream()
                .map(CustomerCaregiverAssignment::getCustomerId)
                .collect(Collectors.toList());

        List<Customer> myExpiringAgreements = expiringAgreements.stream()
                .filter(customer -> customerIds.contains(customer.getId()))
                .collect(Collectors.toList());
        reminders.put("expiringAgreements", myExpiringAgreements);

        return reminders;
    }
}