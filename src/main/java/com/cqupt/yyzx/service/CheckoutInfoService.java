package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.CheckoutInfo;
import com.cqupt.yyzx.entity.Customer;
import com.cqupt.yyzx.mapper.CheckoutInfoMapper;
import com.cqupt.yyzx.mapper.CustomerMapper;
import com.cqupt.yyzx.mapper.NursingAgreementMapper;
import com.cqupt.yyzx.mapper.CustomerCaregiverAssignmentMapper;
import com.cqupt.yyzx.mapper.BedMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退住信息业务逻辑服务
 */
@Service
public class CheckoutInfoService {

    @Autowired
    private CheckoutInfoMapper checkoutInfoMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private NursingAgreementMapper nursingAgreementMapper;

    @Autowired
    private CustomerCaregiverAssignmentMapper assignmentMapper;

    @Autowired
    private BedMapper bedMapper;

    /**
     * 分页查询退住信息列表
     */
    public Map<String, Object> getCheckoutInfoList(String searchKeyword, String checkoutType,
                                                   LocalDate startDate, LocalDate endDate,
                                                   Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<CheckoutInfo> checkoutInfos = checkoutInfoMapper.selectCheckoutInfoList(
                searchKeyword, checkoutType, startDate, endDate, offset, size);
        Integer total = checkoutInfoMapper.countCheckoutInfos(
                searchKeyword, checkoutType, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("checkoutInfos", checkoutInfos);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询退住信息详细信息
     */
    public CheckoutInfo getCheckoutInfoById(Integer id) {
        return checkoutInfoMapper.selectCheckoutInfoById(id);
    }

    /**
     * 客户退住登记
     */
    @Transactional
    public boolean checkoutCustomer(Integer customerId, CheckoutInfo checkoutInfo) {
        try {
            // 1. 检查客户是否存在且在住
            Customer customer = customerMapper.selectCustomerById(customerId);
            if (customer == null) {
                throw new RuntimeException("客户不存在");
            }
            if (customer.getStatus() != 1) {
                throw new RuntimeException("客户不在住状态，无法办理退住");
            }

            // 2. 创建退住信息
            if (checkoutInfo.getCheckoutDate() == null) {
                checkoutInfo.setCheckoutDate(LocalDate.now());
            }

            int checkoutResult = checkoutInfoMapper.insertCheckoutInfo(checkoutInfo);
            if (checkoutResult <= 0) {
                throw new RuntimeException("退住信息保存失败");
            }

            // 3. 更新客户状态
            int customerResult = customerMapper.checkoutCustomer(customerId, checkoutInfo.getId());
            if (customerResult <= 0) {
                throw new RuntimeException("客户状态更新失败");
            }

            // 4. 释放床位
            if (customer.getBedId() != null) {
                bedMapper.updateBedStatus(customer.getBedId(), "空闲");
            }

            // 5. 终止护理协议
            nursingAgreementMapper.terminateCustomerAgreements(customerId);

            // 6. 终止护理人员分配
            assignmentMapper.terminateCustomerAssignments(customerId);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("退住登记失败：" + e.getMessage());
        }
    }

    /**
     * 添加退住信息
     */
    @Transactional
    public boolean addCheckoutInfo(CheckoutInfo checkoutInfo) {
        if (checkoutInfo.getCheckoutDate() == null) {
            checkoutInfo.setCheckoutDate(LocalDate.now());
        }

        return checkoutInfoMapper.insertCheckoutInfo(checkoutInfo) > 0;
    }

    /**
     * 更新退住信息
     */
    @Transactional
    public boolean updateCheckoutInfo(CheckoutInfo checkoutInfo) {
        return checkoutInfoMapper.updateCheckoutInfo(checkoutInfo) > 0;
    }

    /**
     * 删除退住信息
     */
    @Transactional
    public boolean deleteCheckoutInfo(Integer id) {
        // 检查是否有客户关联此退住信息
        // 这里可以添加关联检查逻辑
        return checkoutInfoMapper.deleteCheckoutInfo(id) > 0;
    }

    /**
     * 批量删除退住信息
     */
    @Transactional
    public boolean deleteCheckoutInfos(List<Integer> ids) {
        return checkoutInfoMapper.deleteCheckoutInfos(ids) > 0;
    }

    /**
     * 获取退住统计信息
     */
    public Map<String, Object> getCheckoutStats(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(3);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Map<String, Object>> stats = checkoutInfoMapper.getCheckoutStats(startDate, endDate);
        List<Map<String, Object>> typeStats = checkoutInfoMapper.getCheckoutStatsByType();

        Map<String, Object> result = new HashMap<>();

        // 时间段统计
        int totalInPeriod = 0;
        for (Map<String, Object> stat : stats) {
            Long count = (Long) stat.get("count");
            totalInPeriod += count.intValue();
        }

        // 按类型统计
        Map<String, Integer> typeStatsMap = new HashMap<>();
        for (Map<String, Object> stat : typeStats) {
            String type = (String) stat.get("checkout_type");
            Long count = (Long) stat.get("count");
            typeStatsMap.put(type, count.intValue());
        }

        result.put("totalInPeriod", totalInPeriod);
        result.put("typeStats", typeStatsMap);
        result.put("dailyStats", stats);

        return result;
    }

    /**
     * 查询最近的退住记录
     */
    public List<CheckoutInfo> getRecentCheckouts(Integer limit) {
        if (limit == null) limit = 10;
        return checkoutInfoMapper.selectRecentCheckouts(limit);
    }

    /**
     * 根据退住类型统计
     */
    public List<Map<String, Object>> getCheckoutStatsByType() {
        return checkoutInfoMapper.getCheckoutStatsByType();
    }

    /**
     * 批量退住操作
     */
    @Transactional
    public boolean batchCheckoutCustomers(List<Integer> customerIds, CheckoutInfo checkoutInfo) {
        try {
            for (Integer customerId : customerIds) {
                // 为每个客户创建单独的退住信息
                CheckoutInfo customerCheckoutInfo = new CheckoutInfo();
                customerCheckoutInfo.setCheckoutType(checkoutInfo.getCheckoutType());
                customerCheckoutInfo.setCheckoutReason(checkoutInfo.getCheckoutReason());
                customerCheckoutInfo.setCheckoutDate(checkoutInfo.getCheckoutDate());
                customerCheckoutInfo.setRemarks(checkoutInfo.getRemarks());

                checkoutCustomer(customerId, customerCheckoutInfo);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量退住失败：" + e.getMessage());
        }
    }

    /**
     * 获取退住类型选项
     */
    public List<String> getCheckoutTypes() {
        return Arrays.asList("正常退住", "医疗转院", "自愿退住", "其他原因") ;
    }
}