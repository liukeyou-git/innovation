package com.innovation.controller;

import com.innovation.common.Result;
import com.innovation.entity.User;
import com.innovation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody User user) {
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser != null) {
            // 隐藏密码
            loginUser.setPassword(null);
            return Result.success(loginUser);
        }
        return Result.fail("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        System.out.println("注册请求到达：" + user.getUsername()); // 新增打印
        // 简单验证必要字段
        if (user.getUsername() == null || user.getPassword() == null 
            || user.getRole() == null || user.getRealName() == null) {
            return Result.fail("必填字段不能为空");
        }
        
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.fail("用户名已存在");
        }
    }
}