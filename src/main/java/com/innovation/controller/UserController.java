package com.innovation.controller;

import com.innovation.common.Result;
import com.innovation.entity.User;
import com.innovation.service.UserService;
import com.innovation.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT
            String jwt = jwtUtils.generateToken(user.getUsername());

            // 查询用户完整信息（包含角色）
            User currentUser = userService.selectByUsername(user.getUsername());
            if (currentUser == null) {
                return Result.fail("用户不存在");
            }

            // 构建返回数据（包含token和角色）
            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("role", currentUser.getRole()); // 角色值（0-管理员，1-教师，2-学生）

            return Result.success(data);
        } catch (AuthenticationException e) {
            return Result.fail("用户名或密码错误");
        }
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