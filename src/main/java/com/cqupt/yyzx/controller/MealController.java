package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.Food;
import com.cqupt.yyzx.entity.MealDiary;
import com.cqupt.yyzx.service.FoodService;
import com.cqupt.yyzx.service.MealDiaryService;
import com.cqupt.yyzx.service.NutritionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 健康管家膳食管理控制器
 */
@RestController
@RequestMapping("/api/health/meal")
@CrossOrigin
public class MealController {

    @Autowired
    private FoodService foodService;

    @Autowired
    private MealDiaryService mealDiaryService;

    @Autowired
    private NutritionAnalysisService nutritionAnalysisService;

    // ==================== 食品管理 ====================

    /**
     * 获取食品列表
     */
    @GetMapping("/foods")
    public Result<Map<String, Object>> getFoodList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = foodService.getFoodList(keyword, category, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询食品列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询食品详细信息
     */
    @GetMapping("/foods/{id}")
    public Result<Food> getFoodById(@PathVariable Integer id) {
        try {
            Food food = foodService.getFoodById(id);
            if (food != null) {
                return Result.success(food);
            } else {
                return Result.error("食品不存在");
            }
        } catch (Exception e) {
            return Result.error("查询食品信息失败：" + e.getMessage());
        }
    }

    /**
     * 添加食品
     */
    @PostMapping("/foods")
    public Result<String> addFood(@RequestBody Food food) {
        try {
            // 基本验证
            if (food.getFoodName() == null || food.getFoodName().trim().isEmpty()) {
                return Result.error("食品名称不能为空");
            }
            if (food.getFoodCategory() == null || food.getFoodCategory().trim().isEmpty()) {
                return Result.error("食品分类不能为空");
            }

            boolean success = foodService.addFood(food);
            if (success) {
                return Result.success("添加食品成功");
            } else {
                return Result.error("食品名称已存在");
            }
        } catch (Exception e) {
            return Result.error("添加食品失败：" + e.getMessage());
        }
    }

    /**
     * 更新食品信息
     */
    @PutMapping("/foods/{id}")
    public Result<String> updateFood(@PathVariable Integer id, @RequestBody Food food) {
        try {
            food.setId(id);
            boolean success = foodService.updateFood(food);
            if (success) {
                return Result.success("更新食品成功");
            } else {
                return Result.error("食品名称已存在");
            }
        } catch (Exception e) {
            return Result.error("更新食品失败：" + e.getMessage());
        }
    }

    /**
     * 删除食品
     */
    @DeleteMapping("/foods/{id}")
    public Result<String> deleteFood(@PathVariable Integer id) {
        try {
            boolean success = foodService.deleteFood(id);
            if (success) {
                return Result.success("删除食品成功");
            } else {
                return Result.error("删除食品失败");
            }
        } catch (Exception e) {
            return Result.error("删除食品失败：" + e.getMessage());
        }
    }

    /**
     * 获取食品分类
     */
    @GetMapping("/foods/categories")
    public Result<List<String>> getFoodCategories() {
        try {
            List<String> categories = foodService.getCategoryOptions();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("查询食品分类失败：" + e.getMessage());
        }
    }

    /**
     * 搜索食品
     */
    @GetMapping("/foods/search")
    public Result<List<Food>> searchFoods(@RequestParam String keyword,
                                          @RequestParam(required = false) Integer limit) {
        try {
            List<Food> foods = foodService.searchFoods(keyword, limit);
            return Result.success(foods);
        } catch (Exception e) {
            return Result.error("搜索食品失败：" + e.getMessage());
        }
    }

    /**
     * 获取启用状态的食品
     */
    @GetMapping("/foods/active")
    public Result<List<Food>> getActiveFoods() {
        try {
            List<Food> foods = foodService.getActiveFoods();
            return Result.success(foods);
        } catch (Exception e) {
            return Result.error("查询启用食品失败：" + e.getMessage());
        }
    }

    // ==================== 膳食记录管理 ====================

    /**
     * 获取膳食记录
     */
    @GetMapping("/records")
    public Result<Map<String, Object>> getMealRecords(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = mealDiaryService.getMealDiaryList(
                    customerId, date, mealType, startDate, endDate, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询膳食记录失败：" + e.getMessage());
        }
    }

    /**
     * 添加膳食记录
     */
    @PostMapping("/records")
    public Result<String> addMealRecord(@RequestBody MealDiary mealDiary) {
        try {
            // 基本验证
            if (mealDiary.getCustomerId() == null) {
                return Result.error("客户不能为空");
            }
            if (mealDiary.getFoodId() == null) {
                return Result.error("食品不能为空");
            }
            if (mealDiary.getMealType() == null || mealDiary.getMealType().trim().isEmpty()) {
                return Result.error("餐次类型不能为空");
            }

            boolean success = mealDiaryService.addMealDiary(mealDiary);
            if (success) {
                return Result.success("添加膳食记录成功");
            } else {
                return Result.error("添加膳食记录失败");
            }
        } catch (Exception e) {
            return Result.error("添加膳食记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除膳食记录
     */
    @DeleteMapping("/records/{id}")
    public Result<String> deleteMealRecord(@PathVariable Integer id) {
        try {
            boolean success = mealDiaryService.deleteMealDiary(id);
            if (success) {
                return Result.success("删除膳食记录成功");
            } else {
                return Result.error("删除膳食记录失败");
            }
        } catch (Exception e) {
            return Result.error("删除膳食记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取今日膳食
     */
    @GetMapping("/records/today/{customerId}")
    public Result<List<MealDiary>> getTodayMealRecords(@PathVariable Integer customerId) {
        try {
            List<MealDiary> records = mealDiaryService.getTodayMealDiaries(customerId);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询今日膳食失败：" + e.getMessage());
        }
    }

    /**
     * 快速添加膳食记录
     */
    @PostMapping("/records/quick")
    public Result<String> quickAddMealRecord(@RequestBody Map<String, Object> data) {
        try {
            Integer customerId = (Integer) data.get("customerId");
            Integer foodId = (Integer) data.get("foodId");
            String mealType = (String) data.get("mealType");
            BigDecimal quantity = data.get("quantity") != null ? new BigDecimal(data.get("quantity").toString()) : null;
            String notes = (String) data.get("notes");
            Integer caregiverId = (Integer) data.get("caregiverId");

            if (customerId == null || foodId == null || mealType == null) {
                return Result.error("客户、食品和餐次类型不能为空");
            }

            boolean success = mealDiaryService.quickAddMealRecord(customerId, foodId, mealType, quantity, notes, caregiverId);
            if (success) {
                return Result.success("快速添加膳食记录成功");
            } else {
                return Result.error("快速添加膳食记录失败");
            }
        } catch (Exception e) {
            return Result.error("快速添加膳食记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取餐次类型选项
     */
    @GetMapping("/meal-types")
    public Result<List<String>> getMealTypes() {
        try {
            List<String> mealTypes = mealDiaryService.getMealTypeOptions();
            return Result.success(mealTypes);
        } catch (Exception e) {
            return Result.error("查询餐次类型失败：" + e.getMessage());
        }
    }

    // ==================== 营养分析 ====================

    /**
     * 获取营养统计
     */
    @GetMapping("/nutrition/{customerId}")
    public Result<Map<String, Object>> getNutritionStats(
            @PathVariable Integer customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Map<String, Object> stats = mealDiaryService.getNutritionStats(customerId, startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取营养统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取膳食类型统计
     */
    @GetMapping("/stats/meal-types/{customerId}")
    public Result<List<Map<String, Object>>> getMealTypeStats(
            @PathVariable Integer customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<Map<String, Object>> stats = mealDiaryService.getMealTypeStats(customerId, startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取膳食类型统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取热门食品统计
     */
    @GetMapping("/stats/popular-foods")
    public Result<List<Map<String, Object>>> getPopularFoodsStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer limit) {
        try {
            List<Map<String, Object>> stats = mealDiaryService.getPopularFoodsStats(startDate, endDate, limit);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取热门食品统计失败：" + e.getMessage());
        }
    }

    // ==================== 安全检查 ====================

    /**
     * 检查过敏源
     */
    @PostMapping("/safety-check")
    public Result<Map<String, Object>> performSafetyCheck(@RequestBody Map<String, Object> checkData) {
        try {
            Integer customerId = (Integer) checkData.get("customerId");
            @SuppressWarnings("unchecked")
            List<Integer> foodIds = (List<Integer>) checkData.get("foodIds");

            if (customerId == null || foodIds == null || foodIds.isEmpty()) {
                return Result.error("客户ID和食品列表不能为空");
            }

            Map<String, Object> result = nutritionAnalysisService.performSafetyCheck(customerId, foodIds);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("安全检查失败：" + e.getMessage());
        }
    }

    /**
     * 计算食物营养价值
     */
    @PostMapping("/calculate-nutrition")
    public Result<Map<String, Object>> calculateNutrition(@RequestBody Map<String, Object> data) {
        try {
            Integer foodId = (Integer) data.get("foodId");
            Double quantity = ((Number) data.get("quantity")).doubleValue();

            if (foodId == null || quantity == null || quantity <= 0) {
                return Result.error("食品ID和数量不能为空且必须大于0");
            }

            Map<String, Object> nutrition = nutritionAnalysisService.calculateNutrition(foodId, quantity);
            return Result.success(nutrition);
        } catch (Exception e) {
            return Result.error("计算营养价值失败：" + e.getMessage());
        }
    }

    /**
     * 获取食物搭配建议
     */
    @PostMapping("/food-suggestions")
    public Result<List<String>> getFoodCombinationSuggestions(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> foodIds = (List<Integer>) data.get("foodIds");

            if (foodIds == null || foodIds.isEmpty()) {
                return Result.error("食品列表不能为空");
            }

            List<String> suggestions = nutritionAnalysisService.getFoodCombinationSuggestions(foodIds);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.error("获取搭配建议失败：" + e.getMessage());
        }
    }
}