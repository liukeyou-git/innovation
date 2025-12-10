package com.innovation.service.impl;

import com.innovation.common.ImportResult;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public boolean batchSaveAchievements(List<Achievement> achievements) {
        if (achievements == null || achievements.isEmpty()) {
            return false;
        }

        List<Achievement> insertList = new ArrayList<>();
        List<Achievement> updateList = new ArrayList<>();

        // 区分需要插入和更新的成绩
        for (Achievement achievement : achievements) {
            Achievement existing = achievementMapper.selectByProjectId(achievement.getProjectId());
            if (existing != null) {
                achievement.setAchievementId(existing.getAchievementId());
                updateList.add(achievement);
            } else {
                insertList.add(achievement);
            }
        }

        int successCount = 0;

        // 批量插入新成绩
        if (!insertList.isEmpty()) {
            successCount += achievementMapper.batchInsertAchievements(insertList);
        }

        // 批量更新已有成绩
        if (!updateList.isEmpty()) {
            successCount += achievementMapper.batchUpdateAchievements(updateList);
        }

        // 验证所有成绩是否都被正确处理
        return successCount == achievements.size();
    }

    // 在AchievementServiceImpl.java中添加
    @Override
    @Transactional
    public ImportResult<Achievement> batchImportAchievements(List<Achievement> achievements, Integer teacherId) {
        ImportResult<Achievement> result = new ImportResult<>();
        LocalDateTime now = LocalDateTime.now();

        for (Achievement achievement : achievements) {
            Map<String, Object> tempData = achievement.getTempData();
            String projectName = (String) tempData.get("projectName");
            String studentIds = (String) tempData.get("studentIds");
            List<String> errors = new ArrayList<>();

            try {
                // 1. 验证项目是否存在
                Project project = projectMapper.selectByProjectName(projectName);
                if (project == null) {
                    errors.add("项目不存在");
                } else {
                    // 2. 验证项目是否已结题
                    if (project.getStatus() != Project.STATUS_COMPLETED) {
                        errors.add("项目未结题，不能评分");
                    }
                    // 3. 验证项目是否属于当前教师指导
                    if (!project.getTeacherId().equals(teacherId)) {
                        errors.add("不是您指导的项目，无法评分");
                    }
                    // 4. 验证学生是否为项目成员
                    List<ProjectMember> members = projectMapper.selectMembersByProjectId(project.getProjectId());
                    Set<Integer> memberUserIds = members.stream()
                            .map(ProjectMember::getUserId)
                            .collect(Collectors.toSet());

                    // 解析学生学号并验证
                    String[] studentIdArray = studentIds.split(",");
                    for (String sid : studentIdArray) {
                        sid = sid.trim();
                        User student = userMapper.selectStudentByStudentId(sid);
                        if (student == null) {
                            errors.add("学生学号不存在：" + sid);
                        } else if (!memberUserIds.contains(student.getUserId())) {
                            errors.add("学生不是项目成员：" + sid);
                        }
                    }

                    // 5. 设置项目ID和教师信息
                    achievement.setProjectId(project.getProjectId());
                }

                // 6. 设置公共字段
                achievement.setEvaluatorId(teacherId);
                achievement.setEvaluationTime(now);

                // 收集错误
                if (!errors.isEmpty()) {
                    result.getErrorMessages().add(
                            "项目《" + projectName + "》：" + String.join("；", errors)
                    );
                } else {
                    result.getValidData().add(achievement);
                }
            } catch (Exception e) {
                result.getErrorMessages().add(
                        "项目《" + projectName + "》处理失败：" + e.getMessage()
                );
            }
        }

        // 保存有效数据
        if (!result.getValidData().isEmpty()) {
            batchSaveAchievements(result.getValidData());
        }

        return result;
    }
}