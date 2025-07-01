package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.User;
import com.cqupt.yyzx.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户业务逻辑服务
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        // MD5加密密码
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 查询用户
        User user = userMapper.findByUsernameAndPassword(username, md5Password);

        if (user != null && user.getStatus() == 1) {
            // 更新最后登录时间
            userMapper.updateLastLoginTime(user.getId());
            // 清空密码，不返回给前端
            user.setPassword(null);
            return user;
        }

        return null;
    }

    /**
     * 用户注册
     */
    public boolean register(User user) {
        // 检查用户名是否已存在
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return false;
        }

        // MD5加密密码
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5Password);

        // 设置默认值
        if (user.getRoleId() == null) {
            user.setRoleId(2); // 默认为健康管家
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }

        return userMapper.insert(user) > 0;
    }

    /**
     * 根据用户名查询用户
     */
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 分页查询用户列表
     */
    public Map<String, Object> getUserList(String searchKeyword, Integer roleId, Integer status,
                                           Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<User> users = userMapper.selectUserListWithRole(searchKeyword, roleId, status, offset, size);
        Integer total = userMapper.countUsers(searchKeyword, roleId, status);

        // 清空密码信息
        users.forEach(user -> user.setPassword(null));

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询用户详细信息
     */
    public User getUserById(Integer id) {
        User user = userMapper.selectUserById(id);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }

    /**
     * 添加用户
     */
    @Transactional
    public boolean addUser(User user) {
        try {
            // 检查用户名是否已存在
            if (userMapper.checkUsernameExists(user.getUsername(), null) > 0) {
                throw new RuntimeException("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                if (userMapper.checkEmailExists(user.getEmail(), null) > 0) {
                    throw new RuntimeException("邮箱已存在");
                }
            }

            // MD5加密密码
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword("123456"); // 默认密码
            }
            String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(md5Password);

            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus(1); // 默认启用
            }
            if (user.getRoleId() == null) {
                user.setRoleId(2); // 默认为健康管家
            }

            return userMapper.insert(user) > 0;

        } catch (Exception e) {
            throw new RuntimeException("添加用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public boolean updateUser(User user) {
        try {
            // 检查用户名是否已存在（排除当前用户）
            if (userMapper.checkUsernameExists(user.getUsername(), user.getId()) > 0) {
                throw new RuntimeException("用户名已存在");
            }

            // 检查邮箱是否已存在（排除当前用户）
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                if (userMapper.checkEmailExists(user.getEmail(), user.getId()) > 0) {
                    throw new RuntimeException("邮箱已存在");
                }
            }

            // 如果密码不为空，则加密
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
                user.setPassword(md5Password);
            } else {
                user.setPassword(null); // 不更新密码
            }

            return userMapper.updateUser(user) > 0;

        } catch (Exception e) {
            throw new RuntimeException("更新用户失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @Transactional
    public boolean deleteUser(Integer id) {
        // 检查是否为系统管理员
        User user = userMapper.selectUserById(id);
        if (user != null && user.getRoleId() == 1) {
            throw new RuntimeException("不能删除系统管理员");
        }

        return userMapper.deleteUser(id) > 0;
    }

    /**
     * 批量删除用户
     */
    @Transactional
    public boolean deleteUsers(List<Integer> ids) {
        // 检查是否包含系统管理员
        for (Integer id : ids) {
            User user = userMapper.selectUserById(id);
            if (user != null && user.getRoleId() == 1) {
                throw new RuntimeException("批量删除中包含系统管理员，无法删除");
            }
        }

        return userMapper.deleteUsers(ids) > 0;
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public boolean updateUserStatus(Integer id, Integer status) {
        // 检查是否为系统管理员
        User user = userMapper.selectUserById(id);
        if (user != null && user.getRoleId() == 1 && status == 0) {
            throw new RuntimeException("不能禁用系统管理员");
        }

        return userMapper.updateUserStatus(id, status) > 0;
    }

    /**
     * 重置用户密码
     */
    @Transactional
    public boolean resetPassword(Integer id, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            newPassword = "123456"; // 默认密码
        }

        String md5Password = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        return userMapper.resetPassword(id, md5Password) > 0;
    }

    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username, Integer excludeId) {
        return userMapper.checkUsernameExists(username, excludeId) == 0;
    }

    /**
     * 检查邮箱是否可用
     */
    public boolean isEmailAvailable(String email, Integer excludeId) {
        return userMapper.checkEmailExists(email, excludeId) == 0;
    }

    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStats() {
        List<Map<String, Object>> stats = userMapper.getUserStats();

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
     * 查询最近注册的用户
     */
    public List<User> getRecentUsers(Integer limit) {
        if (limit == null) limit = 10;
        List<User> users = userMapper.selectRecentUsers(limit);
        // 清空密码信息
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    /**
     * 根据角色ID查询用户列表
     */
    public List<User> getUsersByRoleId(Integer roleId) {
        List<User> users = userMapper.selectUsersByRoleId(roleId);
        // 清空密码信息
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    /**
     * 批量更新用户状态
     */
    @Transactional
    public boolean batchUpdateUserStatus(List<Integer> ids, Integer status) {
        try {
            // 如果是禁用操作，检查是否包含系统管理员
            if (status == 0) {
                for (Integer id : ids) {
                    User user = userMapper.selectUserById(id);
                    if (user != null && user.getRoleId() == 1) {
                        throw new RuntimeException("批量操作中包含系统管理员，无法禁用");
                    }
                }
            }

            return userMapper.batchUpdateUserStatus(ids, status) > 0;

        } catch (Exception e) {
            throw new RuntimeException("批量更新状态失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @Transactional
    public boolean changePassword(Integer id, String oldPassword, String newPassword) {
        try {
            User user = userMapper.selectUserById(id);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 验证旧密码
            String md5OldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
            if (!md5OldPassword.equals(user.getPassword())) {
                throw new RuntimeException("原密码不正确");
            }

            // 更新新密码
            String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
            return userMapper.resetPassword(id, md5NewPassword) > 0;

        } catch (Exception e) {
            throw new RuntimeException("修改密码失败：" + e.getMessage());
        }
    }
}