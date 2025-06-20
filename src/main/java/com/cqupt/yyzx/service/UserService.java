package com.cqupt.yyzx.service;


import com.cqupt.yyzx.entity.User;

import com.cqupt.yyzx.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户业务逻辑
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
}
