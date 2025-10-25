package com.innovation.controller;

import com.innovation.common.Result;
import com.innovation.entity.Project;
import com.innovation.entity.User;
import com.innovation.service.ProjectService;
import com.innovation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

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
}