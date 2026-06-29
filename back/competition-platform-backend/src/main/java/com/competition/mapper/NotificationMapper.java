package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    Long countUnread(@Param("userId") Long userId);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Notification> findByUserIdPage(@Param("userId") Long userId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    @Update("DELETE FROM notification WHERE user_id = #{userId} AND is_read = 1")
    int deleteReadNotifications(@Param("userId") Long userId);
}