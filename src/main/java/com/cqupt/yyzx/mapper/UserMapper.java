package com.cqupt.yyzx.mapper;




import com.cqupt.yyzx.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户名和密码查询用户
     */
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    /**
     * 插入新用户
     */
    int insert(User user);

    /**
     * 更新最后登录时间
     */
    int updateLastLoginTime(@Param("id") Integer id);


    /**
     * 分页查询用户列表（包含角色信息）
     */
    List<User> selectUserListWithRole(@Param("searchKeyword") String searchKeyword,
                                      @Param("roleId") Integer roleId,
                                      @Param("status") Integer status,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    /**
     * 查询用户总数
     */
    Integer countUsers(@Param("searchKeyword") String searchKeyword,
                       @Param("roleId") Integer roleId,
                       @Param("status") Integer status);

    /**
     * 根据ID查询用户详细信息
     */
    User selectUserById(@Param("id") Integer id);

    /**
     * 更新用户信息
     */
    int updateUser(User user);

    /**
     * 删除用户
     */
    int deleteUser(@Param("id") Integer id);

    /**
     * 批量删除用户
     */
    int deleteUsers(@Param("ids") List<Integer> ids);

    /**
     * 更新用户状态
     */
    int updateUserStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 重置用户密码
     */
    int resetPassword(@Param("id") Integer id, @Param("password") String password);

    /**
     * 检查用户名是否已存在
     */
    Integer checkUsernameExists(@Param("username") String username, @Param("excludeId") Integer excludeId);

    /**
     * 检查邮箱是否已存在
     */
    Integer checkEmailExists(@Param("email") String email, @Param("excludeId") Integer excludeId);

    /**
     * 获取用户统计信息
     */
    List<java.util.Map<String, Object>> getUserStats();

    /**
     * 查询最近注册的用户
     */
    List<User> selectRecentUsers(@Param("limit") Integer limit);

    /**
     * 根据角色ID查询用户列表
     */
    List<User> selectUsersByRoleId(@Param("roleId") Integer roleId);

    /**
     * 批量更新用户状态
     */
    int batchUpdateUserStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);
}
