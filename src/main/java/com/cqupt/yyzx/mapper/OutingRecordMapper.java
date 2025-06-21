package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.OutingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 外出记录数据访问接口
 */
@Mapper
public interface OutingRecordMapper {

    /**
     * 分页查询外出记录列表（包含客户和登记人信息）
     */
    List<OutingRecord> selectOutingRecordListWithDetails(@Param("searchKeyword") String searchKeyword,
                                                         @Param("approvalStatus") String approvalStatus,
                                                         @Param("outingDate") LocalDate outingDate,
                                                         @Param("customerId") Integer customerId,
                                                         @Param("offset") Integer offset,
                                                         @Param("limit") Integer limit);

    /**
     * 查询外出记录总数
     */
    Integer countOutingRecords(@Param("searchKeyword") String searchKeyword,
                               @Param("approvalStatus") String approvalStatus,
                               @Param("outingDate") LocalDate outingDate,
                               @Param("customerId") Integer customerId);

    /**
     * 根据ID查询外出记录详细信息
     */
    OutingRecord selectOutingRecordById(@Param("id") Integer id);

    /**
     * 插入新外出记录
     */
    int insertOutingRecord(OutingRecord outingRecord);

    /**
     * 更新外出记录信息
     */
    int updateOutingRecord(OutingRecord outingRecord);

    /**
     * 删除外出记录
     */
    int deleteOutingRecord(@Param("id") Integer id);

    /**
     * 批量删除外出记录
     */
    int deleteOutingRecords(@Param("ids") List<Integer> ids);

    /**
     * 更新外出记录审批状态
     */
    int updateOutingRecordApprovalStatus(@Param("id") Integer id, @Param("approvalStatus") String approvalStatus);

    /**
     * 更新外出记录实际返回时间
     */
    int updateOutingRecordActualReturnTime(@Param("id") Integer id, @Param("actualReturnTime") LocalTime actualReturnTime);

    /**
     * 查询客户当日外出记录
     */
    List<OutingRecord> selectTodayOutingRecordsByCustomer(@Param("customerId") Integer customerId);

    /**
     * 查询未返回的外出记录
     */
    List<OutingRecord> selectUnreturnedOutingRecords();

    /**
     * 查询超时未返回的外出记录
     */
    List<OutingRecord> selectOverdueOutingRecords();

    /**
     * 查询待审批的外出申请
     */
    List<OutingRecord> selectPendingApprovalOutingRecords();

    /**
     * 获取外出记录统计信息
     */
    List<java.util.Map<String, Object>> getOutingRecordStats(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询客户的外出历史记录
     */
    List<OutingRecord> selectOutingHistoryByCustomer(@Param("customerId") Integer customerId,
                                                     @Param("limit") Integer limit);

    /**
     * 检查客户是否有未完成的外出记录
     */
    Integer checkCustomerHasUnfinishedOuting(@Param("customerId") Integer customerId);
}