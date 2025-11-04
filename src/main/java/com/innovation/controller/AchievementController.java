package com.innovation.controller;

import com.innovation.common.Result;
import com.innovation.entity.Achievement;
import com.innovation.entity.User;
import com.innovation.service.AchievementService;
import com.innovation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserService userService;

    /**
     * 提交或更新项目成绩
     */
    @PostMapping
    public Result<String> submitAchievement(@RequestBody Achievement achievement) {
        try {
            // 获取当前登录教师ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            achievement.setEvaluatorId(currentUser.getUserId());
            
            boolean success = achievementService.saveAchievement(achievement);
            if (success) {
                return Result.success("成绩提交成功");
            } else {
                return Result.fail("成绩提交失败");
            }
        } catch (Exception e) {
            return Result.fail("处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据项目ID查询成绩
     */
    @GetMapping("/project/{projectId}")
    public Result<Achievement> getAchievementByProject(@PathVariable Integer projectId) {
        try {
            Achievement achievement = achievementService.getAchievementByProjectId(projectId);
            return Result.success(achievement);
        } catch (Exception e) {
            return Result.fail("获取成绩失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取教师评定的所有成绩列表
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getTeacherAchievementList() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();
            
            List<Map<String, Object>> achievements = achievementService.getTeacherAchievements(teacherId);
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.fail("获取成绩列表失败: " + e.getMessage());
        }
    }
}