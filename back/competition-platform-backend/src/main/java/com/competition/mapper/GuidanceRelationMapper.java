package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.GuidanceRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface GuidanceRelationMapper extends BaseMapper<GuidanceRelation> {

    /**
     * 获取教师指导的团队数量
     */
    @Select("SELECT COUNT(*) FROM guidance_relation WHERE teacher_id = #{teacherId} AND status = 1")
    Long countByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 获取团队的指导申请
     */
    @Select("SELECT * FROM guidance_relation WHERE team_id = #{teamId} ORDER BY create_time DESC")
    List<GuidanceRelation> findByTeamId(@Param("teamId") Long teamId);

    /**
     * 获取待处理的指导申请
     */
    @Select("SELECT * FROM guidance_relation WHERE teacher_id = #{teacherId} AND status = 0 ORDER BY create_time DESC")
    List<GuidanceRelation> findPendingByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 接受指导申请
     */
    @Update("UPDATE guidance_relation SET status = 1, teacher_comment = #{comment}, update_time = NOW() WHERE id = #{id}")
    int acceptGuidance(@Param("id") Long id, @Param("comment") String comment);

    /**
     * 拒绝指导申请
     */
    @Update("UPDATE guidance_relation SET status = 2, teacher_comment = #{comment}, update_time = NOW() WHERE id = #{id}")
    int rejectGuidance(@Param("id") Long id, @Param("comment") String comment);

    /**
     * 获取教师指导的团队详情
     */
    @Select("SELECT gr.*, t.team_name, t.competition_id, c.title as competition_title, u.real_name as leader_name " +
            "FROM guidance_relation gr " +
            "LEFT JOIN team t ON gr.team_id = t.id " +
            "LEFT JOIN competition c ON t.competition_id = c.id " +
            "LEFT JOIN user u ON t.leader_id = u.id " +
            "WHERE gr.teacher_id = #{teacherId} AND gr.status = 1")
    List<Map<String, Object>> getTeacherGuidanceTeams(@Param("teacherId") Long teacherId);
}