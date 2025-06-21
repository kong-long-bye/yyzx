package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户数据访问接口
 */
@Mapper
public interface CustomerMapper {

    /**
     * 分页查询客户列表（包含床位和护理信息）
     */
    List<Customer> selectCustomerListWithDetails(@Param("searchKeyword") String searchKeyword,
                                                 @Param("auditStatus") String auditStatus,
                                                 @Param("status") Integer status,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);

    /**
     * 查询客户总数
     */
    Integer countCustomers(@Param("searchKeyword") String searchKeyword,
                           @Param("auditStatus") String auditStatus,
                           @Param("status") Integer status);

    /**
     * 根据ID查询客户详细信息
     */
    Customer selectCustomerById(@Param("id") Integer id);

    /**
     * 根据身份证号查询客户
     */
    Customer selectCustomerByIdCard(@Param("idCard") String idCard);

    /**
     * 插入新客户
     */
    int insertCustomer(Customer customer);

    /**
     * 更新客户信息
     */
    int updateCustomer(Customer customer);

    /**
     * 删除客户
     */
    int deleteCustomer(@Param("id") Integer id);

    /**
     * 批量删除客户
     */
    int deleteCustomers(@Param("ids") List<Integer> ids);

    /**
     * 更新客户审核状态
     */
    int updateCustomerAuditStatus(@Param("id") Integer id, @Param("auditStatus") String auditStatus);

    /**
     * 更新客户状态
     */
    int updateCustomerStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 客户入住登记
     */
    int checkinCustomer(Customer customer);

    /**
     * 客户退住登记
     */
    int checkoutCustomer(@Param("id") Integer id, @Param("checkoutInfoId") Integer checkoutInfoId);

    /**
     * 查询可用床位列表
     */
    List<java.util.Map<String, Object>> selectAvailableBeds();

    /**
     * 查询护理人员列表
     */
    List<java.util.Map<String, Object>> selectCaregiversList();

    /**
     * 检查身份证号是否已存在
     */
    Integer checkIdCardExists(@Param("idCard") String idCard, @Param("excludeId") Integer excludeId);

    /**
     * 获取客户统计信息
     */
    List<java.util.Map<String, Object>> getCustomerStats();

    /**
     * 查询即将到期的护理协议
     */
    List<Customer> selectCustomersWithExpiredAgreements(@Param("days") Integer days);

    /**
     * 查询待审核的客户列表
     */
    List<Customer> selectPendingAuditCustomers();
}