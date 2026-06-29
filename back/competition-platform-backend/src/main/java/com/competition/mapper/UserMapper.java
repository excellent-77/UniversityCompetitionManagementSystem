package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据角色获取用户列表
     */
    @Select("SELECT * FROM user WHERE role = #{role} AND status = 1 ORDER BY create_time DESC")
    List<User> findByRole(@Param("role") Integer role);

    /**
     * 获取用户总数
     */
    @Select("SELECT COUNT(*) FROM user")
    Long getUserCount();

    /**
     * 获取今日注册用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE()")
    Long getTodayRegisterCount();

    /**
     * 获取本周注册用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE YEARWEEK(create_time) = YEARWEEK(NOW())")
    Long getWeekRegisterCount();

    /**
     * 获取本月注册用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE MONTH(create_time) = MONTH(NOW()) AND YEAR(create_time) = YEAR(NOW())")
    Long getMonthRegisterCount();

    /**
     * 根据角色统计用户数量
     */
    @Select("SELECT role, COUNT(*) as count FROM user GROUP BY role")
    List<Map<String, Object>> countByRole();

    /**
     * 批量更新用户状态
     */
    @Update({
            "<script>",
            "UPDATE user SET status = #{status}, update_time = NOW() WHERE id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 搜索用户
     */
    @Select({
            "<script>",
            "SELECT * FROM user WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (username LIKE CONCAT('%', #{keyword}, '%') OR real_name LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='role != null'>",
            "AND role = #{role}",
            "</if>",
            "ORDER BY create_time DESC",
            "</script>"
    })
    List<User> searchUsers(@Param("keyword") String keyword, @Param("role") Integer role);

    /**
     * 获取用户详细信息（包含角色详情）
     */
    @Select({
            "SELECT u.*, ",
            "CASE WHEN u.role = 2 THEN sd.major END as major,",
            "CASE WHEN u.role = 2 THEN sd.grade END as grade,",
            "CASE WHEN u.role = 2 THEN sd.class_name END as class_name,",
            "CASE WHEN u.role = 1 THEN td.department END as department,",
            "CASE WHEN u.role = 1 THEN td.title END as title ",
            "FROM user u ",
            "LEFT JOIN student_detail sd ON u.id = sd.user_id ",
            "LEFT JOIN teacher_detail td ON u.id = td.user_id ",
            "WHERE u.id = #{userId}"
    })
    Map<String, Object> getUserDetail(@Param("userId") Long userId);

    /**
     * 获取活跃用户（最近登录）
     */
    @Select("SELECT * FROM user WHERE status = 1 ORDER BY update_time DESC LIMIT #{limit}")
    List<User> getActiveUsers(@Param("limit") int limit);
}