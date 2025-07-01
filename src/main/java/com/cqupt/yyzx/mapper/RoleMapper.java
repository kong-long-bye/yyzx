package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问接口
 */
@Mapper
public interface RoleMapper {

    /**
     * 分页查询角色列表
     */
    List<Role> selectRoleList(@Param("searchKeyword") String searchKeyword,
                              @Param("status") Integer status,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    /**
     * 查询角色总数
     */
    Integer countRoles(@Param("searchKeyword") String searchKeyword,
                       @Param("status") Integer status);

    /**
     * 根据ID查询角色详细信息
     */
    Role selectRoleById(@Param("id") Integer id);

    /**
     * 根据角色编码查询角色
     */
    Role selectRoleByCode(@Param("roleCode") String roleCode);

    /**
     * 插入新角色
     */
    int insertRole(Role role);

    /**
     * 更新角色信息
     */
    int updateRole(Role role);

    /**
     * 删除角色
     */
    int deleteRole(@Param("id") Integer id);

    /**
     * 批量删除角色
     */
    int deleteRoles(@Param("ids") List<Integer> ids);

    /**
     * 更新角色状态
     */
    int updateRoleStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 获取所有启用的角色列表
     */
    List<Role> selectActiveRoles();

    /**
     * 检查角色编码是否已存在
     */
    Integer checkRoleCodeExists(@Param("roleCode") String roleCode, @Param("excludeId") Integer excludeId);

    /**
     * 检查角色名称是否已存在
     */
    Integer checkRoleNameExists(@Param("roleName") String roleName, @Param("excludeId") Integer excludeId);

    /**
     * 获取角色统计信息
     */
    List<java.util.Map<String, Object>> getRoleStats();

    /**
     * 检查角色是否有关联用户
     */
    Integer checkRoleHasUsers(@Param("roleId") Integer roleId);
}