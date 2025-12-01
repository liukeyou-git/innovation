package com.innovation.service;

import com.innovation.entity.User;

import java.util.List;

public interface UserService {
    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 用户注册
     */
    boolean register(User user);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(String username);

    /**
     * 根据关键字搜索用户
     */
    List<User> getUsersByKeyword(String keyword);

    /**
     * 根据ID删除用户
     */
    boolean deleteUserById(Integer userId);

    /**
     * 更新用户信息
     */
    boolean updateUser(User user);
}