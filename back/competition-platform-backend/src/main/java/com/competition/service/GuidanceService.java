package com.competition.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.entity.GuidanceRelation;
import com.competition.entity.Notification;
import com.competition.mapper.GuidanceRelationMapper;
import com.competition.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuidanceService extends ServiceImpl<GuidanceRelationMapper, GuidanceRelation> {

    @Autowired
    private NotificationMapper notificationMapper;

    public void saveNotification(Notification notification) {
        notificationMapper.insert(notification);
    }
}