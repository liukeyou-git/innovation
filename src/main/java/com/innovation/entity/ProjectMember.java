package com.innovation.entity;

import lombok.Data;

@Data
public class ProjectMember {
    private Integer id; // 主键ID
    private Integer projectId; // 项目ID
    private Integer userId; // 学生用户ID
    private Integer roleInProject; // 角色（0-负责人/1-成员）
    private String contribution; // 贡献描述
}