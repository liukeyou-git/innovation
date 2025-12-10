package com.innovation.service.impl;

import com.innovation.entity.User;
import com.innovation.mapper.UserMapper;
import com.innovation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User login(String username, String password) {
        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        // 验证密码
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            return false;
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setStatus(1);
        // 插入数据库
        return userMapper.insert(user) > 0;
    }

    @Override
    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public List<User> getUsersByKeyword(String keyword) {
        return userMapper.selectUsersByKeyword(keyword);
    }

    @Override
    public boolean deleteUserById(Integer userId) {
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public boolean updateUser(User user) {
        // 如果更新密码，需要重新加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 检查密码是否已加密（简单判断是否包含BCrypt特征字符）
            if (!user.getPassword().startsWith("$2a$") &&
                    !user.getPassword().startsWith("$2b$") &&
                    !user.getPassword().startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            // 如果密码为空，不更新密码字段
            user.setPassword(null);
        }
        return userMapper.update(user) > 0;
    }

    // com/innovation/service/impl/UserServiceImpl.java
    @Override
    public boolean updateUserStatus(Integer userId, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return userMapper.updateStatus(userId, status, now) > 0;
    }
}