package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.StudentDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentDetailMapper extends BaseMapper<StudentDetail> {

    /**
     * 根据学号查找学生
     */
    @Select("SELECT * FROM student_detail WHERE student_no = #{studentNo}")
    StudentDetail findByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 根据专业统计学生人数
     */
    @Select("SELECT major, COUNT(*) as count FROM student_detail GROUP BY major")
    List<Map<String, Object>> countByMajor();

    /**
     * 根据年级统计学生人数
     */
    @Select("SELECT grade, COUNT(*) as count FROM student_detail GROUP BY grade")
    List<Map<String, Object>> countByGrade();

    /**
     * 更新学生技能
     */
    @Update("UPDATE student_detail SET skills = #{skills}, update_time = NOW() WHERE user_id = #{userId}")
    int updateSkills(@Param("userId") Long userId, @Param("skills") String skills);

    /**
     * 根据技能标签搜索学生
     */
    @Select("SELECT sd.*, u.real_name, u.username " +
            "FROM student_detail sd " +
            "LEFT JOIN user u ON sd.user_id = u.id " +
            "WHERE sd.skills LIKE CONCAT('%', #{skill}, '%') AND u.status = 1")
    List<Map<String, Object>> findBySkill(@Param("skill") String skill);

    /**
     * 获取学生完整信息
     */
    @Select("SELECT sd.*, u.real_name, u.username, u.phone, u.email " +
            "FROM student_detail sd " +
            "LEFT JOIN user u ON sd.user_id = u.id " +
            "WHERE sd.user_id = #{userId}")
    Map<String, Object> getStudentFullInfo(@Param("userId") Long userId);

    /**
     * 获取所有学生简要信息（用于推荐）
     */
    @Select("SELECT sd.user_id, sd.major, sd.grade, sd.skills, u.real_name " +
            "FROM student_detail sd " +
            "LEFT JOIN user u ON sd.user_id = u.id " +
            "WHERE u.status = 1")
    List<Map<String, Object>> getAllStudentsBrief();
}