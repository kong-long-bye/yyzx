package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.Food;
import com.cqupt.yyzx.mapper.FoodMapper;
import com.cqupt.yyzx.mapper.CustomerMapper;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 营养分析和安全检查服务
 */
@Service
public class NutritionAnalysisService {

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 过敏源安全检查
     */
    public Map<String, Object> performSafetyCheck(Integer customerId, List<Integer> foodIds) {
        try {
            // 获取客户过敏信息
            String customerAllergies = getCustomerAllergies(customerId);

            List<Food> foods = foodMapper.selectFoodsByIds(foodIds);
            List<Map<String, Object>> results = new ArrayList<>();

            for (Food food : foods) {
                Map<String, Object> result = new HashMap<>();
                result.put("foodId", food.getId());
                result.put("foodName", food.getFoodName());

                List<String> warnings = checkAllergens(food, customerAllergies);
                result.put("isSafe", warnings.isEmpty());
                result.put("warnings", warnings);

                results.add(result);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("results", results);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("安全检查失败：" + e.getMessage());
        }
    }

    /**
     * 检查食品过敏源
     */
    private List<String> checkAllergens(Food food, String customerAllergies) {
        List<String> warnings = new ArrayList<>();

        if (customerAllergies == null || customerAllergies.trim().isEmpty()) {
            return warnings;
        }

        String allergenInfo = food.getAllergenInfo();
        if (allergenInfo == null || allergenInfo.trim().isEmpty()) {
            return warnings;
        }

        // 简单的过敏源匹配逻辑
        String[] customerAllergenArray = customerAllergies.toLowerCase().split("[,，;；\\s]+");
        String[] foodAllergenArray = allergenInfo.toLowerCase().split("[,，;；\\s]+");

        for (String customerAllergen : customerAllergenArray) {
            if (customerAllergen.trim().isEmpty()) continue;

            for (String foodAllergen : foodAllergenArray) {
                if (foodAllergen.trim().isEmpty()) continue;

                if (foodAllergen.contains(customerAllergen) || customerAllergen.contains(foodAllergen)) {
                    warnings.add("可能含有过敏源：" + customerAllergen);
                    break;
                }
            }
        }

        return warnings;
    }

    /**
     * 获取客户过敏信息
     */
    private String getCustomerAllergies(Integer customerId) {
        try {
            // 从客户表获取过敏信息
            var customer = customerMapper.selectCustomerById(customerId);
            if (customer != null) {
                return customer.getAllergies();
            }
            return "";
        } catch (Exception e) {
            // 如果查询失败，返回空字符串
            return "";
        }
    }

    /**
     * 计算食物营养价值
     */
    public Map<String, Object> calculateNutrition(Integer foodId, Double quantity) {
        Food food = foodMapper.selectFoodById(foodId);
        if (food == null) {
            throw new RuntimeException("食品不存在");
        }

        Map<String, Object> nutrition = new HashMap<>();
        nutrition.put("foodId", foodId);
        nutrition.put("foodName", food.getFoodName());
        nutrition.put("quantity", quantity);
        nutrition.put("unit", food.getUnit());

        if (quantity != null && quantity > 0) {
            double calories = food.getCaloriesPerUnit() != null ? food.getCaloriesPerUnit().doubleValue() * quantity : 0;
            double protein = food.getProteinContent() != null ? food.getProteinContent().doubleValue() * quantity : 0;
            double fat = food.getFatContent() != null ? food.getFatContent().doubleValue() * quantity : 0;
            double carbs = food.getCarbContent() != null ? food.getCarbContent().doubleValue() * quantity : 0;
            double cost = food.getPrice() != null ? food.getPrice().doubleValue() * quantity : 0;

            nutrition.put("totalCalories", calories);
            nutrition.put("totalProtein", protein);
            nutrition.put("totalFat", fat);
            nutrition.put("totalCarbs", carbs);
            nutrition.put("totalCost", cost);
        }

        return nutrition;
    }

    /**
     * 批量计算营养价值
     */
    public List<Map<String, Object>> batchCalculateNutrition(List<Map<String, Object>> foodItems) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> item : foodItems) {
            Integer foodId = (Integer) item.get("foodId");
            Double quantity = ((Number) item.get("quantity")).doubleValue();

            Map<String, Object> nutrition = calculateNutrition(foodId, quantity);
            results.add(nutrition);
        }

