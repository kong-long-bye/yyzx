package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.CustomerFocus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 客户关注数据访问接口
 */
@Mapper
public interface CustomerFocusMapper {

    /**
     * 分页查询客户关注列表（包含客户和护理人员信息）
     */
    List<CustomerFocus> selectFocusListWithDetails(@Param("searchKeyword") String searchKeyword,
                                                   @Param("focusLevel") String focusLevel,
                                                   @Param("status") Integer status,
                                                   @Param("caregiverId") Integer caregiverId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("offset") Integer offset,
                                                   @Param("limit") Integer limit);

    /**
     * 查询客户关注总数
     */
    Integer countFocus(@Param("searchKeyword") String searchKeyword,
                       @Param("focusLevel") String focusLevel,
                       @Param("status") Integer status,
                       @Param("caregiverId") Integer caregiverId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);

    /**
     * 根据ID查询客户关注详细信息
     */
    CustomerFocus selectFocusById(@Param("id") Integer id);

    /**
     * 插入新客户关注
     */
    int insertFocus(CustomerFocus focus);

    /**
     * 更新客户关注信息
     */
    int updateFocus(CustomerFocus focus);

    /**
     * 删除客户关注
     */
    int deleteFocus(@Param("id") Integer id);

    /**
     * 批量删除客户关注
     */
    int deleteFocuses(@Param("ids") List<Integer> ids);

    /**
     * 更新客户关注状态
     */
    int updateFocusStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 根据护理人员ID查询关注客户列表
     */
    List<CustomerFocus> selectFocusesByCaregiverId(@Param("caregiverId") Integer caregiverId,
                                                   @Param("status") Integer status);

    /**
     * 根据客户ID查询关注记录
     */
    List<CustomerFocus> selectFocusesByCustomerId(@Param("customerId") Integer customerId);

    /**
     * 检查客户是否已被关注
     */
    CustomerFocus selectActiveFocusByCustomerAndCaregiver(@Param("customerId") Integer customerId,
                                                          @Param("caregiverId") Integer caregiverId);

    /**
     * 获取关注统计信息
     */
    List<java.util.Map<String, Object>> getFocusStats(@Param("caregiverId") Integer caregiverId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 查询即将到期的关注
     */
    List<CustomerFocus> selectExpiringFocuses(@Param("days") Integer days,
                                              @Param("caregiverId") Integer caregiverId);

    /**
     * 查询高关注级别客户
     */
    List<CustomerFocus> selectHighPriorityFocuses(@Param("caregiverId") Integer caregiverId);

    /**
     * 根据关注级别统计
     */
    List<java.util.Map<String, Object>> getFocusStatsByLevel(@Param("caregiverId") Integer caregiverId);

    /**
     * 批量更新关注状态
     */
    int batchUpdateFocusStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    /**
     * 终止护理人员的所有关注
     */
    int terminateCaregiverFocuses(@Param("caregiverId") Integer caregiverId);

    /**
     * 获取关注历史记录
     */
    List<CustomerFocus> selectFocusHistory(@Param("customerId") Integer customerId,
                                           @Param("limit") Integer limit);
}