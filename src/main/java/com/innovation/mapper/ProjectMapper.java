package com.innovation.mapper;

import com.innovation.entity.Project;
import com.innovation.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper {
    /**
     * 创建项目
     */
    int insertProject(Project project);

    /**
     * 批量添加项目成员
     */
    int batchInsertProjectMembers(List<ProjectMember> members);

    /**
     * 根据学生ID查询项目列表
     */
    List<Project> selectProjectsByStudentId(Integer studentId);

    /**
     * 根据项目ID查询项目详情
     */
    Project selectProjectById(Integer projectId);

    /**
     * 根据项目ID查询项目成员
     */
    List<ProjectMember> selectMembersByProjectId(Integer projectId);

    /**
     * 根据教师ID查询指导的项目列表
     */
    List<Project> selectProjectsByTeacherId(Integer teacherId);

    /**
     * 更新项目状态
     */
    int updateProjectStatus(Integer projectId, Integer status);

    /**
     * 根据教师ID和状态查询项目
     */
    List<Project> selectProjectsByTeacherIdAndStatus(
            @Param("teacherId") Integer teacherId,
            @Param("status") Integer... status
    );
}