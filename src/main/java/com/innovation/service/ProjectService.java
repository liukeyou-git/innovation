package com.innovation.service;

import com.innovation.entity.Project;
import com.innovation.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProjectService {
    /**
     * 获取所有教师列表
     */
    List<User> getTeachersList();

    /**
     * 根据学号搜索学生
     */
    User searchStudentByStudentId(String studentId);

    /**
     * 创建项目申报
     */
    boolean createProject(Map<String, Object> projectData, Integer creatorId);

    /**
     * 获取学生已申报的项目列表
     */
    List<Project> getStudentProjects(Integer studentId);

    /**
     * 获取项目详情
     */
    Map<String, Object> getProjectDetail(Integer projectId);

    /**
     * 获取教师指导的项目列表
     */
    List<Project> getTeacherProjects(Integer teacherId);

    /**
     * 审核项目（更新状态）
     */
    boolean auditProject(Integer projectId, Integer status);

    /**
     * 根据教师ID和状态查询项目详情列表（包含学生信息）
     */
    List<Map<String, Object>> getTeacherProjectsByStatusWithDetails(Integer teacherId, Integer... status);

    /**
     * 项目结题操作
     */
    boolean completeProject(Integer projectId, LocalDateTime completeTime);

    /**
     * 获取学生参与的已结题项目
     */
    List<Project> getStudentCompletedProjects(Integer studentId);

    /**
     * 获取教师指导的已结题项目详情列表
     */
    List<Map<String, Object>> getTeacherCompletedProjectsWithDetails(Integer teacherId);
}