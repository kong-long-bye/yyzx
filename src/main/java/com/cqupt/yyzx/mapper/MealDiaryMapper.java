package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.MealDiary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 膳食日记数据访问接口
 */
@Mapper
public interface MealDiaryMapper {

    /**
     * 分页查询膳食记录列表（包含关联信息）
     */
    List<MealDiary> selectMealDiaryListWithDetails(@Param("customerId") Integer customerId,
                                                   @Param("mealDate") LocalDate mealDate,
                                                   @Param("mealType") String mealType,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("offset") Integer offset,
                                                   @Param("limit") Integer limit);

    /**
     * 查询膳食记录总数
     */
    Integer countMealDiaries(@Param("customerId") Integer customerId,
                             @Param("mealDate") LocalDate mealDate,
                             @Param("mealType") String mealType,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    /**
     * 根据ID查询膳食记录详细信息
     */
    MealDiary selectMealDiaryById(@Param("id") Integer id);

    /**
     * 插入新膳食记录
     */
    int insertMealDiary(MealDiary mealDiary);

    /**
     * 更新膳食记录信息
     */
    int updateMealDiary(MealDiary mealDiary);

    /**
     * 删除膳食记录
     */
    int deleteMealDiary(@Param("id") Integer id);

    /**
     * 批量删除膳食记录
     */
    int deleteMealDiaries(@Param("ids") List<Integer> ids);

    /**
     * 根据客户ID和日期查询当日膳食记录
     */
    List<MealDiary> selectTodayMealDiaries(@Param("customerId") Integer customerId,
                                           @Param("mealDate") LocalDate mealDate);

    /**
     * 根据客户ID查询指定时间段膳食记录
     */
    List<MealDiary> selectMealDiariesByPeriod(@Param("customerId") Integer customerId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 获取客户营养统计
     */
    java.util.Map<String, Object> getNutritionStats(@Param("customerId") Integer customerId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * 获取客户费用统计
     */
    java.util.Map<String, Object> getCostStats(@Param("customerId") Integer customerId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 根据客户ID查询最近的膳食记录
     */
    List<MealDiary> selectRecentMealDiaries(@Param("customerId") Integer customerId,
                                            @Param("limit") Integer limit);

    /**
     * 检查重复记录
     */
    Integer checkDuplicateRecord(@Param("customerId") Integer customerId,
                                 @Param("mealDate") LocalDate mealDate,
                                 @Param("mealType") String mealType,
                                 @Param("foodId") Integer foodId);

    /**
     * 获取膳食类型统计
     */
    List<java.util.Map<String, Object>> getMealTypeStats(@Param("customerId") Integer customerId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 获取热门食品统计
     */
    List<java.util.Map<String, Object>> getPopularFoodsStats(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate,
                                                             @Param("limit") Integer limit);

    /**
     * 批量插入膳食记录
     */
    int batchInsertMealDiaries(@Param("records") List<MealDiary> records);
}