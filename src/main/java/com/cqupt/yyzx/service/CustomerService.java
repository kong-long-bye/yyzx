package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.Customer;
import com.cqupt.yyzx.entity.NursingAgreement;
import com.cqupt.yyzx.entity.CustomerCaregiverAssignment;
import com.cqupt.yyzx.mapper.CustomerMapper;
import com.cqupt.yyzx.mapper.NursingAgreementMapper;
import com.cqupt.yyzx.mapper.CustomerCaregiverAssignmentMapper;
import com.cqupt.yyzx.mapper.BedMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户业务逻辑服务
 */
@Service
public class CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private NursingAgreementMapper nursingAgreementMapper;

    @Autowired
    private CustomerCaregiverAssignmentMapper assignmentMapper;

    @Autowired
    private BedMapper bedMapper;

    /**
     * 分页查询客户列表
     */
    public Map<String, Object> getCustomerList(String searchKeyword, String auditStatus, Integer status,
                                               Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<Customer> customers = customerMapper.selectCustomerListWithDetails(searchKeyword, auditStatus, status, offset, size);
        Integer total = customerMapper.countCustomers(searchKeyword, auditStatus, status);

        Map<String, Object> result = new HashMap<>();
        result.put("customers", customers);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询客户详细信息
     */
    public Customer getCustomerById(Integer id) {
        return customerMapper.selectCustomerById(id);
    }

    /**
     * 客户入住登记
     */
    @Transactional
    public boolean checkinCustomer(Customer customer, NursingAgreement agreement, List<Integer> caregiverIds) {
        try {
            // 1. 检查身份证号是否已存在
            if (customerMapper.checkIdCardExists(customer.getIdCard(), null) > 0) {
                throw new RuntimeException("身份证号已存在");
            }

            // 2. 检查床位是否可用
            if (customer.getBedId() != null) {
                String bedStatus = bedMapper.selectBedById(customer.getBedId()).getStatus();
                if (!"空闲".equals(bedStatus)) {
                    throw new RuntimeException("选择的床位不可用");
                }
            }

            // 3. 设置默认值
            if (customer.getAdmissionDate() == null) {
                customer.setAdmissionDate(LocalDate.now());
            }
            if (customer.getAuditStatus() == null) {
                customer.setAuditStatus("待审核");
            }
            if (customer.getStatus() == null) {
                customer.setStatus(0); // 待审核通过后才设为在住
            }

            // 4. 插入客户信息
            int customerResult = customerMapper.insertCustomer(customer);
            if (customerResult <= 0) {
                throw new RuntimeException("客户信息保存失败");
            }

            // 5. 创建护理协议
            if (agreement != null) {
                agreement.setCustomerId(customer.getId());
                if (agreement.getStartDate() == null) {
                    agreement.setStartDate(customer.getAdmissionDate());
                }
                if (agreement.getLevelStatus() == null) {
                    agreement.setLevelStatus("生效");
                }
                nursingAgreementMapper.insertAgreement(agreement);
            }

            // 6. 分配护理人员
            if (caregiverIds != null && !caregiverIds.isEmpty()) {
                for (int i = 0; i < caregiverIds.size(); i++) {
                    CustomerCaregiverAssignment assignment = new CustomerCaregiverAssignment();
                    assignment.setCustomerId(customer.getId());
                    assignment.setCaregiverId(caregiverIds.get(i));
                    assignment.setAssignmentDate(customer.getAdmissionDate());
                    assignment.setPrimaryCaregiver(i == 0 ? 1 : 0); // 第一个为主要护理人员
                    assignment.setStatus(1);
                    assignmentMapper.insertAssignment(assignment);
                }
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("入住登记失败：" + e.getMessage());
        }
    }

    /**
     * 更新客户信息
     */
    @Transactional
    public boolean updateCustomer(Customer customer) {
        // 检查身份证号是否已存在（排除当前客户）
        if (customerMapper.checkIdCardExists(customer.getIdCard(), customer.getId()) > 0) {
            return false;
        }

        return customerMapper.updateCustomer(customer) > 0;
    }

    /**
     * 删除客户
     */
    @Transactional
    public boolean deleteCustomer(Integer id) {
        // 检查客户是否在住
        Customer customer = customerMapper.selectCustomerById(id);
        if (customer != null && customer.getStatus() == 1) {
            throw new RuntimeException("客户正在住，无法删除");
        }

        // 终止相关协议和分配
        nursingAgreementMapper.terminateCustomerAgreements(id);
        assignmentMapper.terminateCustomerAssignments(id);

        return customerMapper.deleteCustomer(id) > 0;
    }

    /**
     * 批量删除客户
     */
    @Transactional
    public boolean deleteCustomers(List<Integer> ids) {
        // 检查是否有客户在住
        for (Integer id : ids) {
            Customer customer = customerMapper.selectCustomerById(id);
            if (customer != null && customer.getStatus() == 1) {
                throw new RuntimeException("客户 " + customer.getName() + " 正在住，无法删除");
            }
        }

        // 批量终止相关协议和分配
        for (Integer id : ids) {
            nursingAgreementMapper.terminateCustomerAgreements(id);
            assignmentMapper.terminateCustomerAssignments(id);
        }

        return customerMapper.deleteCustomers(ids) > 0;
    }

    /**
     * 更新客户审核状态
     */
    @Transactional
    public boolean updateCustomerAuditStatus(Integer id, String auditStatus) {
        boolean result = customerMapper.updateCustomerAuditStatus(id, auditStatus) > 0;

        // 如果审核通过，更新客户状态为在住，并更新床位状态
        if ("已通过".equals(auditStatus)) {
            customerMapper.updateCustomerStatus(id, 1);

            Customer customer = customerMapper.selectCustomerById(id);
            if (customer != null && customer.getBedId() != null) {
                bedMapper.updateBedStatus(customer.getBedId(), "占用");
            }
        }

        return result;
    }

    /**
     * 获取可用床位列表
     */
    public List<Map<String, Object>> getAvailableBeds() {
        return customerMapper.selectAvailableBeds();
    }

    /**
     * 获取护理人员列表
     */
    public List<Map<String, Object>> getCaregiversList() {
        return customerMapper.selectCaregiversList();
    }

    /**
     * 检查身份证号是否可用
     */
    public boolean isIdCardAvailable(String idCard, Integer excludeId) {
        return customerMapper.checkIdCardExists(idCard, excludeId) == 0;
    }

    /**
     * 获取客户统计信息
     */
    public Map<String, Object> getCustomerStats() {
        List<Map<String, Object>> stats = customerMapper.getCustomerStats();

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int inService = 0;
        int checkedOut = 0;
        int pendingAudit = 0;

        for (Map<String, Object> stat : stats) {
            String status = (String) stat.get("status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            if ("1".equals(status)) {
                inService = countInt;
            } else if ("0".equals(status)) {
                checkedOut = countInt;
            }
            total += countInt;
        }

        // 获取待审核数量
        List<Customer> pendingCustomers = customerMapper.selectPendingAuditCustomers();
        pendingAudit = pendingCustomers.size();

        result.put("total", total);
        result.put("inService", inService);
        result.put("checkedOut", checkedOut);
        result.put("pendingAudit", pendingAudit);

        return result;
    }

    /**
     * 查询即将到期的护理协议
     */
    public List<Customer> getCustomersWithExpiredAgreements(Integer days) {
        if (days == null) days = 30; // 默认30天
        return customerMapper.selectCustomersWithExpiredAgreements(days);
    }

    /**
     * 查询待审核的客户列表
     */
    public List<Customer> getPendingAuditCustomers() {
        return customerMapper.selectPendingAuditCustomers();
    }
}