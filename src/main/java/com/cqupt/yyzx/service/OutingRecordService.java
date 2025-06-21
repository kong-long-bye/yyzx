package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.OutingRecord;
import com.cqupt.yyzx.mapper.OutingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外出记录业务逻辑服务
 */
@Service
public class OutingRecordService {

    @Autowired
    private OutingRecordMapper outingRecordMapper;

    /**
     * 分页查询外出记录列表
     */
    public Map<String, Object> getOutingRecordList(String searchKeyword, String approvalStatus,
                                                   LocalDate outingDate, Integer customerId,
                                                   Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<OutingRecord> records = outingRecordMapper.selectOutingRecordListWithDetails(
                searchKeyword, approvalStatus, outingDate, customerId, offset, size);
        Integer total = outingRecordMapper.countOutingRecords(
                searchKeyword, approvalStatus, outingDate, customerId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询外出记录详细信息
     */
    public OutingRecord getOutingRecordById(Integer id) {
        return outingRecordMapper.selectOutingRecordById(id);
    }

    /**
     * 添加外出记录
     */
    @Transactional
    public boolean addOutingRecord(OutingRecord outingRecord) {
        try {
            // 检查客户当日是否已有外出记录
            List<OutingRecord> todayRecords = outingRecordMapper.selectTodayOutingRecordsByCustomer(
                    outingRecord.getCustomerId());

            for (OutingRecord record : todayRecords) {
                if (record.getActualReturnTime() == null) {
                    throw new RuntimeException("客户今日已有外出记录且尚未返回，无法再次申请外出");
                }
            }

            // 设置默认值
            if (outingRecord.getApprovalStatus() == null) {
                outingRecord.setApprovalStatus("待批准");
            }
            if (outingRecord.getOutingDate() == null) {
                outingRecord.setOutingDate(LocalDate.now());
            }

            return outingRecordMapper.insertOutingRecord(outingRecord) > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加外出记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新外出记录信息
     */
    @Transactional
    public boolean updateOutingRecord(OutingRecord outingRecord) {
        return outingRecordMapper.updateOutingRecord(outingRecord) > 0;
    }

    /**
     * 删除外出记录
     */
    @Transactional
    public boolean deleteOutingRecord(Integer id) {
        // 检查记录状态，已批准且未返回的记录不能删除
        OutingRecord record = outingRecordMapper.selectOutingRecordById(id);
        if (record != null && "已批准".equals(record.getApprovalStatus())
                && record.getActualReturnTime() == null) {
            throw new RuntimeException("已批准且未返回的外出记录不能删除");
        }

        return outingRecordMapper.deleteOutingRecord(id) > 0;
    }

    /**
     * 批量删除外出记录
     */
    @Transactional
    public boolean deleteOutingRecords(List<Integer> ids) {
        // 检查记录状态
        for (Integer id : ids) {
            OutingRecord record = outingRecordMapper.selectOutingRecordById(id);
            if (record != null && "已批准".equals(record.getApprovalStatus())
                    && record.getActualReturnTime() == null) {
                throw new RuntimeException("记录中包含已批准且未返回的外出记录，无法删除");
            }
        }

        return outingRecordMapper.deleteOutingRecords(ids) > 0;
    }

    /**
     * 更新外出记录审批状态
     */
    @Transactional
    public boolean updateOutingRecordApprovalStatus(Integer id, String approvalStatus) {
        return outingRecordMapper.updateOutingRecordApprovalStatus(id, approvalStatus) > 0;
    }

    /**
     * 登记返回时间
     */
    @Transactional
    public boolean updateActualReturnTime(Integer id, LocalTime actualReturnTime) {
        return outingRecordMapper.updateOutingRecordActualReturnTime(id, actualReturnTime) > 0;
    }

    /**
     * 快速返回登记（使用当前时间）
     */
    @Transactional
    public boolean quickReturn(Integer id) {
        return updateActualReturnTime(id, LocalTime.now());
    }

    /**
     * 查询未返回的外出记录
     */
    public List<OutingRecord> getUnreturnedOutingRecords() {
        return outingRecordMapper.selectUnreturnedOutingRecords();
    }

    /**
     * 查询超时未返回的外出记录
     */
    public List<OutingRecord> getOverdueOutingRecords() {
        return outingRecordMapper.selectOverdueOutingRecords();
    }

    /**
     * 查询待审批的外出申请
     */
    public List<OutingRecord> getPendingApprovalOutingRecords() {
        return outingRecordMapper.selectPendingApprovalOutingRecords();
    }

    /**
     * 获取外出记录统计信息
     */
    public Map<String, Object> getOutingRecordStats(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Map<String, Object>> stats = outingRecordMapper.getOutingRecordStats(startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int approved = 0;
        int pending = 0;
        int rejected = 0;
        int unreturned = 0;

        for (Map<String, Object> stat : stats) {
            String status = (String) stat.get("approval_status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            total += countInt;

            switch (status) {
                case "已批准":
                    approved = countInt;
                    break;
                case "待批准":
                    pending = countInt;
                    break;
                case "已拒绝":
                    rejected = countInt;
                    break;
            }
        }

        // 获取未返回数量
        List<OutingRecord> unreturnedRecords = getUnreturnedOutingRecords();
        unreturned = unreturnedRecords.size();

        result.put("total", total);
        result.put("approved", approved);
        result.put("pending", pending);
        result.put("rejected", rejected);
        result.put("unreturned", unreturned);

        return result;
    }

    /**
     * 查询客户的外出历史记录
     */
    public List<OutingRecord> getOutingHistoryByCustomer(Integer customerId, Integer limit) {
        if (limit == null) limit = 10;
        return outingRecordMapper.selectOutingHistoryByCustomer(customerId, limit);
    }

    /**
     * 检查客户是否有未完成的外出记录
     */
    public boolean checkCustomerHasUnfinishedOuting(Integer customerId) {
        return outingRecordMapper.checkCustomerHasUnfinishedOuting(customerId) > 0;
    }

    /**
     * 批量审批外出申请
     */
    @Transactional
    public boolean batchApproveOutingRecords(List<Integer> ids, String approvalStatus) {
        try {
            for (Integer id : ids) {
                updateOutingRecordApprovalStatus(id, approvalStatus);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量审批失败：" + e.getMessage());
        }
    }
}