package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.CheckoutInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 退住信息数据访问接口
 */
@Mapper
public interface CheckoutInfoMapper {

    /**
     * 分页查询退住信息列表
     */
    List<CheckoutInfo> selectCheckoutInfoList(@Param("searchKeyword") String searchKeyword,
                                              @Param("checkoutType") String checkoutType,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);

    /**
     * 查询退住信息总数
     */
    Integer countCheckoutInfos(@Param("searchKeyword") String searchKeyword,
                               @Param("checkoutType") String checkoutType,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    /**
     * 根据ID查询退住信息详细信息
     */
    CheckoutInfo selectCheckoutInfoById(@Param("id") Integer id);

    /**
     * 插入新退住信息
     */
    int insertCheckoutInfo(CheckoutInfo checkoutInfo);

    /**
     * 更新退住信息
     */
    int updateCheckoutInfo(CheckoutInfo checkoutInfo);

    /**
     * 删除退住信息
     */
    int deleteCheckoutInfo(@Param("id") Integer id);

    /**
     * 批量删除退住信息
     */
    int deleteCheckoutInfos(@Param("ids") List<Integer> ids);

    /**
     * 获取退住统计信息
     */
    List<java.util.Map<String, Object>> getCheckoutStats(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 查询最近的退住记录
     */
    List<CheckoutInfo> selectRecentCheckouts(@Param("limit") Integer limit);

    /**
     * 根据退住类型统计
     */
    List<java.util.Map<String, Object>> getCheckoutStatsByType();
}