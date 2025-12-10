package com.innovation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovation.common.ImportResult;
import com.innovation.common.Result;
import com.innovation.entity.Achievement;
import com.innovation.entity.User;
import com.innovation.service.AchievementService;
import com.innovation.service.UserService;
import com.innovation.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * 导出教师评定的所有成绩
     */
    @GetMapping("/export")
    public void exportTeacherAchievements(HttpServletResponse response) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();

            List<Map<String, Object>> achievements = achievementService.getTeacherAchievements(teacherId);

            // 检查数据是否为空
            if (achievements == null || achievements.isEmpty()) {
                throw new RuntimeException("无成绩数据可导出");
            }

            // 生成Excel（确保数据有效）
            String fileName = "成绩列表_" + LocalDate.now() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));

            ExcelUtils.exportAchievements(achievements, fileName, response);
        } catch (Exception e) {
            try {
                response.setContentType("application/json;charset=UTF-8");
                Result<String> result = Result.fail("导出失败: " + e.getMessage());
                response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 批量提交或更新项目成绩
     */
    @PostMapping("/batch")
    public Result<String> batchSubmitAchievements(@RequestBody List<Achievement> achievements) {
        try {
            // 获取当前登录教师ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer evaluatorId = currentUser.getUserId();

            // 设置评定教师ID和评定时间
            LocalDateTime now = LocalDateTime.now();
            for (Achievement achievement : achievements) {
                achievement.setEvaluatorId(evaluatorId);
                achievement.setEvaluationTime(now);
            }

            boolean success = achievementService.batchSaveAchievements(achievements);
            if (success) {
                return Result.success("成绩批量录入成功");
            } else {
                return Result.fail("成绩批量录入失败");
            }
        } catch (Exception e) {
            return Result.fail("处理失败: " + e.getMessage());
        }
    }

    /**
     * 下载成绩导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            String fileName = "成绩导入模板.xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));

            // 构建新模板数据（表头+示例，严格匹配用户提供的格式）
            List<String[]> header = new ArrayList<>();
            // 表头：项目名称、成员姓名、成员学号、分数、等级、教师评语（顺序严格一致）
            header.add(new String[]{"项目名称", "成员姓名", "成员学号", "分数", "等级", "教师评语"});
            // 示例数据（对应表头顺序，使用用户提供的示例）
            header.add(new String[]{"智能家居系统", "张三,李四", "zhangsan,lisi", "88", "良好", "还可以吧，已经很棒了"});

            // 调用工具类生成模板
            ExcelUtils.generateTemplate(header, response.getOutputStream());
        } catch (Exception e) {
            try {
                response.setContentType("application/json;charset=UTF-8");
                Result<String> result = Result.fail("模板下载失败: " + e.getMessage());
                response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // 在AchievementController.java中添加
    /**
     * 批量导入成绩
     */
    @PostMapping("/batch/import")
    public Result<ImportResult<Achievement>> batchImport(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.fail("请选择文件");
            }

            // 1. 解析Excel
            ImportResult<Achievement> parseResult = ExcelUtils.parseAchievementImport(file);
            if (parseResult.hasErrors()) {
                return Result.fail("解析失败");
            }

            // 2. 获取当前教师ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();

            // 3. 验证并导入数据
            ImportResult<Achievement> importResult = achievementService.batchImportAchievements(
                    parseResult.getValidData(),
                    teacherId
            );

            if (importResult.hasErrors()) {
                return Result.fail("部分数据导入失败：" + String.join("；", importResult.getErrorMessages()));
            } else {
                return Result.success(importResult);
            }
        } catch (Exception e) {
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
}