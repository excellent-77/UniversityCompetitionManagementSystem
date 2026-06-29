package com.competition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.entity.Announcement;
import com.competition.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {

    /**
     * 获取置顶公告列表
     */
    public List<Announcement> getTopAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .eq(Announcement::getIsTop, 1)
                .orderByDesc(Announcement::getPublishTime);
        return this.list(wrapper);
    }

    /**
     * 获取发布的公告列表
     */
    public List<Announcement> getPublishedAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishTime);
        return this.list(wrapper);
    }

    /**
     * 发布公告
     */
    public boolean publishAnnouncement(Announcement announcement) {
        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        return this.save(announcement);
    }

    /**
     * 下架公告
     */
    public boolean unpublishAnnouncement(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement != null) {
            announcement.setStatus(0);
            return this.updateById(announcement);
        }
        return false;
    }

    /**
     * 置顶公告
     */
    public boolean topAnnouncement(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement != null) {
            announcement.setIsTop(1);
            return this.updateById(announcement);
        }
        return false;
    }

    /**
     * 取消置顶
     */
    public boolean untopAnnouncement(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement != null) {
            announcement.setIsTop(0);
            return this.updateById(announcement);
        }
        return false;
    }
}