package com.innovation.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class Project {
    private Integer projectId; // 项目唯一标识
    private String projectName; // 项目名称
    private String description; // 项目描述
    private Integer teacherId; // 指导教师ID
    private LocalDateTime applyTime; // 申报时间
    private LocalDate startTime; // 开始时间
    private LocalDate endTime; // 结束时间
    private Integer status; // 状态（0-申报中/1-已立项/2-进行中/3-已结题/4-已终止）
    private LocalDateTime createTime; // 创建时间
}