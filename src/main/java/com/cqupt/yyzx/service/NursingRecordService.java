package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.NursingRecord;
import com.cqupt.yyzx.mapper.NursingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 护理记录业务逻辑服务
 */
@Service
public class NursingRecordService {

    @Autowired
    private NursingRecordMapper recordMapper;

    /**
     * 分页查询护理记录列表
     */
    public Map<String, Object> getRecordList(String searchKeyword, String executionStatus,
                                             LocalDate startDate, LocalDate endDate,
                                             Integer customerId, Integer projectId, Integer caregiverId,
                                             Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<NursingRecord> records = recordMapper.selectRecordListWithDetails(
                searchKeyword, executionStatus, startDate, endDate, customerId, projectId, caregiverId, offset, size);
        Integer total = recordMapper.countRecords(
                searchKeyword, executionStatus, startDate, endDate, customerId, projectId, caregiverId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询护理记录详细信息
     */
    public NursingRecord getRecordById(Integer id) {
        return recordMapper.selectRecordById(id);
    }

    /**
     * 添加护理记录
     */
    @Transactional
    public boolean addRecord(NursingRecord record) {
        try {
            // 检查是否已存在相同的护理记录
            List<NursingRecord> duplicateRecords = recordMapper.selectDuplicateRecords(
                    record.getCustomerId(), record.getProjectId(), record.getExecutionDate());

            if (!duplicateRecords.isEmpty()) {
                throw new RuntimeException("该客户今日已有相同项目的护理记录");
            }

            // 设置默认值
            if (record.getExecutionDate() == null) {
                record.setExecutionDate(LocalDate.now());
            }
            if (record.getExecutionTime() == null) {
                record.setExecutionTime(LocalTime.now());
            }
            if (record.getExecutionStatus() == null || record.getExecutionStatus().trim().isEmpty()) {
                record.setExecutionStatus("已完成");
            }

            return recordMapper.insertRecord(record) > 0;

        } catch (Exception e) {
            throw new RuntimeException("添加护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理记录信息
     */
    @Transactional
    public boolean updateRecord(NursingRecord record) {
        return recordMapper.updateRecord(record) > 0;
    }

    /**
     * 删除护理记录
     */
    @Transactional
    public boolean deleteRecord(Integer id) {
        return recordMapper.deleteRecord(id) > 0;
    }

    /**
     * 批量删除护理记录
     */
    @Transactional
    public boolean deleteRecords(List<Integer> ids) {
        return recordMapper.deleteRecords(ids) > 0;
    }

    /**
     * 根据客户ID查询护理记录
     */
    public List<NursingRecord> getRecordsByCustomerId(Integer customerId, Integer limit) {
        if (limit == null) limit = 50;
        return recordMapper.selectRecordsByCustomerId(customerId, limit);
    }

    /**
     * 根据护理人员ID查询护理记录
     */
    public List<NursingRecord> getRecordsByCaregiverId(Integer caregiverId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusWeeks(1);
        if (endDate == null) endDate = LocalDate.now();
        return recordMapper.selectRecordsByCaregiverId(caregiverId, startDate, endDate);
    }

    /**
     * 根据项目ID查询护理记录
     */
    public List<NursingRecord> getRecordsByProjectId(Integer projectId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        return recordMapper.selectRecordsByProjectId(projectId, startDate, endDate);
    }

    /**
     * 查询今日护理记录
     */
    public List<NursingRecord> getTodayRecords(Integer caregiverId) {
        return recordMapper.selectTodayRecords(caregiverId);
    }

    /**
     * 查询未完成的护理记录
     */
    public List<NursingRecord> getUncompletedRecords() {
        return recordMapper.selectUncompletedRecords();
    }

    /**
     * 获取护理记录统计信息
     */
    public Map<String, Object> getRecordStats(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Map<String, Object>> stats = recordMapper.getRecordStats(startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int completed = 0;
        int partialCompleted = 0;
        int unExecuted = 0;
        int abnormal = 0;

        for (Map<String, Object> stat : stats) {
            String status = (String) stat.get("execution_status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            total += countInt;

            switch (status) {
                case "已完成":
                    completed = countInt;
                    break;
                case "部分完成":
                    partialCompleted = countInt;
                    break;
                case "未执行":
                    unExecuted = countInt;
                    break;
                case "异常":
                    abnormal = countInt;
                    break;
            }
        }

        result.put("total", total);
        result.put("completed", completed);
        result.put("partialCompleted", partialCompleted);
        result.put("unExecuted", unExecuted);
        result.put("abnormal", abnormal);

        // 计算完成率
        if (total > 0) {
            double completionRate = ((double) completed / total) * 100;
            result.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        } else {
            result.put("completionRate", 0.0);
        }

        return result;
    }

    /**
     * 获取护理人员工作量统计
     */
    public List<Map<String, Object>> getCaregiverWorkloadStats(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusWeeks(1);
        if (endDate == null) endDate = LocalDate.now();
        return recordMapper.getCaregiverWorkloadStats(startDate, endDate);
    }

    /**
     * 获取项目执行统计
     */
    public List<Map<String, Object>> getProjectExecutionStats(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        return recordMapper.getProjectExecutionStats(startDate, endDate);
    }

    /**
     * 获取客户护理统计
     */
    public Map<String, Object> getCustomerCareStats(Integer customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Map<String, Object>> stats = recordMapper.getCustomerCareStats(customerId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("details", stats);

        // 计算总计信息
        int totalRecords = 0;
        int totalDuration = 0;
        for (Map<String, Object> stat : stats) {
            Long recordCount = (Long) stat.get("record_count");
            Long duration = (Long) stat.get("total_duration");
            totalRecords += recordCount.intValue();
            totalDuration += duration.intValue();
        }

        result.put("totalRecords", totalRecords);
        result.put("totalDuration", totalDuration);
        result.put("avgDuration", totalRecords > 0 ? totalDuration / totalRecords : 0);

        return result;
    }

    /**
     * 批量添加护理记录
     */
    @Transactional
    public boolean batchAddRecords(List<NursingRecord> records) {
        try {
            // 设置默认值
            for (NursingRecord record : records) {
                if (record.getExecutionDate() == null) {
                    record.setExecutionDate(LocalDate.now());
                }
                if (record.getExecutionTime() == null) {
                    record.setExecutionTime(LocalTime.now());
                }
                if (record.getExecutionStatus() == null || record.getExecutionStatus().trim().isEmpty()) {
                    record.setExecutionStatus("已完成");
                }
            }

            return recordMapper.batchInsertRecords(records) > 0;

        } catch (Exception e) {
            throw new RuntimeException("批量添加护理记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理记录状态
     */
    @Transactional
    public boolean updateRecordStatus(Integer id, String status) {
        return recordMapper.updateRecordStatus(id, status) > 0;
    }

    /**
     * 快速记录护理
     */
    @Transactional
    public boolean quickRecord(Integer customerId, Integer projectId, Integer caregiverId, String notes) {
        try {
            NursingRecord record = new NursingRecord();
            record.setCustomerId(customerId);
            record.setProjectId(projectId);
            record.setCaregiverId(caregiverId);
            record.setExecutionDate(LocalDate.now());
            record.setExecutionTime(LocalTime.now());
            record.setExecutionStatus("已完成");
            record.setNotes(notes);

            return addRecord(record);

        } catch (Exception e) {
            throw new RuntimeException("快速记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理质量评估数据
     */
    public Map<String, Object> getNursingQualityData(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(3);
        if (endDate == null) endDate = LocalDate.now();

        List<Map<String, Object>> qualityData = recordMapper.getNursingQualityData(startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("qualityData", qualityData);

        // 计算质量指标
        double avgCompletionRate = 0.0;
        double avgDurationCompliance = 0.0;
        int dataCount = qualityData.size();

        for (Map<String, Object> data : qualityData) {
            Double completionRate = (Double) data.get("completion_rate");
            Double durationCompliance = (Double) data.get("duration_compliance");

            if (completionRate != null) {
                avgCompletionRate += completionRate;
            }
            if (durationCompliance != null) {
                avgDurationCompliance += durationCompliance;
            }
        }

        if (dataCount > 0) {
            avgCompletionRate = avgCompletionRate / dataCount;
            avgDurationCompliance = avgDurationCompliance / dataCount;
        }

        result.put("avgCompletionRate", Math.round(avgCompletionRate * 100.0) / 100.0);
        result.put("avgDurationCompliance", Math.round(avgDurationCompliance * 100.0) / 100.0);

        return result;
    }

    /**
     * 获取护理记录执行状态选项
     */
    public List<String> getExecutionStatusOptions() {
        return  Arrays.asList("已完成", "部分完成", "未执行", "异常");
    }
}