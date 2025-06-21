package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.NursingAgreement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 护理协议数据访问接口
 */
@Mapper
public interface NursingAgreementMapper {

    /**
     * 根据客户ID查询有效的护理协议
     */
    NursingAgreement selectActiveAgreementByCustomerId(@Param("customerId") Integer customerId);

    /**
     * 根据客户ID查询所有护理协议
     */
    List<NursingAgreement> selectAgreementsByCustomerId(@Param("customerId") Integer customerId);

    /**
     * 根据ID查询护理协议详细信息
     */
    NursingAgreement selectAgreementById(@Param("id") Integer id);

    /**
     * 插入新护理协议
     */
    int insertAgreement(NursingAgreement agreement);

    /**
     * 更新护理协议信息
     */
    int updateAgreement(NursingAgreement agreement);

    /**
     * 删除护理协议
     */
    int deleteAgreement(@Param("id") Integer id);

    /**
     * 更新护理协议状态
     */
    int updateAgreementStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 终止客户的所有有效协议
     */
    int terminateCustomerAgreements(@Param("customerId") Integer customerId);

    /**
     * 查询即将到期的协议
     */
    List<NursingAgreement> selectExpiredAgreements(@Param("days") Integer days);

    /**
     * 获取护理等级选项
     */
    List<java.util.Map<String, Object>> getNursingLevelOptions();

    /**
     * 获取护理协议统计信息
     */
    List<java.util.Map<String, Object>> getAgreementStats();
}