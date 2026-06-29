package com.competition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.entity.Notification;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    // ==================== 基础通知发送方法 ====================

    /**
     * 发送系统通知
     */
    @Transactional
    public void sendSystemNotification(Long userId, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(0);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    /**
     * 发送组队邀请通知
     */
    @Transactional
    public void sendTeamInviteNotification(Long userId, String teamName, Long teamId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("团队加入申请");
        notification.setContent("有用户申请加入您的团队【" + teamName + "】，请及时处理");
        notification.setType(1);
        notification.setRelatedId(teamId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    /**
     * 发送团队申请结果通知
     */
    @Transactional
    public void sendTeamApplyResultNotification(Long userId, String teamName, boolean accepted, String reason, Long teamId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        if (accepted) {
            notification.setTitle("加入团队申请已通过");
            notification.setContent("恭喜！您已成功加入团队【" + teamName + "】");
        } else {
            notification.setTitle("加入团队申请被拒绝");
            notification.setContent("您的加入团队【" + teamName + "】申请被拒绝" +
                    (reason != null && !reason.isEmpty() ? "，原因：" + reason : ""));
        }
        notification.setType(1);
        notification.setRelatedId(teamId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    /**
     * 发送团队解散通知
     */
    @Transactional
    public void sendTeamDisbandNotification(Long userId, String teamName, Long teamId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("团队解散通知");
        notification.setContent("您所在的团队【" + teamName + "】已被队长解散");
        notification.setType(1);
        notification.setRelatedId(teamId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    // ==================== 指导相关通知 ====================

    /**
     * 发送指导申请通知
     */
    @Transactional
    public void sendGuidanceApplyNotification(Long teacherId, String teamName, Long teamId) {
        Notification notification = new Notification();
        notification.setUserId(teacherId);
        notification.setTitle("指导申请通知");
        notification.setContent("团队【" + teamName + "】申请您的指导，请及时处理");
        notification.setType(2);
        notification.setRelatedId(teamId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    /**
     * 发送指导申请结果通知
     */
    @Transactional
    public void sendGuidanceResultNotification(Long userId, String teacherName, boolean accepted, String reason, Long teamId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        if (accepted) {
            notification.setTitle("指导申请已通过");
            notification.setContent("教师【" + teacherName + "】已接受您的指导申请");
        } else {
            notification.setTitle("指导申请被拒绝");
            notification.setContent("教师【" + teacherName + "】拒绝了您的指导申请" +
                    (reason != null && !reason.isEmpty() ? "，原因：" + reason : ""));
        }
        notification.setType(2);
        notification.setRelatedId(teamId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    // ==================== 竞赛相关通知 ====================

    /**
     * 发送竞赛通知
     */
    @Transactional
    public void sendCompetitionNotification(Long userId, String competitionTitle, String message, Long competitionId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("竞赛通知");
        notification.setContent("【" + competitionTitle + "】" + message);
        notification.setType(3);
        notification.setRelatedId(competitionId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    /**
     * 批量发送竞赛通知给所有参赛团队
     */
    @Transactional
    public void sendCompetitionNotificationToTeams(Long competitionId, String competitionTitle, String message) {
        List<Team> teams = teamService.getTeamsByCompetition(competitionId);
        List<Notification> notifications = new ArrayList<>();

        for (Team team : teams) {
            // 给队长发通知
            Notification leaderNotification = new Notification();
            leaderNotification.setUserId(team.getLeaderId());
            leaderNotification.setTitle("竞赛通知");
            leaderNotification.setContent("【" + competitionTitle + "】" + message);
            leaderNotification.setType(3);
            leaderNotification.setRelatedId(competitionId);
            leaderNotification.setIsRead(0);
            leaderNotification.setCreateTime(LocalDateTime.now());
            notifications.add(leaderNotification);

            // 给所有成员发通知（排除队长）
            List<Long> memberIds = teamService.getTeamMemberIds(team.getId());
            for (Long memberId : memberIds) {
                if (!memberId.equals(team.getLeaderId())) {
                    Notification memberNotification = new Notification();
                    memberNotification.setUserId(memberId);
                    memberNotification.setTitle("竞赛通知");
                    memberNotification.setContent("【" + competitionTitle + "】" + message);
                    memberNotification.setType(3);
                    memberNotification.setRelatedId(competitionId);
                    memberNotification.setIsRead(0);
                    memberNotification.setCreateTime(LocalDateTime.now());
                    notifications.add(memberNotification);
                }
            }
        }

        if (!notifications.isEmpty()) {
            this.saveBatch(notifications);
        }
    }

    /**
     * 发送竞赛通知给所有学生（适用于公开竞赛）
     */
    @Transactional
    public void sendCompetitionNotificationToAllStudents(String competitionTitle, String message, Long competitionId) {
        List<User> students = userService.getStudents();
        List<Notification> notifications = new ArrayList<>();

        for (User student : students) {
            Notification notification = new Notification();
            notification.setUserId(student.getId());
            notification.setTitle("竞赛通知");
            notification.setContent("【" + competitionTitle + "】" + message);
            notification.setType(3);
            notification.setRelatedId(competitionId);
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notifications.add(notification);
        }

        if (!notifications.isEmpty()) {
            this.saveBatch(notifications);
        }
    }

    // ==================== 通知查询方法 ====================

    /**
     * 获取用户未读通知数量
     */
    public Long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return baseMapper.countUnread(userId);
    }

    /**
     * 获取用户所有通知
     */
    public List<Notification> getUserNotifications(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return baseMapper.findByUserId(userId);
    }

    /**
     * 分页获取用户通知
     */
    public List<Notification> getUserNotificationsPage(Long userId, int page, int size) {
        if (userId == null) {
            return new ArrayList<>();
        }
        int offset = (page - 1) * size;
        return baseMapper.findByUserIdPage(userId, offset, size);
    }

    /**
     * 获取用户通知总数
     */
    public long getUserNotificationCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        return this.count(wrapper);
    }

    // ==================== 通知管理方法 ====================

    /**
     * 标记通知已读
     */
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        if (notificationId == null || userId == null) {
            return false;
        }
        Notification notification = this.getById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            return this.updateById(notification);
        }
        return false;
    }

    /**
     * 标记所有通知已读
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        if (userId == null) {
            return 0;
        }
        return baseMapper.markAllAsRead(userId);
    }

    /**
     * 删除已读通知
     */
    @Transactional
    public int deleteReadNotifications(Long userId) {
        if (userId == null) {
            return 0;
        }
        return baseMapper.deleteReadNotifications(userId);
    }

    /**
     * 删除所有通知（管理员）
     */
    @Transactional
    public int deleteAllNotifications(Long userId) {
        if (userId == null) {
            return 0;
        }
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        return baseMapper.delete(wrapper);
    }

    /**
     * 批量删除通知
     */
    @Transactional
    public int batchDeleteNotifications(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty() || userId == null) {
            return 0;
        }
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .in(Notification::getId, notificationIds);
        return baseMapper.delete(wrapper);
    }


}