        return results;
    }

    /**
     * 食物搭配建议
     */
    public List<String> getFoodCombinationSuggestions(List<Integer> foodIds) {
        List<String> suggestions = new ArrayList<>();
        List<Food> foods = foodMapper.selectFoodsByIds(foodIds);

        // 统计食物分类
        Map<String, Integer> categoryCount = new HashMap<>();
        for (Food food : foods) {
            String category = food.getFoodCategory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }

        // 生成建议
        if (!categoryCount.containsKey("主食")) {
            suggestions.add("建议添加主食，如米饭、面条等");
        }

        if (!categoryCount.containsKey("蔬菜")) {
            suggestions.add("建议添加蔬菜，保证维生素摄入");
        }

        if (!categoryCount.containsKey("荤菜")) {
            suggestions.add("建议添加蛋白质丰富的食物");
        }

        if (categoryCount.getOrDefault("荤菜", 0) > 2) {
            suggestions.add("荤菜较多，建议减少肉类摄入");
        }

        if (!categoryCount.containsKey("汤品")) {
            suggestions.add("建议添加汤品，帮助消化");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("食物搭配均衡，营养丰富");
        }

        return suggestions;
    }

    /**
     * 营养评估
     */
    public Map<String, Object> evaluateNutrition(List<Integer> foodIds, List<Double> quantities) {
        if (foodIds.size() != quantities.size()) {
            throw new RuntimeException("食品ID和数量列表长度不匹配");
        }

        double totalCalories = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCarbs = 0;
        double totalCost = 0;

        List<Food> foods = foodMapper.selectFoodsByIds(foodIds);
        Map<Integer, Food> foodMap = new HashMap<>();
        for (Food food : foods) {
            foodMap.put(food.getId(), food);
        }

        // 计算总营养价值
        for (int i = 0; i < foodIds.size(); i++) {
            Integer foodId = foodIds.get(i);
            Double quantity = quantities.get(i);
            Food food = foodMap.get(foodId);

            if (food != null && quantity != null && quantity > 0) {
                totalCalories += food.getCaloriesPerUnit() != null ? food.getCaloriesPerUnit().doubleValue() * quantity : 0;
                totalProtein += food.getProteinContent() != null ? food.getProteinContent().doubleValue() * quantity : 0;
                totalFat += food.getFatContent() != null ? food.getFatContent().doubleValue() * quantity : 0;
                totalCarbs += food.getCarbContent() != null ? food.getCarbContent().doubleValue() * quantity : 0;
                totalCost += food.getPrice() != null ? food.getPrice().doubleValue() * quantity : 0;
            }
        }

        Map<String, Object> evaluation = new HashMap<>();
        evaluation.put("totalCalories", totalCalories);
        evaluation.put("totalProtein", totalProtein);
        evaluation.put("totalFat", totalFat);
        evaluation.put("totalCarbs", totalCarbs);
        evaluation.put("totalCost", totalCost);

        // 营养评估
        List<String> nutritionAdvice = new ArrayList<>();

        if (totalCalories < 300) {
            nutritionAdvice.add("热量偏低，建议增加主食");
        } else if (totalCalories > 800) {
            nutritionAdvice.add("热量偏高，注意控制食量");
        }

        if (totalProtein < 10) {
            nutritionAdvice.add("蛋白质不足，建议增加肉类或蛋类");
        }

        if (totalFat > 30) {
            nutritionAdvice.add("脂肪含量较高，注意清淡饮食");
        }

        if (nutritionAdvice.isEmpty()) {
            nutritionAdvice.add("营养搭配合理");
        }

        evaluation.put("nutritionAdvice", nutritionAdvice);
        evaluation.put("foodCombinationSuggestions", getFoodCombinationSuggestions(foodIds));

        return evaluation;
    }

    /**
     * 获取营养建议
     */
    public List<String> getNutritionRecommendations(Integer customerId) {
        List<String> recommendations = new ArrayList<>();

        try {
            // 这里可以根据客户的健康状况、年龄等信息提供个性化建议
            // 暂时提供通用建议
            recommendations.add("每日保证充足的蛋白质摄入");
            recommendations.add("多吃新鲜蔬菜和水果");
            recommendations.add("适量摄入优质脂肪");
            recommendations.add("保持饮食规律，定时定量");
            recommendations.add("多喝水，保持充足的水分摄入");

        } catch (Exception e) {
            recommendations.add("建议咨询专业营养师");
        }

        return recommendations;
    }

    /**
     * 检查饮食禁忌
     */
    public List<String> checkDietaryRestrictions(Integer customerId, List<Integer> foodIds) {
        List<String> restrictions = new ArrayList<>();

        try {
            // 获取客户病史信息进行饮食禁忌检查
            var customer = customerMapper.selectCustomerById(customerId);
            if (customer != null && customer.getMedicalHistory() != null) {
                String medicalHistory = customer.getMedicalHistory().toLowerCase();
                List<Food> foods = foodMapper.selectFoodsByIds(foodIds);

                for (Food food : foods) {
                    // 糖尿病患者少吃高糖食品
                    if (medicalHistory.contains("糖尿病") && isHighSugarFood(food)) {
                        restrictions.add(food.getFoodName() + " - 糖尿病患者应控制摄入");
                    }

                    // 高血压患者少吃高盐食品
                    if (medicalHistory.contains("高血压") && isHighSodiumFood(food)) {
                        restrictions.add(food.getFoodName() + " - 高血压患者应少盐饮食");
                    }

                    // 心脏病患者少吃高脂食品
                    if (medicalHistory.contains("心脏病") && isHighFatFood(food)) {
                        restrictions.add(food.getFoodName() + " - 心脏病患者应控制脂肪摄入");
                    }
                }
            }

        } catch (Exception e) {
            // 查询失败时不报错，返回空列表
        }

        return restrictions;
    }

    /**
     * 判断是否为高糖食品
     */
    private boolean isHighSugarFood(Food food) {
        // 简单判断：甜品类或碳水化合物含量高的食品
        return "甜品".equals(food.getFoodCategory()) ||
                (food.getCarbContent() != null && food.getCarbContent().doubleValue() > 30);
    }

    /**
     * 判断是否为高盐食品
     */
    private boolean isHighSodiumFood(Food food) {
        // 简单判断：腌制品、调料等
        String foodName = food.getFoodName().toLowerCase();
        return foodName.contains("咸") || foodName.contains("腌") || foodName.contains("酱");
    }

    /**
     * 判断是否为高脂食品
     */
    private boolean isHighFatFood(Food food) {
        // 简单判断：脂肪含量高的食品
        return food.getFatContent() != null && food.getFatContent().doubleValue() > 15;
    }
}