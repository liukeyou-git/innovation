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

    // 在AchievementServiceImpl中完善getTeacherAchievements方法（如果需要）
    @Override
    public List<Map<String, Object>> getTeacherAchievements(Integer teacherId) {
        List<Achievement> achievements = achievementMapper.selectByEvaluatorId(teacherId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Achievement achievement : achievements) {
            Map<String, Object> item = new HashMap<>();
            // 1. 封装成绩信息
            item.put("achievement", achievement);

            // 2. 封装项目信息（转换为Map，仅保留需要的字段）
            Project project = projectMapper.selectProjectById(achievement.getProjectId());
            if (project != null) {
                Map<String, Object> projectMap = new HashMap<>();
                projectMap.put("projectId", project.getProjectId());
                projectMap.put("projectName", project.getProjectName());
                // 按需添加其他项目字段（如结题时间等）
                item.put("project", projectMap); // 存入Map而非实体类
            }

            // 3. 封装成员信息（转换为Map）
            List<ProjectMember> members = projectMapper.selectMembersByProjectId(achievement.getProjectId());
            List<Map<String, Object>> memberMaps = new ArrayList<>();
            for (ProjectMember member : members) {
                User user = userMapper.selectById(member.getUserId());
                if (user != null) {
                    Map<String, Object> memberMap = new HashMap<>();
                    memberMap.put("realName", user.getRealName());
                    memberMap.put("studentId", user.getStudentId());
                    memberMap.put("roleInProject", member.getRoleInProject());
                    memberMaps.add(memberMap);
                }
            }
            item.put("members", memberMaps);

            result.add(item);
        }

        return result;
    }
}