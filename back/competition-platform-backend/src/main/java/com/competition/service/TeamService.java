package com.competition.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.entity.*;
import com.competition.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamService extends ServiceImpl<TeamMapper, Team> {

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private TeamRecruitmentMapper teamRecruitmentMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private GuidanceRelationMapper guidanceRelationMapper;

    // ==================== 团队成员操作 ====================

    /**
     * 保存团队成员
     */
    public void saveTeamMember(TeamMember teamMember) {
        teamMemberMapper.insert(teamMember);
    }

    /**
     * 更新团队成员
     */
    public void updateTeamMember(TeamMember teamMember) {
        teamMemberMapper.updateById(teamMember);
    }

    /**
     * 根据ID获取团队成员
     */
    public TeamMember getTeamMemberById(Long id) {
        return teamMemberMapper.selectById(id);
    }

    /**
     * 删除团队成员
     */
    public void deleteTeamMember(Long id) {
        teamMemberMapper.deleteById(id);
    }

    /**
     * 根据团队ID获取所有成员
     */
    public List<TeamMember> getMembersByTeamId(Long teamId) {
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, 1);
        return teamMemberMapper.selectList(wrapper);
    }

    /**
     * 获取团队成员列表（带用户信息）
     */
    public List<Map<String, Object>> getTeamMembers(Long teamId) {
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, 1)
                .orderByDesc(TeamMember::getRole);
        List<TeamMember> members = teamMemberMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (TeamMember member : members) {
            User user = userService.getById(member.getUserId());
            if (user != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getId());
                map.put("username", user.getUsername());
                map.put("realName", user.getRealName());
                map.put("role", member.getRole());
                map.put("roleName", member.getRole() == 1 ? "队长" : "队员");
                map.put("joinTime", member.getJoinTime());

                StudentDetail studentDetail = userService.getStudentDetail(user.getId());
                if (studentDetail != null) {
                    map.put("major", studentDetail.getMajor());
                    map.put("grade", studentDetail.getGrade());
                    map.put("skills", studentDetail.getSkills());
                    if (studentDetail.getSkills() != null) {
                        try {
                            List<String> skills = JSONUtil.toList(studentDetail.getSkills(), String.class);
                            map.put("skillList", skills);
                        } catch (Exception e) {
                            map.put("skillList", Arrays.asList(studentDetail.getSkills().split(",")));
                        }
                    }
                }
                result.add(map);
            }
        }
        return result;
    }

    /**
     * 获取团队成员ID列表
     */
    public List<Long> getTeamMemberIds(Long teamId) {
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, 1)
                .select(TeamMember::getUserId);
        List<TeamMember> members = teamMemberMapper.selectList(wrapper);
        return members.stream()
                .map(TeamMember::getUserId)
                .collect(Collectors.toList());
    }

    // ==================== 团队状态检查 ====================

    /**
     * 检查用户是否在指定竞赛中已有团队
     */
    public boolean checkUserHasTeamInCompetition(Long userId, Long competitionId) {
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, 1);
        List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);

        for (TeamMember member : members) {
            Team team = this.getById(member.getTeamId());
            if (team != null && team.getCompetitionId().equals(competitionId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户是否在团队中
     */
    public boolean checkUserInTeam(Long userId, Long teamId) {
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, 1);
        return teamMemberMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查用户是否已申请加入团队
     */
    public boolean checkUserApplied(Long userId, Long teamId) {
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, 2);
        return teamMemberMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查用户是否是队长
     */
    public boolean checkUserIsLeader(Long userId, Long teamId) {
        Team team = this.getById(teamId);
        return team != null && team.getLeaderId().equals(userId);
    }

    // ==================== 用户团队查询 ====================

    /**
     * 获取用户参与的所有团队
     */
    public List<Map<String, Object>> getTeamsByUser(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, 1);
        List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);

        for (TeamMember member : members) {
            Team team = this.getById(member.getTeamId());
            if (team != null) {
                Map<String, Object> teamInfo = new HashMap<>();
                teamInfo.put("teamId", team.getId());
                teamInfo.put("teamName", team.getTeamName());
                teamInfo.put("role", member.getRole() == 1 ? "队长" : "队员");
                teamInfo.put("roleCode", member.getRole());

                Competition competition = competitionService.getById(team.getCompetitionId());
                teamInfo.put("competitionTitle", competition != null ? competition.getTitle() : "");
                teamInfo.put("competitionId", team.getCompetitionId());

                teamInfo.put("memberCount", team.getCurrentMembers());
                teamInfo.put("maxMembers", team.getMaxMembers());
                teamInfo.put("status", team.getStatus());
                teamInfo.put("statusText", getTeamStatusText(team.getStatus()));
                teamInfo.put("description", team.getDescription());
                teamInfo.put("createTime", team.getCreateTime());

                User leader = userService.getById(team.getLeaderId());
                if (leader != null) {
                    teamInfo.put("leaderId", leader.getId());
                    teamInfo.put("leaderName", leader.getRealName());
                    teamInfo.put("leaderUsername", leader.getUsername());
                }

                result.add(teamInfo);
            }
        }
        return result;
    }

    /**
     * 获取用户作为队长的团队（返回Team实体列表）
     */
    public List<Team> getTeamsByUserLeader(Long userId) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getLeaderId, userId);
        return this.list(wrapper);
    }

    /**
     * 获取竞赛的所有团队（返回Team实体列表）- 用于批量操作
     */
    public List<Team> getTeamsByCompetition(Long competitionId) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getCompetitionId, competitionId)
                .orderByDesc(Team::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 获取竞赛的所有团队（返回Map格式，包含详细信息）- 用于前端展示
     */
    public List<Map<String, Object>> getTeamsByCompetitionWithDetail(Long competitionId) {
        List<Team> teams = getTeamsByCompetition(competitionId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Team team : teams) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("id", team.getId());
            teamInfo.put("teamName", team.getTeamName());
            teamInfo.put("leaderId", team.getLeaderId());
            teamInfo.put("currentMembers", team.getCurrentMembers());
            teamInfo.put("maxMembers", team.getMaxMembers());
            teamInfo.put("status", team.getStatus());
            teamInfo.put("statusText", getTeamStatusText(team.getStatus()));

            User leader = userService.getById(team.getLeaderId());
            if (leader != null) {
                teamInfo.put("leaderName", leader.getRealName());
            }

            result.add(teamInfo);
        }
        return result;
    }

    // ==================== 团队招募需求 ====================

    /**
     * 获取团队招募需求
     */
    public List<Map<String, Object>> getTeamRecruitments(Long teamId) {
        if (teamRecruitmentMapper == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TeamRecruitment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamRecruitment::getTeamId, teamId)
                .eq(TeamRecruitment::getStatus, 1);
        List<TeamRecruitment> recruitments = teamRecruitmentMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (TeamRecruitment recruitment : recruitments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", recruitment.getId());
            map.put("needSkills", recruitment.getNeedSkills());
            map.put("needCount", recruitment.getNeedCount());
            map.put("description", recruitment.getDescription());
            map.put("status", recruitment.getStatus());
            result.add(map);
        }
        return result;
    }

    /**
     * 添加团队招募需求
     */
    @Transactional
    public boolean addTeamRecruitment(TeamRecruitment recruitment) {
        if (teamRecruitmentMapper == null) {
            return false;
        }
        recruitment.setCreateTime(LocalDateTime.now());
        return teamRecruitmentMapper.insert(recruitment) > 0;
    }

    /**
     * 关闭招募需求
     */
    @Transactional
    public boolean closeRecruitment(Long recruitmentId) {
        if (teamRecruitmentMapper == null) {
            return false;
        }
        TeamRecruitment recruitment = teamRecruitmentMapper.selectById(recruitmentId);
        if (recruitment != null) {
            recruitment.setStatus(0);
            return teamRecruitmentMapper.updateById(recruitment) > 0;
        }
        return false;
    }

    /**
     * 根据ID获取招募需求
     */
    public TeamRecruitment getRecruitmentById(Long recruitmentId) {
        if (teamRecruitmentMapper == null) {
            return null;
        }
        return teamRecruitmentMapper.selectById(recruitmentId);
    }

    // ==================== 通知操作 ====================

    /**
     * 保存通知
     */
    public void saveNotification(Notification notification) {
        notificationMapper.insert(notification);
    }

    /**
     * 批量创建通知
     */
    public void batchSaveNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            notificationMapper.insert(notification);
        }
    }

    /**
     * 发送团队加入申请通知
     */
    public void sendApplyNotification(Long teamId, Long applicantId) {
        Team team = this.getById(teamId);
        if (team == null) return;

        User applicant = userService.getById(applicantId);
        if (applicant == null) return;

        Notification notification = new Notification();
        notification.setUserId(team.getLeaderId());
        notification.setTitle("团队加入申请");
        notification.setContent(applicant.getRealName() + " 申请加入您的团队 " + team.getTeamName());
        notification.setType(1);
        notification.setRelatedId(teamId);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    /**
     * 发送团队申请结果通知
     */
    public void sendApplyResultNotification(Long userId, Long teamId, boolean accepted, String reason) {
        Team team = this.getById(teamId);
        if (team == null) return;

        Notification notification = new Notification();
        notification.setUserId(userId);
        if (accepted) {
            notification.setTitle("加入团队申请已通过");
            notification.setContent("您的申请已通过，已成功加入团队 " + team.getTeamName());
        } else {
            notification.setTitle("加入团队申请被拒绝");
            notification.setContent("您的申请已被队长拒绝" + (reason != null && !reason.isEmpty() ? "，原因：" + reason : ""));
        }
        notification.setType(1);
        notification.setRelatedId(teamId);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    // ==================== 团队管理操作 ====================

    /**
     * 更新团队状态
     */
    @Transactional
    public boolean updateTeamStatus(Long teamId, Integer status) {
        Team team = this.getById(teamId);
        if (team == null) return false;
        team.setStatus(status);
        team.setUpdateTime(LocalDateTime.now());
        return this.updateById(team);
    }

    /**
     * 增加团队成员数
     */
    @Transactional
    public void incrementMemberCount(Long teamId) {
        Team team = this.getById(teamId);
        if (team != null) {
            team.setCurrentMembers(team.getCurrentMembers() + 1);
            this.updateById(team);
        }
    }

    /**
     * 减少团队成员数
     */
    @Transactional
    public void decrementMemberCount(Long teamId) {
        Team team = this.getById(teamId);
        if (team != null) {
            team.setCurrentMembers(team.getCurrentMembers() - 1);
            this.updateById(team);
        }
    }

    /**
     * 解散团队
     */
    @Transactional
    public boolean disbandTeam(Long teamId) {
        // 删除所有团队成员
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, teamId);
        teamMemberMapper.delete(memberWrapper);

        // 删除团队招募需求
        if (teamRecruitmentMapper != null) {
            LambdaQueryWrapper<TeamRecruitment> recruitmentWrapper = new LambdaQueryWrapper<>();
            recruitmentWrapper.eq(TeamRecruitment::getTeamId, teamId);
            teamRecruitmentMapper.delete(recruitmentWrapper);
        }

        // 删除团队
        return this.removeById(teamId);
    }

    /**
     * 转让队长
     */
    @Transactional
    public boolean transferLeader(Long teamId, Long currentLeaderId, Long newLeaderId) {
        Team team = this.getById(teamId);
        if (team == null || !team.getLeaderId().equals(currentLeaderId)) {
            return false;
        }

        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, newLeaderId)
                .eq(TeamMember::getStatus, 1);
        TeamMember newLeaderMember = teamMemberMapper.selectOne(wrapper);
        if (newLeaderMember == null) {
            return false;
        }

        team.setLeaderId(newLeaderId);
        team.setUpdateTime(LocalDateTime.now());
        this.updateById(team);

        LambdaQueryWrapper<TeamMember> oldLeaderWrapper = new LambdaQueryWrapper<>();
        oldLeaderWrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, currentLeaderId);
        TeamMember oldLeaderMember = teamMemberMapper.selectOne(oldLeaderWrapper);
        if (oldLeaderMember != null) {
            oldLeaderMember.setRole(0);
            teamMemberMapper.updateById(oldLeaderMember);
        }

        newLeaderMember.setRole(1);
        teamMemberMapper.updateById(newLeaderMember);

        return true;
    }

    // ==================== 统计方法 ====================

    /**
     * 获取团队总数
     */
    public long getTotalTeams() {
        return this.count();
    }

    /**
     * 获取团队总数（别名，兼容旧代码）
     */
    public long getTeamCount() {
        return getTotalTeams();
    }

    /**
     * 获取活跃团队数（招募中的团队）
     */
    public long getActiveTeams() {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getStatus, 0);
        return this.count(wrapper);
    }

    /**
     * 获取活跃团队数（别名，兼容旧代码）
     */
    public long getActiveTeamCount() {
        return getActiveTeams();
    }

    /**
     * 获取今日新增团队数
     */
    public long getTodayNewTeams() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Team::getCreateTime, todayStart);
        return this.count(wrapper);
    }

    /**
     * 获取今日新增团队数（别名，兼容旧代码）
     */
    public long getTodayNewTeamCount() {
        return getTodayNewTeams();
    }

    /**
     * 获取竞赛的团队数量
     */
    public long getTeamCountByCompetition(Long competitionId) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getCompetitionId, competitionId);
        return this.count(wrapper);
    }

    /**
     * 获取教师指导的团队数量
     */
    public long getGuidanceTeamCount(Long teacherId) {
        LambdaQueryWrapper<GuidanceRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuidanceRelation::getTeacherId, teacherId)
                .eq(GuidanceRelation::getStatus, 1);
        return guidanceRelationMapper.selectCount(wrapper);
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取团队状态文本
     */
    private String getTeamStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "招募中";
            case 1: return "已满员";
            case 2: return "已解散";
            default: return "未知";
        }
    }

}