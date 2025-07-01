package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.User;
import com.cqupt.yyzx.entity.Role;
import com.cqupt.yyzx.service.UserService;
import com.cqupt.yyzx.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin // 允许跨域访问
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        User user = userService.login(username, password);
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("token", "simple_token_" + user.getId()); // 简单的token生成
            return Result.success("登录成功", data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            return Result.error("真实姓名不能为空");
        }

        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error("用户名已存在");
        }
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = userService.getUserList(searchKeyword, roleId, status, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户详细信息
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            return Result.error("查询用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 添加用户
     */
    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        try {
            // 基本验证
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
                return Result.error("真实姓名不能为空");
            }
            if (user.getRoleId() == null) {
                return Result.error("角色不能为空");
            }

            boolean success = userService.addUser(user);
            if (success) {
                return Result.success("添加用户成功");
            } else {
                return Result.error("添加用户失败");
            }
        } catch (Exception e) {
            return Result.error("添加用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        try {
            // 基本验证
            if (user.getId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
                return Result.error("真实姓名不能为空");
            }
            if (user.getRoleId() == null) {
                return Result.error("角色不能为空");
            }

            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success("更新用户成功");
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            return Result.error("更新用户失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Integer id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return Result.success("删除用户成功");
            } else {
                return Result.error("删除用户失败");
            }
        } catch (Exception e) {
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result<String> deleteUsers(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的用户");
            }

            boolean success = userService.deleteUsers(ids);
            if (success) {
                return Result.success("批量删除用户成功");
            } else {
                return Result.error("批量删除用户失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                return Result.success("更新用户状态成功");
            } else {
                return Result.error("更新用户状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新用户状态失败：" + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> data) {
        try {
            String newPassword = null;
            if (data != null) {
                newPassword = data.get("password");
            }

            boolean success = userService.resetPassword(id, newPassword);
            if (success) {
                return Result.success("重置密码成功");
            } else {
                return Result.error("重置密码失败");
            }
        } catch (Exception e) {
            return Result.error("重置密码失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username,
                                         @RequestParam(required = false) Integer excludeId) {
        try {
            boolean available = userService.isUsernameAvailable(username, excludeId);
            return Result.success(available);
        } catch (Exception e) {
            return Result.error("检查用户名失败：" + e.getMessage());
        }
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email,
                                      @RequestParam(required = false) Integer excludeId) {
        try {
            boolean available = userService.isEmailAvailable(email, excludeId);
            return Result.success(available);
        } catch (Exception e) {
            return Result.error("检查邮箱失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = userService.getUserStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取用户统计失败：" + e.getMessage());
        }
    }

    /**
     * 查询最近注册的用户
     */
    @GetMapping("/recent")
    public Result<List<User>> getRecentUsers(@RequestParam(required = false) Integer limit) {
        try {
            List<User> users = userService.getRecentUsers(limit);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("查询最近用户失败：" + e.getMessage());
        }
    }

    /**
     * 根据角色ID查询用户列表
     */
    @GetMapping("/role/{roleId}")
    public Result<List<User>> getUsersByRoleId(@PathVariable Integer roleId) {
        try {
            List<User> users = userService.getUsersByRoleId(roleId);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("查询角色用户失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新用户状态
     */
    @PutMapping("/batch-status")
    public Result<String> batchUpdateUserStatus(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) data.get("ids");
            Integer status = (Integer) data.get("status");

            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的用户");
            }

            boolean success = userService.batchUpdateUserStatus(ids, status);
            if (success) {
                return Result.success("批量更新状态成功");
            } else {
                return Result.error("批量更新状态失败");
            }
        } catch (Exception e) {
            return Result.error("批量更新状态失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/change-password")
    public Result<String> changePassword(@PathVariable Integer id, @RequestBody Map<String, String> passwordData) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error("原密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }

            boolean success = userService.changePassword(id, oldPassword, newPassword);
            if (success) {
                return Result.success("修改密码成功");
            } else {
                return Result.error("修改密码失败");
            }
        } catch (Exception e) {
            return Result.error("修改密码失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有角色列表（用于下拉选择）
     */
    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles() {
        try {
            List<Role> roles = roleService.getActiveRoles();
            return Result.success(roles);
        } catch (Exception e) {
            return Result.error("查询角色列表失败：" + e.getMessage());
        }
    }
}