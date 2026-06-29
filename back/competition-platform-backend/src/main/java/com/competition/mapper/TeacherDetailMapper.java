package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.TeacherDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherDetailMapper extends BaseMapper<TeacherDetail> {

    /**
     * 根据工号查找教师
     */
    @Select("SELECT * FROM teacher_detail WHERE teacher_no = #{teacherNo}")
    TeacherDetail findByTeacherNo(@Param("teacherNo") String teacherNo);

    /**
     * 获取可指导的教师列表
     */
    @Select("SELECT td.*, u.real_name, u.username " +
            "FROM teacher_detail td " +
            "LEFT JOIN user u ON td.user_id = u.id " +
            "WHERE td.is_available = 1 AND td.current_teams < td.max_teams AND u.status = 1")
    List<Map<String, Object>> getAvailableTeachers();

    /**
     * 根据研究方向搜索教师
     */
    @Select("SELECT td.*, u.real_name, u.username " +
            "FROM teacher_detail td " +
            "LEFT JOIN user u ON td.user_id = u.id " +
            "WHERE td.research_direction LIKE CONCAT('%', #{direction}, '%') AND u.status = 1")
    List<Map<String, Object>> findByResearchDirection(@Param("direction") String direction);

    /**
     * 增加教师指导团队数
     */
    @Update("UPDATE teacher_detail SET current_teams = current_teams + 1 WHERE user_id = #{teacherId}")
    int incrementCurrentTeams(@Param("teacherId") Long teacherId);

    /**
     * 减少教师指导团队数
     */
    @Update("UPDATE teacher_detail SET current_teams = current_teams - 1 WHERE user_id = #{teacherId}")
    int decrementCurrentTeams(@Param("teacherId") Long teacherId);

    /**
     * 更新教师指导状态
     */
    @Update("UPDATE teacher_detail SET is_available = #{isAvailable}, max_teams = #{maxTeams} WHERE user_id = #{teacherId}")
    int updateTeacherStatus(@Param("teacherId") Long teacherId,
                            @Param("isAvailable") Integer isAvailable,
                            @Param("maxTeams") Integer maxTeams);

    /**
     * 根据学院统计教师人数
     */
    @Select("SELECT department, COUNT(*) as count FROM teacher_detail GROUP BY department")
    List<Map<String, Object>> countByDepartment();

    /**
     * 获取教师完整信息
     */
    @Select("SELECT td.*, u.real_name, u.username, u.phone, u.email " +
            "FROM teacher_detail td " +
            "LEFT JOIN user u ON td.user_id = u.id " +
            "WHERE td.user_id = #{userId}")
    Map<String, Object> getTeacherFullInfo(@Param("userId") Long userId);
}