package com.innovation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer userId;
    private String username;
    private String password;
    private Integer role; // 0-admin, 1-teacher, 2-student
    private String realName;
    private String studentId;
    private String className; // 避免与关键字冲突
    private String teacherId;
    private String department;
    private String email;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status; // 0-禁用 1-启用
}