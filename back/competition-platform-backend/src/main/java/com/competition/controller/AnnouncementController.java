package com.competition.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.competition.entity.Announcement;
import com.competition.entity.User;
import com.competition.service.AnnouncementService;
import com.competition.service.UserService;
import com.competition.utils.JwtUtil;
import com.competition.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/announcement")
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取公告列表（公开接口）
     * 普通用户只能看到已发布的公告，管理员可以看到全部
     */
    @GetMapping("/list")
    public ResultVO<Page<Announcement>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Page<Announcement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();

        // 判断是否为管理员
        boolean isAdmin = false;
        if (StrUtil.isNotBlank(token)) {
            String cleanToken = token;
            if (cleanToken.startsWith("Bearer ")) {
                cleanToken = cleanToken.substring(7);
            }
            try {
                Integer role = jwtUtil.getRoleFromToken(cleanToken);
                isAdmin = role != null && role == 0;
            } catch (Exception e) {
                // token无效，按普通用户处理
            }
        }

        // 关键：非管理员只能看到已发布的公告 (status = 1)
        if (!isAdmin) {
            wrapper.eq(Announcement::getStatus, 1);
        } else if (status != null) {
            // 管理员可以根据状态筛选
            wrapper.eq(Announcement::getStatus, status);
        }

        // 关键词搜索
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Announcement::getTitle, keyword);
        }

        // 排序：置顶优先，然后按发布时间倒序
        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishTime);

        Page<Announcement> result = announcementService.page(pageParam, wrapper);

        // 填充发布人姓名
        for (Announcement a : result.getRecords()) {
            if (a.getPublishUserId() != null) {
                User user = userService.getById(a.getPublishUserId());
                if (user != null) {
                    a.setPublishUserName(user.getRealName());
                } else {
                    a.setPublishUserName("系统");
                }
            }
        }

        return ResultVO.success(result);
    }

    /**
     * 获取置顶公告（只返回已发布的）
     */
    @GetMapping("/top")
    public ResultVO<List<Announcement>> getTopAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .eq(Announcement::getIsTop, 1)
                .orderByDesc(Announcement::getPublishTime);

        List<Announcement> list = announcementService.list(wrapper);
        return ResultVO.success(list);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public ResultVO<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return ResultVO.error("公告不存在");
        }

        // 普通用户只能查看已发布的公告
        String token = null;
        boolean isAdmin = false;
        // 尝试从请求头获取token判断是否为管理员
        // 这里简化处理：如果公告已下架且不是管理员，返回错误

        if (announcement.getPublishUserId() != null) {
            User user = userService.getById(announcement.getPublishUserId());
            if (user != null) {
                announcement.setPublishUserName(user.getRealName());
            }
        }

        return ResultVO.success(announcement);
    }

    /**
     * 创建公告（管理员）
     */
    @PostMapping("/create")
    public ResultVO<Void> createAnnouncement(@RequestBody Announcement announcement,
                                             @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        announcement.setPublishUserId(userId);
        announcement.setPublishTime(LocalDateTime.now());

        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }

        boolean saved = announcementService.save(announcement);
        if (saved) {
            log.info("管理员 {} 创建公告: {}", userId, announcement.getTitle());
            return ResultVO.success("创建成功", null);
        }
        return ResultVO.error("创建失败");
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/update")
    public ResultVO<Void> updateAnnouncement(@RequestBody Announcement announcement,
                                             @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        Announcement existing = announcementService.getById(announcement.getId());
        if (existing == null) {
            return ResultVO.error("公告不存在");
        }

        existing.setTitle(announcement.getTitle());
        existing.setContent(announcement.getContent());
        existing.setIsTop(announcement.getIsTop());
        existing.setStatus(announcement.getStatus());
        existing.setUpdateTime(LocalDateTime.now());

        boolean updated = announcementService.updateById(existing);
        if (updated) {
            log.info("管理员更新公告: {}", announcement.getId());
            return ResultVO.success("更新成功", null);
        }
        return ResultVO.error("更新失败");
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO<Void> deleteAnnouncement(@PathVariable Long id,
                                             @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        boolean removed = announcementService.removeById(id);
        if (removed) {
            log.info("管理员删除公告: {}", id);
            return ResultVO.success("删除成功", null);
        }
        return ResultVO.error("删除失败");
    }

    /**
     * 更新公告状态（发布/下架）
     */
    @PutMapping("/update-status")
    public ResultVO<Void> updateAnnouncementStatus(@RequestBody Map<String, Object> params,
                                                   @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        Announcement announcement = announcementService.getById(id);
        if (announcement != null) {
            announcement.setStatus(status);
            announcement.setUpdateTime(LocalDateTime.now());
            announcementService.updateById(announcement);
            log.info("管理员更新公告状态: {}, 状态: {}", id, status == 1 ? "发布" : "下架");
            return ResultVO.success(status == 1 ? "已发布" : "已下架", null);
        }
        return ResultVO.error("公告不存在");
    }

    /**
     * 置顶/取消置顶公告
     */
    @PutMapping("/toggle-top/{id}")
    public ResultVO<Void> toggleTop(@PathVariable Long id,
                                    @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        Announcement announcement = announcementService.getById(id);
        if (announcement != null) {
            announcement.setIsTop(announcement.getIsTop() == 1 ? 0 : 1);
            announcement.setUpdateTime(LocalDateTime.now());
            announcementService.updateById(announcement);
            log.info("管理员置顶公告: {}, 状态: {}", id, announcement.getIsTop() == 1 ? "置顶" : "取消置顶");
            return ResultVO.success(announcement.getIsTop() == 1 ? "已置顶" : "已取消置顶", null);
        }
        return ResultVO.error("公告不存在");
    }
}