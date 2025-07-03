package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.CustomerCaregiverAssignment;
import com.cqupt.yyzx.mapper.CustomerCaregiverAssignmentMapper;
import com.cqupt.yyzx.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户护理设置业务逻辑服务
 */
@Service
public class CustomerCareService {

    @Autowired
    private CustomerCaregiverAssignmentMapper assignmentMapper;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 根据客户ID查询护理人员分配
     */
    public List<CustomerCaregiverAssignment> getAssignmentsByCustomerId(Integer customerId) {
        return assignmentMapper.selectAssignmentsByCustomerId(customerId);
    }

    /**
     * 根据护理人员ID查询客户分配
     */
    public List<CustomerCaregiverAssignment> getAssignmentsByCaregiverId(Integer caregiverId) {
        return assignmentMapper.selectAssignmentsByCaregiverId(caregiverId);
    }

    /**
     * 根据ID查询分配详细信息
     */
    public CustomerCaregiverAssignment getAssignmentById(Integer id) {
        return assignmentMapper.selectAssignmentById(id);
    }

    /**
     * 创建护理人员分配
     */
    @Transactional
    public boolean createAssignment(CustomerCaregiverAssignment assignment) {
        try {
            // 检查是否已存在相同的分配
            if (assignmentMapper.checkAssignmentExists(assignment.getCustomerId(), assignment.getCaregiverId()) > 0) {
                throw new RuntimeException("该护理人员已分配给此客户");
            }

            // 设置默认值
            if (assignment.getAssignmentDate() == null) {
                assignment.setAssignmentDate(LocalDate.now());
            }
            if (assignment.getStatus() == null) {
                assignment.setStatus(1); // 默认有效
            }
            if (assignment.getPrimaryCaregiver() == null) {
                assignment.setPrimaryCaregiver(0); // 默认非主要护理人员
            }

            return assignmentMapper.insertAssignment(assignment) > 0;

        } catch (Exception e) {
            throw new RuntimeException("创建护理分配失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理人员分配
     */
    @Transactional
    public boolean updateAssignment(CustomerCaregiverAssignment assignment) {
        return assignmentMapper.updateAssignment(assignment) > 0;
    }

    /**
     * 删除护理人员分配
     */
    @Transactional
    public boolean deleteAssignment(Integer id) {
        return assignmentMapper.deleteAssignment(id) > 0;
    }

    /**
     * 更新分配状态
     */
    @Transactional
    public boolean updateAssignmentStatus(Integer id, Integer status) {
        return assignmentMapper.updateAssignmentStatus(id, status) > 0;
    }

    /**
     * 终止客户的所有护理人员分配
     */
    @Transactional
    public boolean terminateCustomerAssignments(Integer customerId) {
        return assignmentMapper.terminateCustomerAssignments(customerId) > 0;
    }

    /**
     * 查询客户的主要护理人员
     */
    public CustomerCaregiverAssignment getPrimaryCaregiverByCustomerId(Integer customerId) {
        return assignmentMapper.selectPrimaryCaregiverByCustomerId(customerId);
    }

    /**
     * 设置主要护理人员
     */
    @Transactional
    public boolean setPrimaryCaregiver(Integer customerId, Integer caregiverId) {
        try {
            // 检查分配是否存在
            if (assignmentMapper.checkAssignmentExists(customerId, caregiverId) == 0) {
                throw new RuntimeException("该护理人员未分配给此客户");
            }

            return assignmentMapper.setPrimaryCaregiver(customerId, caregiverId) > 0;

        } catch (Exception e) {
            throw new RuntimeException("设置主要护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 取消主要护理人员设置
     */
    @Transactional
    public boolean unsetPrimaryCaregiver(Integer customerId) {
        return assignmentMapper.unsetPrimaryCaregiver(customerId) > 0;
    }

    /**
     * 批量分配护理人员
     */
    @Transactional
    public boolean batchAssignCaregivers(Integer customerId, List<Integer> caregiverIds, Integer primaryCaregiverId) {
        try {
            // 先终止当前的分配
            terminateCustomerAssignments(customerId);

            // 创建新的分配
            for (Integer caregiverId : caregiverIds) {
                CustomerCaregiverAssignment assignment = new CustomerCaregiverAssignment();
                assignment.setCustomerId(customerId);
                assignment.setCaregiverId(caregiverId);
                assignment.setAssignmentDate(LocalDate.now());
                assignment.setStatus(1);
                assignment.setPrimaryCaregiver(caregiverId.equals(primaryCaregiverId) ? 1 : 0);

                assignmentMapper.insertAssignment(assignment);
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException("批量分配护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 更换护理人员
     */
    @Transactional
    public boolean replaceCaregivers(Integer customerId, List<Integer> newCaregiverIds, Integer newPrimaryCaregiverId) {
        try {
            return batchAssignCaregivers(customerId, newCaregiverIds, newPrimaryCaregiverId);
        } catch (Exception e) {
            throw new RuntimeException("更换护理人员失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理人员工作负载统计
     */
    public List<Map<String, Object>> getCaregiverWorkloadStats() {
        return assignmentMapper.getCaregiverWorkloadStats();
    }

    /**
     * 查询分配历史记录
     */
    public List<CustomerCaregiverAssignment> getAssignmentHistory(Integer customerId, Integer limit) {
        if (limit == null) limit = 20;
        return assignmentMapper.selectAssignmentHistory(customerId, limit);
    }

    /**
     * 获取可用护理人员列表
     */
    public List<Map<String, Object>> getAvailableCaregivers() {
        return customerMapper.selectCaregiversList();
    }

    /**
     * 护理人员分配统计
     */
    public Map<String, Object> getAssignmentStats() {
        List<Map<String, Object>> workloadStats = getCaregiverWorkloadStats();

        Map<String, Object> result = new HashMap<>();
        int totalCaregivers = 0;
        int activeCaregivers = 0;
        int totalCustomers = 0;
        int averageCustomersPerCaregiver = 0;

        for (Map<String, Object> stat : workloadStats) {
            totalCaregivers++;
            Long customerCount = (Long) stat.get("customer_count");
            int customerCountInt = customerCount.intValue();

            if (customerCountInt > 0) {
                activeCaregivers++;
                totalCustomers += customerCountInt;
            }
        }

        if (activeCaregivers > 0) {
            averageCustomersPerCaregiver = totalCustomers / activeCaregivers;
        }

        result.put("totalCaregivers", totalCaregivers);
        result.put("activeCaregivers", activeCaregivers);
        result.put("totalCustomers", totalCustomers);
        result.put("averageCustomersPerCaregiver", averageCustomersPerCaregiver);
        result.put("workloadDetails", workloadStats);

        return result;
    }

    /**
     * 护理质量评估
     */
    public Map<String, Object> getCareQualityAssessment(Integer customerId, Integer caregiverId) {
        // 这里可以结合护理记录等数据进行质量评估
        Map<String, Object> assessment = new HashMap<>();

        // 获取分配信息
        List<CustomerCaregiverAssignment> assignments;
        if (customerId != null) {
            assignments = getAssignmentsByCustomerId(customerId);
        } else {
            assignments = getAssignmentsByCaregiverId(caregiverId);
        }

        assessment.put("assignments", assignments);
        assessment.put("assignmentCount", assignments.size());

        // 计算分配时长
        int totalDays = 0;
        for (CustomerCaregiverAssignment assignment : assignments) {
            LocalDate endDate = assignment.getEndDate() != null ? assignment.getEndDate() : LocalDate.now();
            totalDays += endDate.toEpochDay() - assignment.getAssignmentDate().toEpochDay();
        }

        assessment.put("totalCareDays", totalDays);
        assessment.put("averageCareDays", assignments.size() > 0 ? totalDays / assignments.size() : 0);

        return assessment;
    }

    /**
     * 智能护理人员推荐
     */
    public List<Map<String, Object>> recommendCaregivers(Integer customerId, String nursingLevel) {
        List<Map<String, Object>> availableCaregivers = getAvailableCaregivers();

        // 根据护理等级和工作负载进行推荐排序
        // 这里可以添加更复杂的推荐算法


        return availableCaregivers.stream()
                .sorted((c1, c2) -> {
                    // 按工作负载升序排列，优先推荐工作负载较轻的护理人员
                    Long count1 = (Long) c1.get("customer_count");
                    Long count2 = (Long) c2.get("customer_count");
                    return count1.compareTo(count2);
                })
                .limit(5) // 推荐前5个
                .collect(Collectors.toList());
    }

    /**
     * 批量更新分配状态
     */
    @Transactional
    public boolean batchUpdateAssignmentStatus(List<Integer> ids, Integer status) {
        try {
            for (Integer id : ids) {
                updateAssignmentStatus(id, status);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量更新状态失败：" + e.getMessage());
        }
    }

    /**
     * 检查护理人员是否可以分配
     */
    public boolean canAssignCaregiver(Integer caregiverId, Integer maxCustomers) {
        List<CustomerCaregiverAssignment> assignments = getAssignmentsByCaregiverId(caregiverId);
        return assignments.size() < (maxCustomers != null ? maxCustomers : 10); // 默认最多分配10个客户
    }

    /**
     * 获取护理团队配置
     */
    public Map<String, Object> getCareTeamConfiguration(Integer customerId) {
        List<CustomerCaregiverAssignment> assignments = getAssignmentsByCustomerId(customerId);
        CustomerCaregiverAssignment primaryCaregiver = getPrimaryCaregiverByCustomerId(customerId);

        Map<String, Object> config = new HashMap<>();
        config.put("assignments", assignments);
        config.put("primaryCaregiver", primaryCaregiver);
        config.put("teamSize", assignments.size());

        // 统计不同角色的护理人员
        Map<String, Integer> roleStats = new HashMap<>();
        for (CustomerCaregiverAssignment assignment : assignments) {
            // 这里需要关联用户表获取角色信息
            // roleStats.put(role, roleStats.getOrDefault(role, 0) + 1);
        }
        config.put("roleStats", roleStats);

        return config;
    }

    public List<CustomerCaregiverAssignment> getAllAssignments() {
        return assignmentMapper.selectAllAssignments();
    }
}