package com.innovation.controller;

import com.innovation.common.Result;
import com.innovation.entity.User;
import com.innovation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
public class AdminController {

    @Autowired
    private UserService userService;

    // com/innovation/controller/AdminController.java
    /**
     * 启用/禁用用户账号
     */
    @PutMapping("/users/{userId}/status")
    public Result<String> updateUserStatus(
            @PathVariable Integer userId,
            @RequestBody Map<String, Integer> statusData) {
        try {
            Integer status = statusData.get("status");
            if (status == null || (status != 0 && status != 1)) {
                return Result.fail("请提供有效的状态值（0-禁用，1-启用）");
            }

            boolean success = userService.updateUserStatus(userId, status);
            if (success) {
                String message = status == 1 ? "用户账号已启用" : "用户账号已禁用";
                return Result.success(message);
            } else {
                return Result.fail("更新用户状态失败");
            }
        } catch (Exception e) {
            return Result.fail("处理用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户列表（支持搜索）
     */
    @GetMapping("/users")
    public Result<List<User>> getUsersList(@RequestParam(required = false) String keyword) {
        try {
            List<User> users = userService.getUsersByKeyword(keyword);
            return Result.success(users);
        } catch (Exception e) {
            return Result.fail("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入用户
     */
    @PostMapping("/users/batch-import")
    public Result<Map<String, Integer>> batchImportUsers(@RequestBody List<User> userList) {
        try {
            // 记录导入结果
            int successCount = 0;
            int failureCount = 0;
            
            for (User user : userList) {
                // 设置创建和更新时间
                LocalDateTime now = LocalDateTime.now();
                user.setCreateTime(now);
                user.setUpdateTime(now);
                user.setStatus(1);
                
                // 尝试注册用户
                if (userService.register(user)) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }
            
            // 构建返回结果
            Map<String, Integer> result = Map.of(
                "successCount", successCount,
                "failureCount", failureCount
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("批量导入用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public Result<String> deleteUser(@PathVariable Integer userId) {
        try {
            boolean success = userService.deleteUserById(userId);
            if (success) {
                return Result.success("用户删除成功");
            } else {
                return Result.fail("用户不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.fail("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{userId}")
    public Result<String> updateUser(
            @PathVariable Integer userId,
            @RequestBody User user) {
        try {
            // 设置用户ID，确保更新的是指定用户
            user.setUserId(userId);
            user.setUpdateTime(LocalDateTime.now());
            
            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success("用户更新成功");
            } else {
                return Result.fail("用户不存在或更新失败");
            }
        } catch (Exception e) {
            return Result.fail("更新用户失败: " + e.getMessage());
        }
    }
}