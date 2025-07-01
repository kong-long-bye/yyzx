package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 食品数据访问接口
 */
@Mapper
public interface FoodMapper {

    /**
     * 分页查询食品列表
     */
    List<Food> selectFoodList(@Param("keyword") String keyword,
                              @Param("category") String category,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    /**
     * 查询食品总数
     */
    Integer countFoods(@Param("keyword") String keyword,
                       @Param("category") String category);

    /**
     * 根据ID查询食品详细信息
     */
    Food selectFoodById(@Param("id") Integer id);

    /**
     * 根据名称查询食品
     */
    Food selectFoodByName(@Param("foodName") String foodName);

    /**
     * 插入新食品
     */
    int insertFood(Food food);

    /**
     * 更新食品信息
     */
    int updateFood(Food food);

    /**
     * 删除食品
     */
    int deleteFood(@Param("id") Integer id);

    /**
     * 批量删除食品
     */
    int deleteFoods(@Param("ids") List<Integer> ids);

    /**
     * 更新食品状态
     */
    int updateFoodStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 获取所有食品分类
     */
    List<String> selectAllCategories();

    /**
     * 根据分类查询食品
     */
    List<Food> selectFoodsByCategory(@Param("category") String category);

    /**
     * 获取启用状态的食品
     */
    List<Food> selectActiveFoods();

    /**
     * 检查食品名称是否已存在
     */
    Integer checkFoodNameExists(@Param("foodName") String foodName, @Param("excludeId") Integer excludeId);

    /**
     * 根据关键字搜索食品
     */
    List<Food> searchFoodsByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 获取食品统计信息
     */
    List<java.util.Map<String, Object>> getFoodStats();

    /**
     * 根据ID列表查询食品
     */
    List<Food> selectFoodsByIds(@Param("ids") List<Integer> ids);
}