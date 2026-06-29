package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 获取竞赛的所有团队
     */
    @Select("SELECT * FROM team WHERE competition_id = #{competitionId} ORDER BY create_time DESC")
    List<Team> findByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 获取用户创建的团队
     */
    @Select("SELECT * FROM team WHERE leader_id = #{leaderId} ORDER BY create_time DESC")
    List<Team> findByLeaderId(@Param("leaderId") Long leaderId);

    /**
     * 增加团队成员数
     */
    @Update("UPDATE team SET current_members = current_members + 1 WHERE id = #{teamId}")
    int incrementMemberCount(@Param("teamId") Long teamId);

    /**
     * 减少团队成员数
     */
    @Update("UPDATE team SET current_members = current_members - 1 WHERE id = #{teamId}")
    int decrementMemberCount(@Param("teamId") Long teamId);

    /**
     * 更新团队状态
     */
    @Update("UPDATE team SET status = #{status} WHERE id = #{teamId}")
    int updateStatus(@Param("teamId") Long teamId, @Param("status") Integer status);

    /**
     * 获取招募中的团队
     */
    @Select("SELECT t.*, c.title as competition_title " +
            "FROM team t " +
            "LEFT JOIN competition c ON t.competition_id = c.id " +
            "WHERE t.status = 0 AND t.current_members < t.max_members")
    List<Map<String, Object>> getRecruitingTeams();

    /**
     * 获取热门团队（报名人数最多）
     */
    @Select("SELECT t.*, COUNT(cr.id) as register_count " +
            "FROM team t " +
            "LEFT JOIN competition_registration cr ON t.id = cr.team_id " +
            "GROUP BY t.id " +
            "ORDER BY register_count DESC " +
            "LIMIT #{limit}")
    List<Team> getHotTeams(@Param("limit") int limit);

    /**
     * 根据竞赛统计团队数量
     */
    @Select("SELECT competition_id, COUNT(*) as team_count FROM team GROUP BY competition_id")
    List<Map<String, Object>> countByCompetition();
}