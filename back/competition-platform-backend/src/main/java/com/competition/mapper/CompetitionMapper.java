package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

    /**
     * 获取进行中的竞赛数量
     */
    @Select("SELECT COUNT(*) FROM competition WHERE status = 2")
    Long countOngoing();

    /**
     * 获取报名中的竞赛数量
     */
    @Select("SELECT COUNT(*) FROM competition WHERE status = 1")
    Long countRegistering();

    /**
     * 获取已结束的竞赛数量
     */
    @Select("SELECT COUNT(*) FROM competition WHERE status = 3")
    Long countEnded();

    /**
     * 更新竞赛状态
     */
    @Update("UPDATE competition SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据类别统计竞赛数量
     */
    @Select("SELECT category, COUNT(*) as count FROM competition GROUP BY category")
    List<Map<String, Object>> countByCategory();

    /**
     * 获取热门竞赛（报名人数最多）
     */
    @Select("SELECT c.*, COUNT(cr.id) as register_count " +
            "FROM competition c " +
            "LEFT JOIN competition_registration cr ON c.id = cr.competition_id " +
            "WHERE c.status = 1 " +
            "GROUP BY c.id " +
            "ORDER BY register_count DESC " +
            "LIMIT #{limit}")
    List<Competition> getHotCompetitions(@Param("limit") int limit);
}