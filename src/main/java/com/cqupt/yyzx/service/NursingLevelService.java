package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.NursingAgreement;
import com.cqupt.yyzx.mapper.NursingAgreementMapper;
import com.cqupt.yyzx.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 护理级别业务逻辑服务
 */
@Service
public class NursingLevelService {

    @Autowired
    private NursingAgreementMapper agreementMapper;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 获取护理等级选项
     */
    public List<Map<String, Object>> getNursingLevelOptions() {
        return agreementMapper.getNursingLevelOptions();
    }

    /**
     * 根据客户ID查询有效的护理协议
     */
    public NursingAgreement getActiveAgreementByCustomerId(Integer customerId) {
        return agreementMapper.selectActiveAgreementByCustomerId(customerId);
    }

    /**
     * 根据客户ID查询所有护理协议
     */
    public List<NursingAgreement> getAgreementsByCustomerId(Integer customerId) {
        return agreementMapper.selectAgreementsByCustomerId(customerId);
    }

    /**
     * 根据ID查询护理协议详细信息
     */
    public NursingAgreement getAgreementById(Integer id) {
        return agreementMapper.selectAgreementById(id);
    }

    /**
     * 创建护理协议
     */
    @Transactional
    public boolean createAgreement(NursingAgreement agreement) {
        try {
            // 设置默认值
            if (agreement.getLevelStatus() == null || agreement.getLevelStatus().trim().isEmpty()) {
                agreement.setLevelStatus("生效");
            }
            if (agreement.getStartDate() == null) {
                agreement.setStartDate(LocalDate.now());
            }

            // 如果是新的生效协议，先终止客户的其他生效协议
            if ("生效".equals(agreement.getLevelStatus())) {
                agreementMapper.terminateCustomerAgreements(agreement.getCustomerId());
            }

            return agreementMapper.insertAgreement(agreement) > 0;

        } catch (Exception e) {
            throw new RuntimeException("创建护理协议失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理协议信息
     */
    @Transactional
    public boolean updateAgreement(NursingAgreement agreement) {
        return agreementMapper.updateAgreement(agreement) > 0;
    }

    /**
     * 删除护理协议
     */
    @Transactional
    public boolean deleteAgreement(Integer id) {
        return agreementMapper.deleteAgreement(id) > 0;
    }

    /**
     * 更新护理协议状态
     */
    @Transactional
    public boolean updateAgreementStatus(Integer id, String status) {
        try {
            NursingAgreement agreement = agreementMapper.selectAgreementById(id);
            if (agreement == null) {
                throw new RuntimeException("护理协议不存在");
            }

            // 如果要设为生效状态，先终止该客户的其他生效协议
            if ("生效".equals(status)) {
                agreementMapper.terminateCustomerAgreements(agreement.getCustomerId());
            }

            return agreementMapper.updateAgreementStatus(id, status) > 0;

        } catch (Exception e) {
            throw new RuntimeException("更新协议状态失败：" + e.getMessage());
        }
    }

    /**
     * 终止客户的所有有效协议
     */
    @Transactional
    public boolean terminateCustomerAgreements(Integer customerId) {
        return agreementMapper.terminateCustomerAgreements(customerId) > 0;
    }

    /**
     * 查询即将到期的协议
     */
    public List<NursingAgreement> getExpiringAgreements(Integer days) {
        if (days == null) days = 30; // 默认30天
        return agreementMapper.selectExpiredAgreements(days);
    }

    /**
     * 获取护理协议统计信息
     */
    public Map<String, Object> getAgreementStats() {
        List<Map<String, Object>> stats = agreementMapper.getAgreementStats();

        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Integer>> levelStats = new HashMap<>();

        int totalActive = 0;
        int totalSuspended = 0;
        int totalTerminated = 0;

        for (Map<String, Object> stat : stats) {
            String levelName = (String) stat.get("level_name");
            String levelStatus = (String) stat.get("level_status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            // 按等级分组统计
            levelStats.computeIfAbsent(levelName, k -> new HashMap<>())
                    .put(levelStatus, countInt);

            // 按状态总计
            switch (levelStatus) {
                case "生效":
                    totalActive += countInt;
                    break;
                case "暂停":
                    totalSuspended += countInt;
                    break;
                case "终止":
                    totalTerminated += countInt;
                    break;
            }
        }

        result.put("levelStats", levelStats);
        result.put("totalActive", totalActive);
        result.put("totalSuspended", totalSuspended);
        result.put("totalTerminated", totalTerminated);
        result.put("total", totalActive + totalSuspended + totalTerminated);

        return result;
    }

    /**
     * 护理等级升级
     */
    @Transactional
    public boolean upgradeNursingLevel(Integer customerId, String newLevelCode, String newLevelName,
                                       BigDecimal newMonthlyFee, String serviceContent, String reason) {
        try {
            // 终止当前有效协议
            terminateCustomerAgreements(customerId);

            // 创建新的护理协议
            NursingAgreement newAgreement = new NursingAgreement();
            newAgreement.setCustomerId(customerId);
            newAgreement.setLevelCode(newLevelCode);
            newAgreement.setLevelName(newLevelName);
            newAgreement.setMonthlyFee(newMonthlyFee);
            newAgreement.setServiceContent(serviceContent);
            newAgreement.setLevelStatus("生效");
            newAgreement.setStartDate(LocalDate.now());

            return agreementMapper.insertAgreement(newAgreement) > 0;

        } catch (Exception e) {
            throw new RuntimeException("护理等级升级失败：" + e.getMessage());
        }
    }

    /**
     * 护理等级降级
     */
    @Transactional
    public boolean downgradeNursingLevel(Integer customerId, String newLevelCode, String newLevelName,
                                         BigDecimal newMonthlyFee, String serviceContent, String reason) {
        try {
            // 终止当前有效协议
            terminateCustomerAgreements(customerId);

            // 创建新的护理协议
            NursingAgreement newAgreement = new NursingAgreement();
            newAgreement.setCustomerId(customerId);
            newAgreement.setLevelCode(newLevelCode);
            newAgreement.setLevelName(newLevelName);
            newAgreement.setMonthlyFee(newMonthlyFee);
            newAgreement.setServiceContent(serviceContent);
            newAgreement.setLevelStatus("生效");
            newAgreement.setStartDate(LocalDate.now());

            return agreementMapper.insertAgreement(newAgreement) > 0;

        } catch (Exception e) {
            throw new RuntimeException("护理等级降级失败：" + e.getMessage());
        }
    }

    /**
     * 暂停护理服务
     */
    @Transactional
    public boolean suspendNursingService(Integer customerId, String reason) {
        try {
            NursingAgreement activeAgreement = agreementMapper.selectActiveAgreementByCustomerId(customerId);
            if (activeAgreement == null) {
                throw new RuntimeException("客户没有有效的护理协议");
            }

            return agreementMapper.updateAgreementStatus(activeAgreement.getId(), "暂停") > 0;

        } catch (Exception e) {
            throw new RuntimeException("暂停护理服务失败：" + e.getMessage());
        }
    }

    /**
     * 恢复护理服务
     */
    @Transactional
    public boolean resumeNursingService(Integer customerId) {
        try {
            // 查找最近的暂停协议
            List<NursingAgreement> agreements = agreementMapper.selectAgreementsByCustomerId(customerId);
            NursingAgreement suspendedAgreement = null;

            for (NursingAgreement agreement : agreements) {
                if ("暂停".equals(agreement.getLevelStatus())) {
                    suspendedAgreement = agreement;
                    break;
                }
            }

            if (suspendedAgreement == null) {
                throw new RuntimeException("客户没有暂停的护理协议");
            }

            return agreementMapper.updateAgreementStatus(suspendedAgreement.getId(), "生效") > 0;

        } catch (Exception e) {
            throw new RuntimeException("恢复护理服务失败：" + e.getMessage());
        }
    }

    /**
     * 续签护理协议
     */
    @Transactional
    public boolean renewAgreement(Integer customerId, LocalDate newEndDate, BigDecimal newMonthlyFee) {
        try {
            NursingAgreement activeAgreement = agreementMapper.selectActiveAgreementByCustomerId(customerId);
            if (activeAgreement == null) {
                throw new RuntimeException("客户没有有效的护理协议");
            }

            // 更新协议信息
            activeAgreement.setEndDate(newEndDate);
            if (newMonthlyFee != null) {
                activeAgreement.setMonthlyFee(newMonthlyFee);
            }

            return agreementMapper.updateAgreement(activeAgreement) > 0;

        } catch (Exception e) {
            throw new RuntimeException("续签协议失败：" + e.getMessage());
        }
    }

    /**
     * 护理等级变更历史
     */
    public List<NursingAgreement> getNursingLevelHistory(Integer customerId) {
        return agreementMapper.selectAgreementsByCustomerId(customerId);
    }

    /**
     * 获取护理等级分布统计
     */
    public Map<String, Object> getNursingLevelDistribution() {
        List<Map<String, Object>> stats = agreementMapper.getAgreementStats();

        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> levelDistribution = new HashMap<>();
        Map<String, BigDecimal> levelRevenue = new HashMap<>();

        for (Map<String, Object> stat : stats) {
            String levelName = (String) stat.get("level_name");
            String levelStatus = (String) stat.get("level_status");
            Long count = (Long) stat.get("count");
            BigDecimal avgFee = (BigDecimal) stat.get("avg_fee");

            // 只统计生效状态的协议
            if ("生效".equals(levelStatus)) {
                levelDistribution.put(levelName, count.intValue());
                levelRevenue.put(levelName, avgFee.multiply(new BigDecimal(count)));
            }
        }

        result.put("levelDistribution", levelDistribution);
        result.put("levelRevenue", levelRevenue);

        return result;
    }

    /**
     * 获取护理等级详情
     */
    public Map<String, Object> getNursingLevelDetail(String levelCode) {
        List<Map<String, Object>> options = getNursingLevelOptions();

        for (Map<String, Object> option : options) {
            if (levelCode.equals(option.get("level_code"))) {
                return option;
            }
        }

        return null;
    }

    /**
     * 批量更新协议状态
     */
    @Transactional
    public boolean batchUpdateAgreementStatus(List<Integer> ids, String status) {
        try {
            for (Integer id : ids) {
                updateAgreementStatus(id, status);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量更新状态失败：" + e.getMessage());
        }
    }
}