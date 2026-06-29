package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    /**
     * 获取团队成员列表
     */
    @Select("SELECT tm.*, u.real_name, u.username, u.avatar " +
            "FROM team_member tm " +
            "LEFT JOIN user u ON tm.user_id = u.id " +
            "WHERE tm.team_id = #{teamId} AND tm.status = 1")
    List<Map<String, Object>> getTeamMembers(@Param("teamId") Long teamId);

    /**
     * 获取用户加入的团队
     */
    @Select("SELECT tm.*, t.team_name, t.competition_id, c.title as competition_title " +
            "FROM team_member tm " +
            "LEFT JOIN team t ON tm.team_id = t.id " +
            "LEFT JOIN competition c ON t.competition_id = c.id " +
            "WHERE tm.user_id = #{userId} AND tm.status = 1")
    List<Map<String, Object>> getUserTeams(@Param("userId") Long userId);

    /**
     * 获取团队的待处理申请
     */
    @Select("SELECT tm.*, u.real_name, u.username " +
            "FROM team_member tm " +
            "LEFT JOIN user u ON tm.user_id = u.id " +
            "WHERE tm.team_id = #{teamId} AND tm.status = 2")
    List<Map<String, Object>> getPendingApplications(@Param("teamId") Long teamId);

    /**
     * 检查用户是否在团队中
     */
    @Select("SELECT COUNT(*) FROM team_member WHERE team_id = #{teamId} AND user_id = #{userId} AND status = 1")
    int checkUserInTeam(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 检查用户是否已申请
     */
    @Select("SELECT COUNT(*) FROM team_member WHERE team_id = #{teamId} AND user_id = #{userId} AND status = 2")
    int checkUserApplied(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 处理加入申请
     */
    @Update("UPDATE team_member SET status = #{status} WHERE id = #{id}")
    int handleApplication(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 获取队长信息
     */
    @Select("SELECT tm.*, u.real_name, u.username " +
            "FROM team_member tm " +
            "LEFT JOIN user u ON tm.user_id = u.id " +
            "WHERE tm.team_id = #{teamId} AND tm.role = 1")
    Map<String, Object> getTeamLeader(@Param("teamId") Long teamId);

    /**
     * 获取团队成员数量
     */
    @Select("SELECT COUNT(*) FROM team_member WHERE team_id = #{teamId} AND status = 1")
    int getMemberCount(@Param("teamId") Long teamId);
}