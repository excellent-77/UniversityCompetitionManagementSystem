package com.competition.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.competition.entity.Notification;
import com.competition.service.NotificationService;
import com.competition.utils.JwtUtil;
import com.competition.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notification")
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户通知列表
     */
    @GetMapping("/list")
    public ResultVO<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("Authorization") String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 分页查询
        int offset = (page - 1) * size;
        List<Notification> notifications = notificationService.getBaseMapper()
                .findByUserIdPage(userId, offset, size);
        long total = notificationService.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId));
        long unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", notifications);
        result.put("total", total);
        result.put("unreadCount", unreadCount);

        return ResultVO.success(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ResultVO<Map<String, Long>> getUnreadCount(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);

        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);

        return ResultVO.success(result);
    }

    /**
     * 标记通知已读
     */
    @PutMapping("/read/{id}")
    public ResultVO<Void> markAsRead(@PathVariable Long id,
                                     @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);

        boolean success = notificationService.markAsRead(id, userId);
        if (success) {
            return ResultVO.success("标记成功", null);
        }
        return ResultVO.error("标记失败");
    }

    /**
     * 标记所有通知已读
     */
    @PutMapping("/read-all")
    public ResultVO<Void> markAllAsRead(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);

        int count = notificationService.markAllAsRead(userId);
        return ResultVO.success("已标记 " + count + " 条通知为已读", null);
    }

    /**
     * 删除已读通知
     */
    @DeleteMapping("/clear-read")
    public ResultVO<Void> clearReadNotifications(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);

        int count = notificationService.deleteReadNotifications(userId);
        return ResultVO.success("已删除 " + count + " 条已读通知", null);
    }
}