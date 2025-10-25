package com.innovation.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectApplyDTO {
    private String projectName; // 项目名称
    private String description; // 项目描述
    private String teacherName; // 指导教师
    private LocalDate startTime; // 项目开始时间
    private LocalDate endTime; // 项目结束时间
    private List<Integer> memberUserIds; // 成员用户ID列表（不含负责人）
    private String contribution; // 负责人贡献描述
}