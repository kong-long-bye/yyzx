package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.Food;
import com.cqupt.yyzx.entity.MealDiary;
import com.cqupt.yyzx.mapper.FoodMapper;
import com.cqupt.yyzx.mapper.MealDiaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 膳食日记业务逻辑服务
 */
@Service
public class MealDiaryService {

    @Autowired
    private MealDiaryMapper mealDiaryMapper;

    @Autowired
    private FoodMapper foodMapper;

    /**
     * 分页查询膳食记录列表
     */
    public Map<String, Object> getMealDiaryList(Integer customerId, LocalDate mealDate, String mealType,
                                                LocalDate startDate, LocalDate endDate,
                                                Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<MealDiary> records = mealDiaryMapper.selectMealDiaryListWithDetails(
                customerId, mealDate, mealType, startDate, endDate, offset, size);
        Integer total = mealDiaryMapper.countMealDiaries(
                customerId, mealDate, mealType, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询膳食记录详细信息
     */
    public MealDiary getMealDiaryById(Integer id) {
        return mealDiaryMapper.selectMealDiaryById(id);
    }

    /**
     * 添加膳食记录
     */
    @Transactional
    public boolean addMealDiary(MealDiary mealDiary) {
        try {
            // 设置默认值
            if (mealDiary.getMealDate() == null) {
                mealDiary.setMealDate(LocalDate.now());
            }
            if (mealDiary.getQuantity() == null) {
                mealDiary.setQuantity(new BigDecimal("1.0"));
            }

            // 检查重复记录
            Integer duplicateCount = mealDiaryMapper.checkDuplicateRecord(
                    mealDiary.getCustomerId(), mealDiary.getMealDate(),
                    mealDiary.getMealType(), mealDiary.getFoodId());

            if (duplicateCount > 0) {
                throw new RuntimeException("该餐次已存在相同食品记录");
            }

            return mealDiaryMapper.insertMealDiary(mealDiary) > 0;

        } catch (Exception e) {
            throw new RuntimeException("添加膳食记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新膳食记录信息
     */
    @Transactional
    public boolean updateMealDiary(MealDiary mealDiary) {
        return mealDiaryMapper.updateMealDiary(mealDiary) > 0;
    }

    /**
     * 删除膳食记录
     */
    @Transactional
    public boolean deleteMealDiary(Integer id) {
        return mealDiaryMapper.deleteMealDiary(id) > 0;
    }

    /**
     * 批量删除膳食记录
     */
    @Transactional
    public boolean deleteMealDiaries(List<Integer> ids) {
        return mealDiaryMapper.deleteMealDiaries(ids) > 0;
    }

    /**
     * 查询当日膳食记录
     */
    public List<MealDiary> getTodayMealDiaries(Integer customerId) {
        return mealDiaryMapper.selectTodayMealDiaries(customerId, LocalDate.now());
    }

    /**
     * 查询指定日期膳食记录
     */
    public List<MealDiary> getMealDiariesByDate(Integer customerId, LocalDate date) {
        return mealDiaryMapper.selectTodayMealDiaries(customerId, date);
    }

    /**
     * 查询指定时间段膳食记录
     */
    public List<MealDiary> getMealDiariesByPeriod(Integer customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusWeeks(1);
        if (endDate == null) endDate = LocalDate.now();
        return mealDiaryMapper.selectMealDiariesByPeriod(customerId, startDate, endDate);
    }

    /**
     * 获取客户营养统计
     */
    public Map<String, Object> getNutritionStats(Integer customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusWeeks(1);
        if (endDate == null) endDate = LocalDate.now();

        Map<String, Object> nutritionStats = mealDiaryMapper.getNutritionStats(customerId, startDate, endDate);
        Map<String, Object> costStats = mealDiaryMapper.getCostStats(customerId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();

        // 计算时间段信息
        long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        Map<String, Object> period = new HashMap<>();
        period.put("startDate", startDate);
        period.put("endDate", endDate);
        period.put("days", days);

        // 处理营养统计
        Map<String, Object> nutrition = new HashMap<>();
        if (nutritionStats != null) {
            BigDecimal totalCalories = (BigDecimal) nutritionStats.get("total_calories");
            BigDecimal totalProtein = (BigDecimal) nutritionStats.get("total_protein");
            BigDecimal totalFat = (BigDecimal) nutritionStats.get("total_fat");
            BigDecimal totalCarbs = (BigDecimal) nutritionStats.get("total_carbs");
            BigDecimal totalCost = (BigDecimal) costStats.get("total_cost");

            nutrition.put("totalCalories", totalCalories != null ? totalCalories.doubleValue() : 0.0);
            nutrition.put("totalProtein", totalProtein != null ? totalProtein.doubleValue() : 0.0);
            nutrition.put("totalFat", totalFat != null ? totalFat.doubleValue() : 0.0);
            nutrition.put("totalCarbs", totalCarbs != null ? totalCarbs.doubleValue() : 0.0);
            nutrition.put("totalCost", totalCost != null ? totalCost.doubleValue() : 0.0);

            // 计算平均值
            if (days > 0) {
                nutrition.put("avgDailyCalories", totalCalories != null ? totalCalories.doubleValue() / days : 0.0);
                nutrition.put("avgDailyProtein", totalProtein != null ? totalProtein.doubleValue() / days : 0.0);
                nutrition.put("avgDailyCost", totalCost != null ? totalCost.doubleValue() / days : 0.0);
            }
        }

        result.put("period", period);
        result.put("nutrition", nutrition);

        // 添加营养建议
        result.put("suggestions", generateNutritionSuggestions(nutrition));

        return result;
    }

    /**
     * 查询最近的膳食记录
     */
    public List<MealDiary> getRecentMealDiaries(Integer customerId, Integer limit) {
        if (limit == null) limit = 10;
        return mealDiaryMapper.selectRecentMealDiaries(customerId, limit);
    }

    /**
     * 获取膳食类型统计
     */
    public List<Map<String, Object>> getMealTypeStats(Integer customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusWeeks(1);
        if (endDate == null) endDate = LocalDate.now();
        return mealDiaryMapper.getMealTypeStats(customerId, startDate, endDate);
    }

    /**
     * 获取热门食品统计
     */
    public List<Map<String, Object>> getPopularFoodsStats(LocalDate startDate, LocalDate endDate, Integer limit) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        if (limit == null) limit = 10;
        return mealDiaryMapper.getPopularFoodsStats(startDate, endDate, limit);
    }

    /**
     * 批量添加膳食记录
     */
    @Transactional
    public boolean batchAddMealDiaries(List<MealDiary> records) {
        try {
            // 设置默认值
            for (MealDiary record : records) {
                if (record.getMealDate() == null) {
                    record.setMealDate(LocalDate.now());
                }
                if (record.getQuantity() == null) {
                    record.setQuantity(new BigDecimal("1.0"));
                }
            }

            return mealDiaryMapper.batchInsertMealDiaries(records) > 0;

        } catch (Exception e) {
            throw new RuntimeException("批量添加膳食记录失败：" + e.getMessage());
        }
    }

    /**
     * 快速添加膳食记录
     */
    @Transactional
    public boolean quickAddMealRecord(Integer customerId, Integer foodId, String mealType,
                                      BigDecimal quantity, String notes, Integer caregiverId) {
        try {
            MealDiary record = new MealDiary();
            record.setCustomerId(customerId);
            record.setFoodId(foodId);
            record.setMealType(mealType);
            record.setQuantity(quantity != null ? quantity : new BigDecimal("1.0"));
            record.setNotes(notes);
            record.setCaregiverId(caregiverId);
            record.setMealDate(LocalDate.now());

            return addMealDiary(record);

        } catch (Exception e) {
            throw new RuntimeException("快速添加膳食记录失败：" + e.getMessage());
        }
    }

    /**
     * 生成营养建议
     */
    private List<String> generateNutritionSuggestions(Map<String, Object> nutrition) {
        List<String> suggestions = new java.util.ArrayList<>();

        Double avgDailyCalories = (Double) nutrition.get("avgDailyCalories");
        Double avgDailyProtein = (Double) nutrition.get("avgDailyProtein");

        if (avgDailyCalories != null) {
            if (avgDailyCalories < 1500) {
                suggestions.add("建议增加热量摄入，确保营养充足");
            } else if (avgDailyCalories > 2500) {
                suggestions.add("建议控制热量摄入，注意饮食搭配");
            }
        }

        if (avgDailyProtein != null) {
            if (avgDailyProtein < 50) {
                suggestions.add("建议增加蛋白质摄入，可适量增加肉类、蛋类食物");
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("营养摄入均衡，请继续保持");
        }

        return suggestions;
    }

    /**
     * 获取餐次类型选项
     */
    public List<String> getMealTypeOptions() {
        List<String> categories = new ArrayList<>();
        categories.add("早餐");
        categories.add("午餐");
        categories.add("晚餐");
        categories.add("加餐");

        return categories;
    }
}