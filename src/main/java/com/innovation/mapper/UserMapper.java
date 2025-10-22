package com.innovation.mapper;

import com.innovation.entity.User;
import org.apache.ibatis.annotations.Mapper;

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
}