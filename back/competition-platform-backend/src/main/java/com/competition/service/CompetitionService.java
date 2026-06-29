package com.competition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.entity.Competition;
import com.competition.mapper.CompetitionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompetitionService extends ServiceImpl<CompetitionMapper, Competition> {

    /**
     * 获取进行中的竞赛
     */
    public List<Competition> getOngoingCompetitions() {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 2)
                .orderByDesc(Competition::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 获取报名中的竞赛
     */
    public List<Competition> getRegisteringCompetitions() {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1)
                .orderByDesc(Competition::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 获取已结束的竞赛
     */
    public List<Competition> getEndedCompetitions() {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 3)
                .orderByDesc(Competition::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 获取热门竞赛（报名人数最多）
     */
    public List<Competition> getHotCompetitions(int limit) {
        return baseMapper.getHotCompetitions(limit);
    }

    /**
     * 获取竞赛统计信息
     */
    public Map<String, Object> getCompetitionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = this.count();
        long registering = this.count(new LambdaQueryWrapper<Competition>().eq(Competition::getStatus, 1));
        long ongoing = this.count(new LambdaQueryWrapper<Competition>().eq(Competition::getStatus, 2));
        long ended = this.count(new LambdaQueryWrapper<Competition>().eq(Competition::getStatus, 3));

        stats.put("total", total);
        stats.put("registering", registering);
        stats.put("ongoing", ongoing);
        stats.put("ended", ended);

        return stats;
    }

    /**
     * 按类别统计竞赛数量
     */
    public List<Map<String, Object>> countByCategory() {
        return baseMapper.countByCategory();
    }

    /**
     * 更新竞赛状态（根据时间自动更新）
     */
    public void updateCompetitionStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 报名中的竞赛，如果报名结束时间已过，改为进行中
        LambdaQueryWrapper<Competition> registeringWrapper = new LambdaQueryWrapper<>();
        registeringWrapper.eq(Competition::getStatus, 1)
                .lt(Competition::getEndTime, now);
        List<Competition> registeringCompetitions = this.list(registeringWrapper);
        for (Competition competition : registeringCompetitions) {
            competition.setStatus(2);
            this.updateById(competition);
        }

        // 进行中的竞赛，如果竞赛时间已过，改为已结束
        LambdaQueryWrapper<Competition> ongoingWrapper = new LambdaQueryWrapper<>();
        ongoingWrapper.eq(Competition::getStatus, 2)
                .lt(Competition::getCompetitionTime, now);
        List<Competition> ongoingCompetitions = this.list(ongoingWrapper);
        for (Competition competition : ongoingCompetitions) {
            competition.setStatus(3);
            this.updateById(competition);
        }

        // 未开始的竞赛，如果报名开始时间已到，改为报名中
        LambdaQueryWrapper<Competition> notStartedWrapper = new LambdaQueryWrapper<>();
        notStartedWrapper.eq(Competition::getStatus, 0)
                .le(Competition::getStartTime, now);
        List<Competition> notStartedCompetitions = this.list(notStartedWrapper);
        for (Competition competition : notStartedCompetitions) {
            competition.setStatus(1);
            this.updateById(competition);
        }
    }

    /**
     * 根据类别获取竞赛
     */
    public List<Competition> getCompetitionsByCategory(String category) {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getCategory, category)
                .orderByDesc(Competition::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 搜索竞赛
     */
    public List<Competition> searchCompetitions(String keyword) {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Competition::getTitle, keyword)
                .or()
                .like(Competition::getDescription, keyword)
                .orderByDesc(Competition::getCreateTime);
        return this.list(wrapper);
    }
}