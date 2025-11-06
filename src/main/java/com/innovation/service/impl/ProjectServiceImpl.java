package com.innovation.service.impl;

import com.innovation.entity.Achievement;
import com.innovation.entity.Project;
import com.innovation.entity.ProjectMember;
import com.innovation.entity.User;
import com.innovation.mapper.ProjectMapper;
import com.innovation.mapper.UserMapper;
import com.innovation.service.AchievementService;
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

    @Autowired
    private AchievementService achievementService;

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
            User teacher = userMapper.selectById(project.getTeacherId());
            project.setTeacherName(teacher.getRealName());
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
        List<ProjectMember> membersList = projectMapper.selectMembersByProjectId(projectId);
        result.put("members", membersList);

        // 获取指导教师信息
        User teacher = userMapper.selectById(project.getTeacherId());
        result.put("teacher", teacher);

        // 获取成员的用户信息并转换格式
        List<Map<String, Object>> memberUsers = new ArrayList<>();
        for (ProjectMember member : membersList) {
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("userId", user.getUserId());
                memberInfo.put("realName", user.getRealName());
                memberInfo.put("studentId", user.getStudentId());
                memberInfo.put("roleInProject", member.getRoleInProject());
                memberInfo.put("contribution", member.getContribution());
                memberUsers.add(memberInfo);
            }
        }
        result.put("memberUsers", memberUsers);

        // 获取申报学生信息（项目创建者，即第一个成员或负责人）
        if (!membersList.isEmpty()) {
            ProjectMember creatorMember = membersList.stream()
                    .filter(m -> m.getRoleInProject() == 0) // 0表示负责人
                    .findFirst()
                    .orElse(membersList.get(0));

            User creator = userMapper.selectById(creatorMember.getUserId());
            if (creator != null) {
                result.put("studentName", creator.getRealName());
                result.put("studentId", creator.getStudentId());
            }
        }

        // 添加提交时间（申报时间）
        result.put("submitTime", project.getApplyTime());

        return result;
    }

    @Override
    public List<Project> getTeacherProjects(Integer teacherId) {
        return projectMapper.selectProjectsByTeacherId(teacherId);
    }

    @Transactional
    @Override
    public boolean auditProject(Integer projectId, Integer status) {
        // 验证状态值是否合法
        if (status < 1 || status > 4) {
            throw new RuntimeException("无效的项目状态");
        }
        return projectMapper.updateProjectStatus(projectId, status) > 0;
    }

    @Override
    public List<Map<String, Object>> getTeacherProjectsByStatusWithDetails(Integer teacherId, Integer... status) {
        List<Project> projects = projectMapper.selectProjectsByTeacherIdAndStatus(teacherId, status);
        List<Map<String, Object>> projectList = new ArrayList<>();

        for (Project project : projects) {
            Map<String, Object> projectInfo = new HashMap<>();
            projectInfo.put("id", project.getProjectId());
            projectInfo.put("projectName", project.getProjectName());
            projectInfo.put("status", project.getStatus());
            projectInfo.put("description", project.getDescription());
            projectInfo.put("submitTime", project.getApplyTime());
            projectInfo.put("completeTime", project.getCompleteTime());

            // 获取申报学生信息
            List<ProjectMember> members = projectMapper.selectMembersByProjectId(project.getProjectId());
            if (!members.isEmpty()) {
                ProjectMember creatorMember = members.stream()
                        .filter(m -> m.getRoleInProject() == 0)
                        .findFirst()
                        .orElse(members.get(0));

                User creator = userMapper.selectById(creatorMember.getUserId());
                if (creator != null) {
                    projectInfo.put("studentName", creator.getRealName());
                    projectInfo.put("studentId", creator.getStudentId());
                }
            }

            projectList.add(projectInfo);
        }

        return projectList;
    }

    @Transactional
    @Override
    public boolean completeProject(Integer projectId, LocalDateTime completeTime) {
        // 验证项目是否存在
        Project project = projectMapper.selectProjectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 验证项目状态是否为"已通过"（只有已通过的项目才能结题）
        if (project.getStatus() != Project.STATUS_APPROVED) {
            throw new RuntimeException("只有已通过的项目才能进行结题操作");
        }

        // 执行更新操作，设置状态为已结题并记录时间
        return projectMapper.updateProjectStatusWithTime(
                projectId,
                Project.STATUS_COMPLETED,
                completeTime
        ) > 0;
    }

    @Override
    public List<Project> getStudentCompletedProjects(Integer studentId) {
        return projectMapper.selectProjectsByStudentIdAndStatus(studentId, Project.STATUS_COMPLETED);
    }

    @Override
    public List<Map<String, Object>> getTeacherCompletedProjectsWithDetails(Integer teacherId) {
        return getTeacherProjectsByStatusWithDetails(teacherId, Project.STATUS_COMPLETED);
    }

    // 在ProjectServiceImpl.java中实现方法
    @Override
    public List<Map<String, Object>> getTeacherUnscoredCompletedProjects(Integer teacherId) {
        // 获取教师所有已结题项目
        List<Map<String, Object>> completedProjects = getTeacherProjectsByStatusWithDetails(teacherId, Project.STATUS_COMPLETED);

        // 过滤出未评分的项目
        List<Map<String, Object>> unscoredProjects = new ArrayList<>();
        for (Map<String, Object> project : completedProjects) {
            Integer projectId = (Integer) project.get("id");
            Achievement achievement = achievementService.getAchievementByProjectId(projectId);

            // 如果没有成绩记录，即为未评分项目
            if (achievement == null) {
                unscoredProjects.add(project);
            }
        }
        return unscoredProjects;
    }
}