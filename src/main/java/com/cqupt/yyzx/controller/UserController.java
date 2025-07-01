package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.User;
import com.cqupt.yyzx.entity.Role;
import com.cqupt.yyzx.service.UserService;
import com.cqupt.yyzx.service.RoleService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Api(tags = "用户管理", description = "用户管理相关接口")
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
    @ApiOperation(value = "用户登录", notes = "用户通过用户名和密码登录系统")
    @ApiResponses({
            @ApiResponse(code = 200, message = "登录成功"),
            @ApiResponse(code = 400, message = "用户名或密码错误")
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @ApiParam(value = "登录信息", required = true, example = "{\"username\":\"admin\",\"password\":\"123456\"}")
            @RequestBody Map<String, String> loginData) {
        // 原有代码不变...
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
    @ApiOperation(value = "用户注册", notes = "注册新用户账号")
    @ApiResponses({
            @ApiResponse(code = 200, message = "注册成功"),
            @ApiResponse(code = 400, message = "用户名已存在或参数错误")
    })
    @PostMapping("/register")
    public Result<String> register(
            @ApiParam(value = "用户信息", required = true) @RequestBody User user) {
        // 原有代码不变...
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
    @ApiOperation(value = "分页查询用户列表", notes = "根据条件分页查询用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchKeyword", value = "搜索关键字（用户名、真实姓名、手机号）", paramType = "query"),
            @ApiImplicitParam(name = "roleId", value = "角色ID", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "status", value = "用户状态（1-正常，0-禁用）", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "page", value = "页码", paramType = "query", defaultValue = "1", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页大小", paramType = "query", defaultValue = "10", dataType = "int")
    })
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
    @ApiOperation(value = "根据ID查询用户", notes = "通过用户ID获取用户详细信息")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataType = "int")
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
    @ApiOperation(value = "添加用户", notes = "创建新的用户账号")
    @PostMapping("/add")
    public Result<String> addUser(
            @ApiParam(value = "用户信息", required = true) @RequestBody User user) {
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
    @ApiOperation(value = "更新用户信息", notes = "修改用户的基本信息")
    @PutMapping("/update")
    public Result<String> updateUser(
            @ApiParam(value = "用户信息", required = true) @RequestBody User user) {
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
    @ApiOperation(value = "删除用户", notes = "根据用户ID删除用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataType = "int")
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
    @ApiOperation(value = "批量删除用户", notes = "根据用户ID列表批量删除用户")
    @DeleteMapping("/batch")
    public Result<String> deleteUsers(
            @ApiParam(value = "用户ID列表", required = true, example = "[1,2,3]") @RequestBody List<Integer> ids) {
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
    @ApiOperation(value = "更新用户状态", notes = "启用或禁用用户账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataType = "int"),
            @ApiImplicitParam(name = "status", value = "状态（1-启用，0-禁用）", required = true, paramType = "query", dataType = "int")
    })
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
    @ApiOperation(value = "重置用户密码", notes = "管理员重置用户密码，如果不提供新密码则重置为默认密码")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataType = "int")
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Integer id,
                                        @ApiParam(value = "密码信息", example = "{\"password\":\"123456\"}") @RequestBody(required = false) Map<String, String> data) {
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
    @ApiOperation(value = "检查用户名可用性", notes = "检查用户名是否已被使用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, paramType = "query"),
            @ApiImplicitParam(name = "excludeId", value = "排除的用户ID（编辑时使用）", paramType = "query", dataType = "int")
    })
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
    @ApiOperation(value = "检查邮箱可用性", notes = "检查邮箱地址是否已被使用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱地址", required = true, paramType = "query"),
            @ApiImplicitParam(name = "excludeId", value = "排除的用户ID（编辑时使用）", paramType = "query", dataType = "int")
    })
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
    @ApiOperation(value = "获取用户统计", notes = "获取用户数量、状态等统计信息")
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
    @ApiOperation(value = "查询最近注册用户", notes = "获取最近注册的用户列表")
    @ApiImplicitParam(name = "limit", value = "返回数量限制", paramType = "query", defaultValue = "10", dataType = "int")
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
    @ApiOperation(value = "根据角色查询用户", notes = "获取指定角色的所有用户")
    @ApiImplicitParam(name = "roleId", value = "角色ID", required = true, paramType = "path", dataType = "int")
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
    @ApiOperation(value = "批量更新用户状态", notes = "批量启用或禁用用户账号")
    @PutMapping("/batch-status")
    public Result<String> batchUpdateUserStatus(
            @ApiParam(value = "批量操作数据", required = true, example = "{\"ids\":[1,2,3],\"status\":1}")
            @RequestBody Map<String, Object> data) {
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
    @ApiOperation(value = "修改密码", notes = "用户修改自己的登录密码")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataType = "int")
    @PutMapping("/{id}/change-password")
    public Result<String> changePassword(@PathVariable Integer id,
                                         @ApiParam(value = "密码修改信息", required = true, example = "{\"oldPassword\":\"123456\",\"newPassword\":\"newpass123\"}")
                                         @RequestBody Map<String, String> passwordData) {
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
    @ApiOperation(value = "获取角色列表", notes = "获取所有可用角色，用于下拉选择")
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