package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.Role;
import com.cqupt.yyzx.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色业务逻辑服务
 */
@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 分页查询角色列表
     */
    public Map<String, Object> getRoleList(String searchKeyword, Integer status,
                                           Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<Role> roles = roleMapper.selectRoleList(searchKeyword, status, offset, size);
        Integer total = roleMapper.countRoles(searchKeyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("roles", roles);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询角色详细信息
     */
    public Role getRoleById(Integer id) {
        return roleMapper.selectRoleById(id);
    }

    /**
     * 添加角色
     */
    @Transactional
    public boolean addRole(Role role) {
        try {
            // 检查角色编码是否已存在
            if (roleMapper.checkRoleCodeExists(role.getRoleCode(), null) > 0) {
                throw new RuntimeException("角色编码已存在");
            }

            // 检查角色名称是否已存在
            if (roleMapper.checkRoleNameExists(role.getRoleName(), null) > 0) {
                throw new RuntimeException("角色名称已存在");
            }

            // 设置默认值
            if (role.getStatus() == null) {
                role.setStatus(1); // 默认启用
            }

            return roleMapper.insertRole(role) > 0;

        } catch (Exception e) {
            throw new RuntimeException("添加角色失败：" + e.getMessage());
        }
    }

    /**
     * 更新角色信息
     */
    @Transactional
    public boolean updateRole(Role role) {
        try {
            // 检查角色编码是否已存在（排除当前角色）
            if (roleMapper.checkRoleCodeExists(role.getRoleCode(), role.getId()) > 0) {
                throw new RuntimeException("角色编码已存在");
            }

            // 检查角色名称是否已存在（排除当前角色）
            if (roleMapper.checkRoleNameExists(role.getRoleName(), role.getId()) > 0) {
                throw new RuntimeException("角色名称已存在");
            }

            return roleMapper.updateRole(role) > 0;

        } catch (Exception e) {
            throw new RuntimeException("更新角色失败：" + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @Transactional
    public boolean deleteRole(Integer id) {
        // 检查是否为系统预设角色
        Role role = roleMapper.selectRoleById(id);
        if (role != null && (role.getId() <= 3)) { // 假设前3个为系统预设角色
            throw new RuntimeException("不能删除系统预设角色");
        }

        // 检查角色是否有关联用户
        if (roleMapper.checkRoleHasUsers(id) > 0) {
            throw new RuntimeException("该角色下还有用户，无法删除");
        }

        return roleMapper.deleteRole(id) > 0;
    }

    /**
     * 批量删除角色
     */
    @Transactional
    public boolean deleteRoles(List<Integer> ids) {
        // 检查是否包含系统预设角色
        for (Integer id : ids) {
            Role role = roleMapper.selectRoleById(id);
            if (role != null && (role.getId() <= 3)) {
                throw new RuntimeException("批量删除中包含系统预设角色，无法删除");
            }

            // 检查角色是否有关联用户
            if (roleMapper.checkRoleHasUsers(id) > 0) {
                throw new RuntimeException("角色 " + role.getRoleName() + " 下还有用户，无法删除");
            }
        }

        return roleMapper.deleteRoles(ids) > 0;
    }

    /**
     * 更新角色状态
     */
    @Transactional
    public boolean updateRoleStatus(Integer id, Integer status) {
        // 检查是否为系统预设角色
        Role role = roleMapper.selectRoleById(id);
        if (role != null && (role.getId() <= 3) && status == 0) {
            throw new RuntimeException("不能禁用系统预设角色");
        }

        return roleMapper.updateRoleStatus(id, status) > 0;
    }

    /**
     * 获取所有启用的角色列表
     */
    public List<Role> getActiveRoles() {
        return roleMapper.selectActiveRoles();
    }

    /**
     * 检查角色编码是否可用
     */
    public boolean isRoleCodeAvailable(String roleCode, Integer excludeId) {
        return roleMapper.checkRoleCodeExists(roleCode, excludeId) == 0;
    }

    /**
     * 检查角色名称是否可用
     */
    public boolean isRoleNameAvailable(String roleName, Integer excludeId) {
        return roleMapper.checkRoleNameExists(roleName, excludeId) == 0;
    }

    /**
     * 获取角色统计信息
     */
    public Map<String, Object> getRoleStats() {
        List<Map<String, Object>> stats = roleMapper.getRoleStats();

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int active = 0;
        int inactive = 0;

        for (Map<String, Object> stat : stats) {
            Integer status = (Integer) stat.get("status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            total += countInt;

            if (status == 1) {
                active = countInt;
            } else if (status == 0) {
                inactive = countInt;
            }
        }

        result.put("total", total);
        result.put("active", active);
        result.put("inactive", inactive);

        return result;
    }

    /**
     * 根据角色编码查询角色
     */
    public Role getRoleByCode(String roleCode) {
        return roleMapper.selectRoleByCode(roleCode);
    }

    /**
     * 批量更新角色状态
     */
    @Transactional
    public boolean batchUpdateRoleStatus(List<Integer> ids, Integer status) {
        try {
            // 如果是禁用操作，检查是否包含系统预设角色
            if (status == 0) {
                for (Integer id : ids) {
                    Role role = roleMapper.selectRoleById(id);
                    if (role != null && (role.getId() <= 3)) {
                        throw new RuntimeException("批量操作中包含系统预设角色，无法禁用");
                    }
                }
            }

            for (Integer id : ids) {
                updateRoleStatus(id, status);
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException("批量更新状态失败：" + e.getMessage());
        }
    }
}