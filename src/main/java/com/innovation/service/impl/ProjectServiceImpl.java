package com.innovation.service.impl;

import com.innovation.entity.Project;
import com.innovation.entity.ProjectMember;
import com.innovation.entity.User;
import com.innovation.mapper.ProjectMapper;
import com.innovation.mapper.UserMapper;
import com.innovation.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<User> getTeachersList() {
        return userMapper.selectAllTeachers();
    }

    @Override
    public User searchStudentByStudentId(String studentId) {
        return userMapper.selectStudentByStudentId(studentId);
    }

    @Transactional
    @Override
    public boolean createProject(Map<String, Object> projectData, Integer creatorId) {
        try {
            // 创建项目
            Project project = new Project();
            project.setProjectName((String) projectData.get("projectName"));
            project.setDescription((String) projectData.get("description"));
            project.setTeacherId((Integer) projectData.get("teacherId"));
            project.setApplyTime(LocalDateTime.now());

            // 处理日期
            if (projectData.get("startTime") != null) {
                project.setStartTime(LocalDate.parse((String) projectData.get("startTime")));
            }
            if (projectData.get("endTime") != null) {
                project.setEndTime(LocalDate.parse((String) projectData.get("endTime")));
            }

            project.setStatus(0); // 0表示申报中
            project.setCreateTime(LocalDateTime.now());

            // 插入项目并获取生成的projectId
            projectMapper.insertProject(project);
            Integer projectId = project.getProjectId();

            // 处理项目成员
            List<Map<String, Object>> members = (List<Map<String, Object>>) projectData.get("members");
            List<ProjectMember> projectMembers = new ArrayList<>();

            for (Map<String, Object> member : members) {
                ProjectMember projectMember = new ProjectMember();
                projectMember.setProjectId(projectId);
                projectMember.setUserId((Integer) member.get("userId"));
                projectMember.setRoleInProject(Integer.parseInt((String) member.get("roleInProject")));
                projectMember.setContribution((String) member.get("contribution"));
                projectMembers.add(projectMember);
            }

            // 批量插入项目成员
            projectMapper.batchInsertProjectMembers(projectMembers);

            return true;
        } catch (Exception e) {
            // 事务会自动回滚
            throw new RuntimeException("创建项目失败", e);
        }
    }

    @Override
    public List<Project> getStudentProjects(Integer studentId) {
        return projectMapper.selectProjectsByStudentId(studentId);
    }

    @Override
    public Map<String, Object> getProjectDetail(Integer projectId) {
        Map<String, Object> result = new HashMap<>();

        // 获取项目基本信息
        Project project = projectMapper.selectProjectById(projectId);
        if (project == null) {
            return null;
        }
        result.put("project", project);

        // 获取项目成员信息
        List<ProjectMember> members = projectMapper.selectMembersByProjectId(projectId);
        result.put("members", members);

        // 获取指导教师信息
        User teacher = userMapper.selectById(project.getTeacherId());
        result.put("teacher", teacher);

        // 获取成员的用户信息
        List<User> memberUsers = new ArrayList<>();
        for (ProjectMember member : members) {
            User user = userMapper.selectById(member.getUserId());
            memberUsers.add(user);
        }
        result.put("memberUsers", memberUsers);

        return result;
    }
}