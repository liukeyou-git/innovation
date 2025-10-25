package com.innovation.mapper;

import com.innovation.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(String username);
    
    /**
     * 插入新用户
     */
    int insert(User user);

    /**
     * 查询所有教师
     */
    List<User> selectAllTeachers();

    /**
     * 根据学号查询学生
     */
    User selectStudentByStudentId(String studentId);

    /**
     * 根据ID查询用户
     */
    User selectById(Integer userId);
}