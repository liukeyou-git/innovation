package com.innovation.service;

import com.innovation.entity.Project;
import com.innovation.entity.User;
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
}