package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.CustomerFocus;
import com.cqupt.yyzx.mapper.CustomerFocusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户关注业务逻辑服务
 */
@Service
public class CustomerFocusService {

    @Autowired
    private CustomerFocusMapper focusMapper;

    /**
     * 分页查询客户关注列表
     */
    public Map<String, Object> getFocusList(String searchKeyword, String focusLevel, Integer status,
                                            Integer caregiverId, LocalDate startDate, LocalDate endDate,
                                            Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<CustomerFocus> focuses = focusMapper.selectFocusListWithDetails(
                searchKeyword, focusLevel, status, caregiverId, startDate, endDate, offset, size);
        Integer total = focusMapper.countFocus(
                searchKeyword, focusLevel, status, caregiverId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("focuses", focuses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询客户关注详细信息
     */
    public CustomerFocus getFocusById(Integer id) {
        return focusMapper.selectFocusById(id);
    }

    /**
     * 添加客户关注
     */
    @Transactional
    public boolean addFocus(CustomerFocus focus) {
        try {
            // 检查是否已存在有效的关注记录
            CustomerFocus existingFocus = focusMapper.selectActiveFocusByCustomerAndCaregiver(
                    focus.getCustomerId(), focus.getCaregiverId());

            if (existingFocus != null) {
                throw new RuntimeException("该客户已在关注列表中");
            }

            // 设置默认值
            if (focus.getStartDate() == null) {
                focus.setStartDate(LocalDate.now());
            }
            if (focus.getStatus() == null) {
                focus.setStatus(1); // 默认关注中
            }
            if (focus.getFocusLevel() == null || focus.getFocusLevel().trim().isEmpty()) {
                focus.setFocusLevel("中");
            }

            return focusMapper.insertFocus(focus) > 0;

        } catch (Exception e) {
            throw new RuntimeException("添加客户关注失败：" + e.getMessage());
        }
    }

    /**
     * 更新客户关注信息
     */
    @Transactional
    public boolean updateFocus(CustomerFocus focus) {
        return focusMapper.updateFocus(focus) > 0;
    }

    /**
     * 删除客户关注
     */
    @Transactional
    public boolean deleteFocus(Integer id) {
        return focusMapper.deleteFocus(id) > 0;
    }

    /**
     * 批量删除客户关注
     */
    @Transactional
    public boolean deleteFocuses(List<Integer> ids) {
        return focusMapper.deleteFocuses(ids) > 0;
    }

    /**
     * 更新客户关注状态
     */
    @Transactional
    public boolean updateFocusStatus(Integer id, Integer status) {
        return focusMapper.updateFocusStatus(id, status) > 0;
    }

    /**
     * 根据护理人员ID查询关注客户列表
     */
    public List<CustomerFocus> getFocusesByCaregiverId(Integer caregiverId, Integer status) {
        return focusMapper.selectFocusesByCaregiverId(caregiverId, status);
    }

    /**
     * 根据客户ID查询关注记录
     */
    public List<CustomerFocus> getFocusesByCustomerId(Integer customerId) {
        return focusMapper.selectFocusesByCustomerId(customerId);
    }

    /**
     * 检查客户是否已被关注
     */
    public boolean isCustomerFocused(Integer customerId, Integer caregiverId) {
        CustomerFocus focus = focusMapper.selectActiveFocusByCustomerAndCaregiver(customerId, caregiverId);
        return focus != null;
    }

    /**
     * 获取关注统计信息
     */
    public Map<String, Object> getFocusStats(Integer caregiverId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Map<String, Object>> stats = focusMapper.getFocusStats(caregiverId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int active = 0;
        int ended = 0;

        for (Map<String, Object> stat : stats) {
            Integer status = (Integer) stat.get("status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            total += countInt;

            if (status == 1) {
                active = countInt;
            } else if (status == 0) {
                ended = countInt;
            }
        }

        result.put("total", total);
        result.put("active", active);
        result.put("ended", ended);

        return result;
    }

    /**
     * 查询即将到期的关注
     */
    public List<CustomerFocus> getExpiringFocuses(Integer days, Integer caregiverId) {
        if (days == null) days = 7; // 默认7天
        return focusMapper.selectExpiringFocuses(days, caregiverId);
    }

    /**
     * 查询高关注级别客户
     */
    public List<CustomerFocus> getHighPriorityFocuses(Integer caregiverId) {
        return focusMapper.selectHighPriorityFocuses(caregiverId);
    }

    /**
     * 根据关注级别统计
     */
    public Map<String, Object> getFocusStatsByLevel(Integer caregiverId) {
        List<Map<String, Object>> stats = focusMapper.getFocusStatsByLevel(caregiverId);

        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> levelStats = new HashMap<>();

        for (Map<String, Object> stat : stats) {
            String level = (String) stat.get("focus_level");
            Long count = (Long) stat.get("count");
            levelStats.put(level, count.intValue());
        }

        result.put("levelStats", levelStats);
        result.put("high", levelStats.getOrDefault("高", 0));
        result.put("medium", levelStats.getOrDefault("中", 0));
        result.put("low", levelStats.getOrDefault("低", 0));

        return result;
    }

    /**
     * 批量更新关注状态
     */
    @Transactional
    public boolean batchUpdateFocusStatus(List<Integer> ids, Integer status) {
        try {
            return focusMapper.batchUpdateFocusStatus(ids, status) > 0;
        } catch (Exception e) {
            throw new RuntimeException("批量更新关注状态失败：" + e.getMessage());
        }
    }

    /**
     * 终止护理人员的所有关注
     */
    @Transactional
    public boolean terminateCaregiverFocuses(Integer caregiverId) {
        return focusMapper.terminateCaregiverFocuses(caregiverId) > 0;
    }

    /**
     * 获取关注历史记录
     */
    public List<CustomerFocus> getFocusHistory(Integer customerId, Integer limit) {
        if (limit == null) limit = 10;
        return focusMapper.selectFocusHistory(customerId, limit);
    }

    /**
     * 设置关注到期
     */
    @Transactional
    public boolean setFocusExpire(Integer id, LocalDate endDate) {
        try {
            CustomerFocus focus = focusMapper.selectFocusById(id);
            if (focus == null) {
                throw new RuntimeException("关注记录不存在");
            }

            focus.setEndDate(endDate);
            focus.setStatus(0); // 设为已结束

            return focusMapper.updateFocus(focus) > 0;

        } catch (Exception e) {
            throw new RuntimeException("设置关注到期失败：" + e.getMessage());
        }
    }

    /**
     * 延长关注期限
     */
    @Transactional
    public boolean extendFocus(Integer id, LocalDate newEndDate) {
        try {
            CustomerFocus focus = focusMapper.selectFocusById(id);
            if (focus == null) {
                throw new RuntimeException("关注记录不存在");
            }

            focus.setEndDate(newEndDate);
            if (focus.getStatus() == 0) {
                focus.setStatus(1); // 重新激活
            }

            return focusMapper.updateFocus(focus) > 0;

        } catch (Exception e) {
            throw new RuntimeException("延长关注期限失败：" + e.getMessage());
        }
    }

    /**
     * 升级关注级别
     */
    @Transactional
    public boolean upgradeFocusLevel(Integer id, String newLevel, String reason) {
        try {
            CustomerFocus focus = focusMapper.selectFocusById(id);
            if (focus == null) {
                throw new RuntimeException("关注记录不存在");
            }

            focus.setFocusLevel(newLevel);
            focus.setFocusReason(reason);

            return focusMapper.updateFocus(focus) > 0;

        } catch (Exception e) {
            throw new RuntimeException("升级关注级别失败：" + e.getMessage());
        }
    }

    /**
     * 获取关注级别选项
     */
    public List<String> getFocusLevelOptions() {
        return Arrays.asList("高", "中", "低");
    }

    /**
     * 获取关注原因模板
     */
    public List<String> getFocusReasonTemplates() {
        return Arrays.asList(
                "身体状况需要密切观察",
                "心理状态不稳定",
                "用药需要特别注意",
                "家属特别要求",
                "新入住适应期",
                "康复训练关键期",
                "慢性疾病管理",
                "跌倒风险较高",
                "认知功能下降",
                "其他原因"
        );
    }

    /**
     * 智能关注建议
     */
    public Map<String, Object> getFocusRecommendations(Integer caregiverId) {
        // 这里可以根据护理记录、健康数据等提供智能建议
        Map<String, Object> recommendations = new HashMap<>();

        // 获取当前关注统计
        Map<String, Object> currentStats = getFocusStatsByLevel(caregiverId);
        recommendations.put("currentStats", currentStats);

        // 获取高优先级关注
        List<CustomerFocus> highPriority = getHighPriorityFocuses(caregiverId);
        recommendations.put("highPriorityCount", highPriority.size());

        // 获取即将到期的关注
        List<CustomerFocus> expiring = getExpiringFocuses(7, caregiverId);
        recommendations.put("expiringCount", expiring.size());

        return recommendations;
    }

    /**
     * 关注提醒检查
     */
    public Map<String, Object> checkFocusAlerts(Integer caregiverId) {
        Map<String, Object> alerts = new HashMap<>();

        // 即将到期的关注
        List<CustomerFocus> expiring = getExpiringFocuses(3, caregiverId);
        alerts.put("expiring", expiring);

        // 高关注级别客户
        List<CustomerFocus> highPriority = getHighPriorityFocuses(caregiverId);
        alerts.put("highPriority", highPriority);

        // 长期关注（超过30天）
        List<CustomerFocus> longTerm = focusMapper.selectFocusesByCaregiverId(caregiverId, 1)
                .stream()
                .filter(focus -> {
                    LocalDate startDate = focus.getStartDate();
                    return startDate != null && startDate.isBefore(LocalDate.now().minusDays(30));
                })
                .collect(Collectors.toList());
        alerts.put("longTerm", longTerm);

        return alerts;
    }
}