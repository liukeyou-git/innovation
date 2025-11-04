package com.innovation.service.impl;

import com.innovation.entity.Achievement;
import com.innovation.entity.Project;
import com.innovation.entity.ProjectMember;
import com.innovation.entity.User;
import com.innovation.mapper.AchievementMapper;
import com.innovation.mapper.ProjectMapper;
import com.innovation.mapper.UserMapper;
import com.innovation.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private ProjectMapper projectMapper;

    // 新增注入UserMapper用于查询成员用户信息
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean saveAchievement(Achievement achievement) {
        // 原有代码不变...
        achievement.setEvaluationTime(LocalDateTime.now());
        Achievement existing = achievementMapper.selectByProjectId(achievement.getProjectId());
        if (existing != null) {
            achievement.setAchievementId(existing.getAchievementId());
            return achievementMapper.updateAchievement(achievement) > 0;
        } else {
            return achievementMapper.insertAchievement(achievement) > 0;
        }
    }

    @Override
    public Achievement getAchievementByProjectId(Integer projectId) {
        return achievementMapper.selectByProjectId(projectId);
    }

    @Override
    public List<Map<String, Object>> getTeacherAchievements(Integer teacherId) {
        List<Achievement> achievements = achievementMapper.selectByEvaluatorId(teacherId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Achievement achievement : achievements) {
            Map<String, Object> item = new HashMap<>();
            item.put("achievement", achievement);

            // 获取项目信息
            Project project = projectMapper.selectProjectById(achievement.getProjectId());
            item.put("project", project);

            // 新增：获取项目成员列表及用户详情
            if (project != null) {
                List<ProjectMember> members = projectMapper.selectMembersByProjectId(project.getProjectId());
                List<Map<String, Object>> memberUsers = new ArrayList<>();
                for (ProjectMember member : members) {
                    User user = userMapper.selectById(member.getUserId());
                    if (user != null) {
                        Map<String, Object> memberInfo = new HashMap<>();
                        memberInfo.put("userId", user.getUserId());
                        memberInfo.put("realName", user.getRealName());
                        memberInfo.put("studentId", user.getStudentId()); // 学生学号
                        memberInfo.put("roleInProject", member.getRoleInProject()); // 0-负责人/1-成员
                        memberInfo.put("contribution", member.getContribution()); // 贡献描述
                        memberUsers.add(memberInfo);
                    }
                }
                item.put("members", memberUsers); // 加入成员列表
            }

            result.add(item);
        }

        return result;
    }
}