package com.innovation.mapper;

import com.innovation.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AchievementMapper {
    /**
     * 新增项目成绩
     */
    int insertAchievement(Achievement achievement);
    
    /**
     * 更新项目成绩
     */
    int updateAchievement(Achievement achievement);
    
    /**
     * 根据项目ID查询成绩
     */
    Achievement selectByProjectId(Integer projectId);
    
    /**
     * 根据教师ID查询其评定的成绩列表
     */
    List<Achievement> selectByEvaluatorId(Integer evaluatorId);
}