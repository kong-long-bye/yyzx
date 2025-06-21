package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.CustomerCaregiverAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户护理人员分配数据访问接口
 */
@Mapper
public interface CustomerCaregiverAssignmentMapper {

    /**
     * 根据客户ID查询护理人员分配
     */
    List<CustomerCaregiverAssignment> selectAssignmentsByCustomerId(@Param("customerId") Integer customerId);

    /**
     * 根据护理人员ID查询客户分配
     */
    List<CustomerCaregiverAssignment> selectAssignmentsByCaregiverId(@Param("caregiverId") Integer caregiverId);

    /**
     * 根据ID查询分配详细信息
     */
    CustomerCaregiverAssignment selectAssignmentById(@Param("id") Integer id);

    /**
     * 插入新分配记录
     */
    int insertAssignment(CustomerCaregiverAssignment assignment);

    /**
     * 更新分配记录
     */
    int updateAssignment(CustomerCaregiverAssignment assignment);

    /**
     * 删除分配记录
     */
    int deleteAssignment(@Param("id") Integer id);

    /**
     * 更新分配状态
     */
    int updateAssignmentStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 终止客户的所有护理人员分配
     */
    int terminateCustomerAssignments(@Param("customerId") Integer customerId);

    /**
     * 查询客户的主要护理人员
     */
    CustomerCaregiverAssignment selectPrimaryCaregiverByCustomerId(@Param("customerId") Integer customerId);

    /**
     * 设置主要护理人员
     */
    int setPrimaryCaregiver(@Param("customerId") Integer customerId, @Param("caregiverId") Integer caregiverId);

    /**
     * 取消主要护理人员设置
     */
    int unsetPrimaryCaregiver(@Param("customerId") Integer customerId);

    /**
     * 检查护理人员是否已分配给客户
     */
    Integer checkAssignmentExists(@Param("customerId") Integer customerId, @Param("caregiverId") Integer caregiverId);

    /**
     * 获取护理人员工作负载统计
     */
    List<java.util.Map<String, Object>> getCaregiverWorkloadStats();

    /**
     * 查询分配历史记录
     */
    List<CustomerCaregiverAssignment> selectAssignmentHistory(@Param("customerId") Integer customerId,
                                                              @Param("limit") Integer limit);
}