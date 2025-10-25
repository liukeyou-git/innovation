package com.innovation.service.impl;

import com.innovation.entity.User;
import com.innovation.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    // 在loadUserByUsername方法中设置角色权限
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

        // 根据用户角色设置权限（ROLE_前缀是Spring Security要求）
        List<String> roles = new ArrayList<>();
        if (user.getRole() == 0) {
            roles.add("ROLE_ADMIN");
        } else if (user.getRole() == 1) {
            roles.add("ROLE_TEACHER");
        } else if (user.getRole() == 2) {
            roles.add("ROLE_STUDENT");
        }

        // 将String类型的roles转换为GrantedAuthority集合
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new) // 每个角色包装为SimpleGrantedAuthority
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities) // 传入GrantedAuthority集合
                .build();
    }
}