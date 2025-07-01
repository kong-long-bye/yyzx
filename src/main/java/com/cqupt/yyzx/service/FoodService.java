package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.Food;
import com.cqupt.yyzx.mapper.FoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 食品业务逻辑服务
 */
@Service
public class FoodService {

    @Autowired
    private FoodMapper foodMapper;

    /**
     * 分页查询食品列表
     */
    public Map<String, Object> getFoodList(String keyword, String category, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<Food> foods = foodMapper.selectFoodList(keyword, category, offset, size);
        Integer total = foodMapper.countFoods(keyword, category);

        Map<String, Object> result = new HashMap<>();
        result.put("foods", foods);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询食品详细信息
     */
    public Food getFoodById(Integer id) {
        return foodMapper.selectFoodById(id);
    }

    /**
     * 添加食品
     */
    @Transactional
    public boolean addFood(Food food) {
        // 检查食品名称是否已存在
        if (foodMapper.checkFoodNameExists(food.getFoodName(), null) > 0) {
            return false;
        }

        // 设置默认值
        if (food.getStatus() == null) {
            food.setStatus(1); // 默认启用
        }
        if (food.getUnit() == null || food.getUnit().trim().isEmpty()) {
            food.setUnit("份");
        }
        if (food.getPrice() == null) {
            food.setPrice(new BigDecimal("0.00"));
        }
        if (food.getCaloriesPerUnit() == null) {
            food.setCaloriesPerUnit(new BigDecimal("0.00"));
        }

        return foodMapper.insertFood(food) > 0;
    }

    /**
     * 更新食品信息
     */
    @Transactional
    public boolean updateFood(Food food) {
        // 检查食品名称是否已存在（排除当前食品）
        if (foodMapper.checkFoodNameExists(food.getFoodName(), food.getId()) > 0) {
            return false;
        }

        return foodMapper.updateFood(food) > 0;
    }

    /**
     * 删除食品
     */
    @Transactional
    public boolean deleteFood(Integer id) {
        return foodMapper.deleteFood(id) > 0;
    }

    /**
     * 批量删除食品
     */
    @Transactional
    public boolean deleteFoods(List<Integer> ids) {
        return foodMapper.deleteFoods(ids) > 0;
    }

    /**
     * 更新食品状态
     */
    @Transactional
    public boolean updateFoodStatus(Integer id, Integer status) {
        return foodMapper.updateFoodStatus(id, status) > 0;
    }

    /**
     * 获取所有食品分类
     */
    public List<String> getAllCategories() {
        return foodMapper.selectAllCategories();
    }

    /**
     * 根据分类查询食品
     */
    public List<Food> getFoodsByCategory(String category) {
        return foodMapper.selectFoodsByCategory(category);
    }

    /**
     * 获取启用状态的食品
     */
    public List<Food> getActiveFoods() {
        return foodMapper.selectActiveFoods();
    }

    /**
     * 检查食品名称是否可用
     */
    public boolean isFoodNameAvailable(String foodName, Integer excludeId) {
        return foodMapper.checkFoodNameExists(foodName, excludeId) == 0;
    }

    /**
     * 搜索食品
     */
    public List<Food> searchFoods(String keyword, Integer limit) {
        if (limit == null) limit = 20;
        return foodMapper.searchFoodsByKeyword(keyword, limit);
    }

    /**
     * 获取食品统计信息
     */
    public Map<String, Object> getFoodStats() {
        List<Map<String, Object>> stats = foodMapper.getFoodStats();

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int active = 0;
        int inactive = 0;

        for (Map<String, Object> stat : stats) {
            Integer status = (Integer) stat.get("status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            total += countInt;

            if (status == 1) {
                active = countInt;
            } else if (status == 0) {
                inactive = countInt;
            }
        }

        result.put("total", total);
        result.put("active", active);
        result.put("inactive", inactive);

        return result;
    }

    /**
     * 根据ID列表查询食品
     */
    public List<Food> getFoodsByIds(List<Integer> ids) {
        return foodMapper.selectFoodsByIds(ids);
    }

    /**
     * 批量更新食品状态
     */
    @Transactional
    public boolean batchUpdateFoodStatus(List<Integer> ids, Integer status) {
        try {
            for (Integer id : ids) {
                updateFoodStatus(id, status);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量更新状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取默认食品分类选项
     */
    public List<String> getCategoryOptions() {
        List<String> categories = new ArrayList<>();
        categories.add("主食");
        categories.add("荤菜");
        categories.add("蔬菜");
        categories.add("汤品");
        categories.add("水果");
        categories.add("饮品");
        categories.add("零食");
        categories.add("调料");
        return categories;
    }
}