package com.innovation.mapper;

import com.innovation.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ProjectMemberMapper {
    // 批量插入项目成员
    int batchInsert(List<ProjectMember> members);

    // 根据项目ID查询成员
    List<ProjectMember> selectByProjectId(Integer projectId);
}