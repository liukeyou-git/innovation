package com.innovation.controller;

import com.innovation.common.Result;
import com.innovation.entity.Achievement;
import com.innovation.entity.Project;
import com.innovation.entity.User;
import com.innovation.service.AchievementService;
import com.innovation.service.ProjectService;
import com.innovation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    /**
     * 获取教师列表（供学生选择指导教师）
     */
    @GetMapping("/teachers")
    public Result<List<User>> getTeachersList() {
        try {
            List<User> teachers = projectService.getTeachersList();
            return Result.success(teachers);
        } catch (Exception e) {
            return Result.fail("获取教师列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据学号搜索学生信息
     */
    @GetMapping("/students/search")
    public Result<User> searchStudentByUserId(@RequestParam String studentId) {
        try {
            User student = projectService.searchStudentByStudentId(studentId);
            return Result.success(student);
        } catch (Exception e) {
            return Result.fail("搜索学生失败: " + e.getMessage());
        }
    }

    /**
     * 创建项目申报
     */
    @PostMapping("/projects")
    public Result<String> createProject(@RequestBody Map<String, Object> projectData) {
        try {
            // 获取当前登录用户ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer creatorId = currentUser.getUserId();

            boolean success = projectService.createProject(projectData, creatorId);
            if (success) {
                return Result.success("项目申报成功");
            } else {
                return Result.fail("项目申报失败");
            }
        } catch (Exception e) {
            return Result.fail("创建项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生已申报的项目列表
     */
    @GetMapping("/student/projects")
    public Result<List<Project>> getStudentProjects() {
        try {
            // 获取当前登录用户ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer studentId = currentUser.getUserId();

            List<Project> projects = projectService.getStudentProjects(studentId);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/projects/{projectId}")
    public Result<Map<String, Object>> getProjectDetail(@PathVariable Integer projectId) {
        try {
            Map<String, Object> projectDetail = projectService.getProjectDetail(projectId);
            if (projectDetail != null) {
                return Result.success(projectDetail);
            } else {
                return Result.fail("项目不存在");
            }
        } catch (Exception e) {
            return Result.fail("获取项目详情失败: " + e.getMessage());
        }
    }

    /**
     * 项目结题操作
     */
    @PutMapping("teacher/projects/{projectId}/complete")
    public Result<String> completeProject(
            @PathVariable Integer projectId,
            @RequestBody Map<String, Object> completeData) {
        try {
            // 1. 获取前端传递的带时区的时间字符串（如：2025-10-29T14:38:04.005Z）
            String completeTimeStr = (String) completeData.get("completeTime");

            // 2. 使用OffsetDateTime解析带时区的时间（支持Z标识）
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(completeTimeStr);

            // 3. 转换为LocalDateTime（去除时区信息，仅保留日期时间）
            LocalDateTime completeTime = offsetDateTime.toLocalDateTime();

            // 4. 执行结题操作
            boolean success = projectService.completeProject(projectId, completeTime);
            if (success) {
                return Result.success("项目结题成功");
            } else {
                return Result.fail("项目结题失败");
            }
        } catch (Exception e) {
            return Result.fail("结题处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生参与的已结题项目
     */
    @GetMapping("/student/projects/completed")
    public Result<List<Project>> getStudentCompletedProjects() {
        try {
            // 获取当前登录用户ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer studentId = currentUser.getUserId();

            List<Project> projects = projectService.getStudentCompletedProjects(studentId);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.fail("获取已结题项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师指导的已结题项目
     */
    @GetMapping("teacher/projects/completed")
    public Result<List<Map<String, Object>>> getCompletedProjects() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();

            List<Map<String, Object>> completedProjects = projectService.getTeacherCompletedProjectsWithDetails(teacherId);
            return Result.success(completedProjects);
        } catch (Exception e) {
            return Result.fail("获取已结题项目失败: " + e.getMessage());
        }
    }

    /**
     * 查询项目成绩
     */
    @GetMapping("student/projects/{projectId}/achievement")
    public Result<Achievement> getProjectAchievement(@PathVariable Integer projectId) {
        try {
            Achievement achievement = achievementService.getAchievementByProjectId(projectId);
            return Result.success(achievement);
        } catch (Exception e) {
            return Result.fail("获取成绩失败: " + e.getMessage());
        }
    }
}