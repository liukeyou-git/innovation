package com.innovation.service;

import com.innovation.entity.Achievement;
import java.util.List;
import java.util.Map;

public interface AchievementService {
    /**
     * 保存项目成绩(新增或更新)
     */
    boolean saveAchievement(Achievement achievement);
    
    /**
     * 根据项目ID查询成绩
     */
    Achievement getAchievementByProjectId(Integer projectId);
    
    /**
     * 获取教师评定的成绩列表(包含项目信息)
     */
    List<Map<String, Object>> getTeacherAchievements(Integer teacherId);
}