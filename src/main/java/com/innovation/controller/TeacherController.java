package com.innovation.controller;

import com.innovation.common.Result;
import com.innovation.entity.Project;
import com.innovation.entity.User;
import com.innovation.service.ProjectService;
import com.innovation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    // 修改待审核项目接口
    @GetMapping("/projects/pending")
    public Result<List<Map<String, Object>>> getPendingProjects() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();

            // 调用Service查询状态为0（待审核）的项目
            List<Map<String, Object>> pendingProjects = projectService.getTeacherProjectsByStatusWithDetails(teacherId, 0);
            return Result.success(pendingProjects);
        } catch (Exception e) {
            return Result.fail("获取待审核项目失败: " + e.getMessage());
        }
    }

    // 修改已审核项目接口
    @GetMapping("/projects/approved")
    public Result<List<Map<String, Object>>> getApprovedProjects() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();

            // 调用Service查询状态为1（通过）或2（驳回）的项目
            List<Map<String, Object>> approvedProjects = projectService.getTeacherProjectsByStatusWithDetails(teacherId, 1, 2);
            return Result.success(approvedProjects);
        } catch (Exception e) {
            return Result.fail("获取已审核项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师指导的项目列表
     */
    @GetMapping("/projects")
    public Result<List<Project>> getTeacherProjects() {
        try {
            // 获取当前登录教师ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();

            List<Project> projects = projectService.getTeacherProjects(teacherId);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/projects/{projectId}/review")
    public Result<String> reviewProject(
            @PathVariable Integer projectId,
            @RequestBody Map<String, Integer> reviewData) {
        try {
            Integer status = reviewData.get("status");
            if (status == null) {
                return Result.fail("请指定审核结果状态");
            }

            boolean success = projectService.auditProject(projectId, status);
            if (success) {
                return Result.success("项目审核成功");
            } else {
                return Result.fail("项目审核失败");
            }
        } catch (Exception e) {
            return Result.fail("审核处理失败: " + e.getMessage());
        }
    }

    /**
     * 查看项目详情（复用已有的服务方法）
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

    // 在TeacherController.java中添加接口
    @GetMapping("/projects/completed/unscored")
    public Result<List<Map<String, Object>>> getUnscoredCompletedProjects() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.selectByUsername(username);
            Integer teacherId = currentUser.getUserId();

            List<Map<String, Object>> unscoredProjects = projectService.getTeacherUnscoredCompletedProjects(teacherId);
            return Result.success(unscoredProjects);
        } catch (Exception e) {
            return Result.fail("获取未评分已结题项目失败: " + e.getMessage());
        }
    }
}