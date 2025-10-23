package com.innovation.service;

import com.innovation.entity.User;

public interface UserService {
    /**
     * 用户登录
     */
    User login(String username, String password);
    
    /**
     * 用户注册
     */
    boolean register(User user);

    User selectByUsername(String username);
}