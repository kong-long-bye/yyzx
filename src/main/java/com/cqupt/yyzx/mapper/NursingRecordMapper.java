package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.NursingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 护理记录数据访问接口
 */
@Mapper
public interface NursingRecordMapper {

    /**
     * 分页查询护理记录列表（包含客户、项目、护理人员信息）
     */
    List<NursingRecord> selectRecordListWithDetails(@Param("searchKeyword") String searchKeyword,
                                                    @Param("executionStatus") String executionStatus,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    @Param("customerId") Integer customerId,
                                                    @Param("projectId") Integer projectId,
                                                    @Param("caregiverId") Integer caregiverId,
                                                    @Param("offset") Integer offset,
                                                    @Param("limit") Integer limit);

    /**
     * 查询护理记录总数
     */
    Integer countRecords(@Param("searchKeyword") String searchKeyword,
                         @Param("executionStatus") String executionStatus,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("customerId") Integer customerId,
                         @Param("projectId") Integer projectId,
                         @Param("caregiverId") Integer caregiverId);

    /**
     * 根据ID查询护理记录详细信息
     */
    NursingRecord selectRecordById(@Param("id") Integer id);

    /**
     * 插入新护理记录
     */
    int insertRecord(NursingRecord record);

    /**
     * 更新护理记录信息
     */
    int updateRecord(NursingRecord record);

    /**
     * 删除护理记录
     */
    int deleteRecord(@Param("id") Integer id);

    /**
     * 批量删除护理记录
     */
    int deleteRecords(@Param("ids") List<Integer> ids);

    /**
     * 根据客户ID查询护理记录
     */
    List<NursingRecord> selectRecordsByCustomerId(@Param("customerId") Integer customerId,
                                                  @Param("limit") Integer limit);

    /**
     * 根据护理人员ID查询护理记录
     */
    List<NursingRecord> selectRecordsByCaregiverId(@Param("caregiverId") Integer caregiverId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 根据项目ID查询护理记录
     */
    List<NursingRecord> selectRecordsByProjectId(@Param("projectId") Integer projectId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 查询今日护理记录
     */
    List<NursingRecord> selectTodayRecords(@Param("caregiverId") Integer caregiverId);

    /**
     * 查询未完成的护理记录
     */
    List<NursingRecord> selectUncompletedRecords();

    /**
     * 获取护理记录统计信息
     */
    List<java.util.Map<String, Object>> getRecordStats(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * 获取护理人员工作量统计
     */
    List<java.util.Map<String, Object>> getCaregiverWorkloadStats(@Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    /**
     * 获取项目执行统计
     */
    List<java.util.Map<String, Object>> getProjectExecutionStats(@Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);

    /**
     * 获取客户护理统计
     */
    List<java.util.Map<String, Object>> getCustomerCareStats(@Param("customerId") Integer customerId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询重复护理记录
     */
    List<NursingRecord> selectDuplicateRecords(@Param("customerId") Integer customerId,
                                               @Param("projectId") Integer projectId,
                                               @Param("executionDate") LocalDate executionDate);

    /**
     * 批量插入护理记录
     */
    int batchInsertRecords(@Param("records") List<NursingRecord> records);

    /**
     * 更新护理记录状态
     */
    int updateRecordStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 根据条件查询护理记录数量
     */
    Integer getRecordCountByCondition(@Param("customerId") Integer customerId,
                                      @Param("projectId") Integer projectId,
                                      @Param("executionDate") LocalDate executionDate);

    /**
     * 查询护理质量评估数据
     */
    List<java.util.Map<String, Object>> getNursingQualityData(@Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
}