package com.innovation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Achievement {
    private Integer achievementId; // 成绩ID
    private Integer projectId; // 项目ID
    private Integer score; // 分数(0-100)
    private String grade; // 等级(优秀/良好/中等/及格/不及格)
    private String teacherComment; // 教师评语
    private LocalDateTime evaluationTime; // 评定时间
    private Integer evaluatorId; // 评定教师ID
}