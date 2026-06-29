package com.competition.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.competition.entity.*;
import com.competition.mapper.TeamMemberMapper;
import com.competition.mapper.TeamRecruitmentMapper;
import com.competition.service.*;
import com.competition.utils.JwtUtil;
import com.competition.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/team")
@CrossOrigin
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CompetitionRegistrationService competitionRegistrationService;

    @Autowired
    private TeamRecruitmentMapper teamRecruitmentMapper;


    @Autowired
    private GuidanceService guidanceService;

    // ==================== 团队创建 ====================

    /**
     * 创建团队
     */
    @PostMapping("/create")
    public ResultVO<Map<String, Object>> createTeam(@RequestBody Map<String, Object> params,
                                                    @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);

        // 只有学生可以创建团队
        if (role != 2) {
            return ResultVO.error("只有学生可以创建团队");
        }

        Long competitionId = Long.valueOf(params.get("competitionId").toString());
        String teamName = params.get("teamName").toString();
        String description = params.get("description") != null ?
                params.get("description").toString() : "";
        Integer maxMembers = params.get("maxMembers") != null ?
                Integer.parseInt(params.get("maxMembers").toString()) : 5;

        // 验证竞赛是否存在
        Competition competition = competitionService.getById(competitionId);
        if (competition == null) {
            return ResultVO.error("竞赛不存在");
        }

        // 验证队伍人数范围
        if (maxMembers < competition.getMinTeamMembers() || maxMembers > competition.getMaxTeamMembers()) {
            return ResultVO.error("队伍人数必须在 " + competition.getMinTeamMembers() +
                    " 到 " + competition.getMaxTeamMembers() + " 之间");
        }

        // 检查用户是否已在该竞赛中创建或加入团队
        boolean hasTeam = teamService.checkUserHasTeamInCompetition(userId, competitionId);
        if (hasTeam) {
            return ResultVO.error("您已在该竞赛中创建或加入团队");
        }

        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            return ResultVO.error("用户不存在");
        }

        // 创建团队
        Team team = new Team();
        team.setTeamName(teamName);
        team.setCompetitionId(competitionId);
        team.setLeaderId(userId);
        team.setDescription(description);
        team.setMaxMembers(maxMembers);
        team.setCurrentMembers(1);
        team.setStatus(0); // 招募中
        team.setCreateTime(LocalDateTime.now());

        boolean saved = teamService.save(team);
        if (!saved) {
            return ResultVO.error("创建团队失败");
        }

        // 添加队长为成员
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(userId);
        teamMember.setRole(1); // 队长
        teamMember.setStatus(1); // 正常
        teamMember.setJoinTime(LocalDateTime.now());
        teamService.saveTeamMember(teamMember);

        Map<String, Object> result = new HashMap<>();
        result.put("teamId", team.getId());
        result.put("teamName", team.getTeamName());
        result.put("leaderId", userId);
        result.put("leaderName", user.getRealName());
        result.put("leaderUsername", user.getUsername());

        log.info("用户 {} ({}) 创建团队成功: {}, 竞赛: {}",
                user.getRealName(), userId, teamName, competition.getTitle());

        return ResultVO.success("团队创建成功", result);
    }

    // ==================== 团队详情 ====================

    /**
     * 获取团队详情
     */
    @GetMapping("/{id}")
    public ResultVO<Map<String, Object>> getTeamDetail(@PathVariable Long id) {
        Team team = teamService.getById(id);
        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", team.getId());
        result.put("teamName", team.getTeamName());
        result.put("competitionId", team.getCompetitionId());
        result.put("leaderId", team.getLeaderId());
        result.put("description", team.getDescription());
        result.put("status", team.getStatus());
        result.put("statusText", getTeamStatusText(team.getStatus()));
        result.put("maxMembers", team.getMaxMembers());
        result.put("currentMembers", team.getCurrentMembers());
        result.put("createTime", team.getCreateTime());

        // 获取竞赛信息
        Competition competition = competitionService.getById(team.getCompetitionId());
        if (competition != null) {
            result.put("competitionTitle", competition.getTitle());
            result.put("competitionStatus", competition.getStatus());
        }

        // 获取队长信息
        User leader = userService.getById(team.getLeaderId());
        if (leader != null) {
            Map<String, Object> leaderInfo = new HashMap<>();
            leaderInfo.put("id", leader.getId());
            leaderInfo.put("username", leader.getUsername());
            leaderInfo.put("realName", leader.getRealName());
            leaderInfo.put("phone", leader.getPhone());
            leaderInfo.put("email", leader.getEmail());

            // 获取队长技能
            StudentDetail leaderDetail = userService.getStudentDetail(leader.getId());
            if (leaderDetail != null) {
                leaderInfo.put("major", leaderDetail.getMajor());
                leaderInfo.put("grade", leaderDetail.getGrade());
                leaderInfo.put("skills", leaderDetail.getSkills());
                // 解析技能列表
                if (leaderDetail.getSkills() != null) {
                    try {
                        List<String> skills = JSONUtil.toList(leaderDetail.getSkills(), String.class);
                        leaderInfo.put("skillList", skills);
                    } catch (Exception e) {
                        leaderInfo.put("skillList", Arrays.asList(leaderDetail.getSkills().split(",")));
                    }
                }
            }
            result.put("leader", leaderInfo);
        }

        // 获取成员列表
        List<Map<String, Object>> members = teamService.getTeamMembers(team.getId());
        result.put("members", members);

        // 获取招募需求
        List<Map<String, Object>> recruitments = teamService.getTeamRecruitments(team.getId());
        result.put("recruitments", recruitments);

        log.info("获取团队详情: {} (ID: {})", team.getTeamName(), team.getId());
        return ResultVO.success(result);
    }

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

    // ==================== 我的团队 ====================

    /**
     * 获取我的团队列表
     */
    @GetMapping("/my-teams")
    public ResultVO<List<Map<String, Object>>> getMyTeams(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        List<Map<String, Object>> teams = teamService.getTeamsByUser(userId);
        return ResultVO.success(teams);
    }

    // ==================== 智能推荐 ====================

    /**
     * 智能推荐队友
     */
    @GetMapping("/recommend/{competitionId}")
    public ResultVO<List<Map<String, Object>>> recommendTeamMembers(
            @PathVariable Long competitionId,
            @RequestHeader("Authorization") String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        StudentDetail currentStudent = userService.getStudentDetail(userId);
        if (currentStudent == null || currentStudent.getSkills() == null) {
            return ResultVO.success(Collections.emptyList());
        }

        List<String> currentSkills = JSONUtil.toList(currentStudent.getSkills(), String.class);

        Competition competition = competitionService.getById(competitionId);
        List<String> requiredSkills = new ArrayList<>();
        if (competition != null && competition.getRequiredSkills() != null) {
            requiredSkills = JSONUtil.toList(competition.getRequiredSkills(), String.class);
        }

        List<User> students = userService.getStudents();

        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (User student : students) {
            if (student.getId().equals(userId)) {
                continue;
            }

            boolean hasTeam = teamService.checkUserHasTeamInCompetition(student.getId(), competitionId);
            if (hasTeam) {
                continue;
            }

            StudentDetail studentDetail = userService.getStudentDetail(student.getId());
            if (studentDetail == null || studentDetail.getSkills() == null) {
                continue;
            }

            List<String> studentSkills = JSONUtil.toList(studentDetail.getSkills(), String.class);

            int matchScore = 0;
            Set<String> complementarySkills = new HashSet<>();

            for (String requiredSkill : requiredSkills) {
                if (!currentSkills.contains(requiredSkill) && studentSkills.contains(requiredSkill)) {
                    matchScore += 2;
                    complementarySkills.add(requiredSkill);
                }
            }

            for (String skill : studentSkills) {
                if (currentSkills.contains(skill)) {
                    matchScore += 1;
                }
            }

            if (matchScore > 0) {
                Map<String, Object> recommend = new HashMap<>();
                recommend.put("userId", student.getId());
                recommend.put("username", student.getUsername());
                recommend.put("realName", student.getRealName());
                recommend.put("major", studentDetail.getMajor());
                recommend.put("grade", studentDetail.getGrade());
                recommend.put("skills", studentSkills);
                recommend.put("matchScore", matchScore);
                recommend.put("complementarySkills", complementarySkills);
                recommendations.add(recommend);
            }
        }

        recommendations.sort((a, b) ->
                Integer.compare((int) b.get("matchScore"), (int) a.get("matchScore")));

        return ResultVO.success(recommendations);
    }

    // ==================== 团队申请 ====================

    /**
     * 申请加入团队
     */
    @PostMapping("/apply")
    public ResultVO<Void> applyToTeam(@RequestBody Map<String, Object> params,
                                      @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Long teamId = Long.valueOf(params.get("teamId").toString());
        String applyReason = params.get("applyReason") != null ?
                params.get("applyReason").toString() : "";

        Team team = teamService.getById(teamId);
        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 检查团队是否已满员
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            return ResultVO.error("团队已满员");
        }

        // 检查是否已在团队中
        if (teamService.checkUserInTeam(userId, teamId)) {
            return ResultVO.error("您已在该团队中");
        }

        // 检查是否已申请
        if (teamService.checkUserApplied(userId, teamId)) {
            return ResultVO.error("您已申请过该团队，请等待审核");
        }

        // 创建申请记录
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(teamId);
        teamMember.setUserId(userId);
        teamMember.setRole(0); // 队员
        teamMember.setStatus(2); // 待审核
        teamMember.setJoinTime(LocalDateTime.now());

        try {
            teamService.saveTeamMember(teamMember);

            // 发送通知给队长
            notificationService.sendTeamInviteNotification(
                    team.getLeaderId(),
                    team.getTeamName(),
                    teamId
            );

            log.info("用户 {} 申请加入团队 {}", userId, teamId);
            return ResultVO.success("申请已提交", null);
        } catch (Exception e) {
            log.error("申请失败", e);
            return ResultVO.error("申请失败");
        }
    }

    /**
     * 处理团队申请（队长）
     */
    @PutMapping("/handle-apply")
    public ResultVO<Void> handleApply(@RequestBody Map<String, Object> params,
                                      @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Long teamMemberId = Long.valueOf(params.get("teamMemberId").toString());
        Integer action = Integer.valueOf(params.get("action").toString());
        String comment = params.get("comment") != null ? params.get("comment").toString() : "";

        TeamMember teamMember = teamService.getTeamMemberById(teamMemberId);
        if (teamMember == null) {
            return ResultVO.error("申请记录不存在");
        }

        Team team = teamService.getById(teamMember.getTeamId());
        if (!team.getLeaderId().equals(userId)) {
            return ResultVO.error("只有队长可以处理申请");
        }

        if (action == 1) { // 同意
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                return ResultVO.error("团队已满员");
            }

            teamMember.setStatus(1); // 正常
            team.setCurrentMembers(team.getCurrentMembers() + 1);
            teamService.updateById(team);
            teamService.updateTeamMember(teamMember);

            // 发送通知给申请人
            notificationService.sendTeamApplyResultNotification(
                    teamMember.getUserId(),
                    team.getTeamName(),
                    true,
                    null,
                    team.getId()
            );

            log.info("用户 {} 同意加入团队 {}", userId, team.getId());
            return ResultVO.success("已同意加入", null);

        } else { // 拒绝
            teamMember.setStatus(3); // 已拒绝
            teamService.updateTeamMember(teamMember);

            // 发送拒绝通知
            notificationService.sendTeamApplyResultNotification(
                    teamMember.getUserId(),
                    team.getTeamName(),
                    false,
                    comment,
                    team.getId()
            );

            log.info("用户 {} 拒绝加入团队 {}", userId, team.getId());
            return ResultVO.success("已拒绝", null);
        }
    }

    /**
     * 获取待处理申请列表（队长）
     */
    @GetMapping("/pending-applications/{teamId}")
    public ResultVO<List<Map<String, Object>>> getPendingApplications(
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Team team = teamService.getById(teamId);

        if (team == null || !team.getLeaderId().equals(userId)) {
            return ResultVO.error("无权限查看");
        }

        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, 2); // 待审核状态

        List<TeamMember> members = teamMemberMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (TeamMember member : members) {
            User user = userService.getById(member.getUserId());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("teamMemberId", member.getId());
                item.put("userId", user.getId());
                item.put("username", user.getUsername());
                item.put("realName", user.getRealName());
                item.put("applyTime", member.getJoinTime());
                result.add(item);
            }
        }

        return ResultVO.success(result);
    }

    // ==================== 团队管理 ====================

    /**
     * 退出团队
     */
    @DeleteMapping("/quit/{teamId}")
    public ResultVO<Void> quitTeam(@PathVariable Long teamId,
                                   @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Team team = teamService.getById(teamId);

        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 队长不能直接退出，需要先转让队长或解散团队
        if (team.getLeaderId().equals(userId)) {
            return ResultVO.error("队长不能直接退出团队，请先转让队长权限或解散团队");
        }

        // 查找成员关系
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, 1);
        TeamMember teamMember = teamMemberMapper.selectOne(wrapper);

        if (teamMember == null) {
            return ResultVO.error("您不是该团队成员");
        }

        // 更新状态为已退出
        teamMember.setStatus(0); // 已退出
        teamMemberMapper.updateById(teamMember);

        // 更新团队人数
        team.setCurrentMembers(team.getCurrentMembers() - 1);
        teamService.updateById(team);

        log.info("用户 {} 退出团队 {}", userId, teamId);
        return ResultVO.success("已退出团队", null);
    }

    /**
     * 解散团队（队长或管理员）
     */
    @DeleteMapping("/disband/{teamId}")
    public ResultVO<Void> disbandTeam(@PathVariable Long teamId,
                                      @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        Team team = teamService.getById(teamId);

        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 只有队长或管理员可以解散团队
        if (!team.getLeaderId().equals(userId) && role != 0) {
            return ResultVO.error("只有队长或管理员可以解散团队");
        }

        // 获取所有成员，准备发送通知
        List<Long> memberIds = teamService.getTeamMemberIds(teamId);

        // ========== 关键修复：先删除外键关联的数据 ==========

        // 1. 删除竞赛报名记录
        LambdaQueryWrapper<CompetitionRegistration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(CompetitionRegistration::getTeamId, teamId);
        competitionRegistrationService.remove(regWrapper);

        // 2. 删除指导申请记录
        LambdaQueryWrapper<GuidanceRelation> guidanceWrapper = new LambdaQueryWrapper<>();
        guidanceWrapper.eq(GuidanceRelation::getTeamId, teamId);
        guidanceService.remove(guidanceWrapper);

        // 3. 删除团队成员
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, teamId);
        teamMemberMapper.delete(memberWrapper);

        // 4. 删除团队招募需求
        if (teamRecruitmentMapper != null) {
            LambdaQueryWrapper<TeamRecruitment> recruitmentWrapper = new LambdaQueryWrapper<>();
            recruitmentWrapper.eq(TeamRecruitment::getTeamId, teamId);
            teamRecruitmentMapper.delete(recruitmentWrapper);
        }

        // 5. 删除团队
        boolean removed = teamService.removeById(teamId);

        if (removed) {
            // 发送解散通知给所有成员
            for (Long memberId : memberIds) {
                Notification notification = new Notification();
                notification.setUserId(memberId);
                notification.setTitle("团队解散通知");
                notification.setContent("您所在的团队【" + team.getTeamName() + "】已被解散");
                notification.setType(1);
                notification.setRelatedId(teamId);
                notification.setIsRead(0);
                notification.setCreateTime(LocalDateTime.now());
                teamService.saveNotification(notification);
            }

            log.info("用户 {} 或管理员 {} 解散团队 {}", userId, role == 0 ? "(管理员)" : "", teamId);
            return ResultVO.success("团队已解散", null);
        }

        return ResultVO.error("解散失败");
    }

    /**
     * 转让队长权限
     */
    @PutMapping("/transfer-leader")
    public ResultVO<Void> transferLeader(@RequestBody Map<String, Object> params,
                                         @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Long teamId = Long.valueOf(params.get("teamId").toString());
        Long newLeaderId = Long.valueOf(params.get("newLeaderId").toString());

        Team team = teamService.getById(teamId);
        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 只有队长可以转让
        if (!team.getLeaderId().equals(userId)) {
            return ResultVO.error("只有队长可以转让权限");
        }

        // 检查新队长是否在团队中
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, newLeaderId)
                .eq(TeamMember::getStatus, 1);
        TeamMember newLeaderMember = teamMemberMapper.selectOne(wrapper);

        if (newLeaderMember == null) {
            return ResultVO.error("新队长不在团队中");
        }

        // 更新团队队长
        team.setLeaderId(newLeaderId);
        teamService.updateById(team);

        // 更新原队长为普通队员
        LambdaQueryWrapper<TeamMember> oldLeaderWrapper = new LambdaQueryWrapper<>();
        oldLeaderWrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId);
        TeamMember oldLeaderMember = teamMemberMapper.selectOne(oldLeaderWrapper);
        if (oldLeaderMember != null) {
            oldLeaderMember.setRole(0);
            teamMemberMapper.updateById(oldLeaderMember);
        }

        // 新队长设置为队长
        newLeaderMember.setRole(1);
        teamMemberMapper.updateById(newLeaderMember);

        log.info("用户 {} 将团队 {} 转让给 {}", userId, teamId, newLeaderId);
        return ResultVO.success("队长转让成功", null);
    }

    // ==================== 竞赛团队查询 ====================

    /**
     * 获取竞赛的所有团队（分页）
     */
    @GetMapping("/competition/{competitionId}")
    public ResultVO<Map<String, Object>> getTeamsByCompetition(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getCompetitionId, competitionId)
                .eq(Team::getStatus, 0) // 只显示招募中的团队
                .orderByDesc(Team::getCreateTime);

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Team::getTeamName, keyword);
        }

        Page<Team> pageParam = new Page<>(page, size);
        Page<Team> teamPage = teamService.page(pageParam, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Team team : teamPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", team.getId());
            item.put("teamName", team.getTeamName());
            item.put("description", team.getDescription());
            item.put("currentMembers", team.getCurrentMembers());
            item.put("maxMembers", team.getMaxMembers());
            item.put("status", team.getStatus());

            User leader = userService.getById(team.getLeaderId());
            item.put("leaderName", leader != null ? leader.getRealName() : "");

            // 获取招募需求
            List<Map<String, Object>> recruitments = teamService.getTeamRecruitments(team.getId());
            item.put("recruitments", recruitments);

            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", teamPage.getTotal());
        result.put("page", teamPage.getCurrent());
        result.put("size", teamPage.getSize());

        return ResultVO.success(result);
    }

    /**
     * 获取用户申请的团队列表
     */
    @GetMapping("/my-applications")
    public ResultVO<List<Map<String, Object>>> getMyApplications(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, 2); // 待审核状态
        List<TeamMember> applications = teamMemberMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (TeamMember app : applications) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", app.getId());
            item.put("teamId", app.getTeamId());

            Team team = teamService.getById(app.getTeamId());
            if (team != null) {
                item.put("teamName", team.getTeamName());
            }

            item.put("applyTime", app.getJoinTime());
            result.add(item);
        }

        return ResultVO.success(result);
    }

    // ==================== 招募需求管理 ====================

    /**
     * 发布招募需求
     */
    @PostMapping("/recruitment")
    public ResultVO<Void> addRecruitment(@RequestBody Map<String, Object> params,
                                         @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Long teamId = Long.valueOf(params.get("teamId").toString());

        Team team = teamService.getById(teamId);
        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 只有队长可以发布招募需求
        if (!team.getLeaderId().equals(userId)) {
            return ResultVO.error("只有队长可以发布招募需求");
        }

        String needSkills = params.get("needSkills").toString();
        Integer needCount = Integer.valueOf(params.get("needCount").toString());
        String description = params.get("description") != null ?
                params.get("description").toString() : "";

        TeamRecruitment recruitment = new TeamRecruitment();
        recruitment.setTeamId(teamId);
        recruitment.setNeedSkills(needSkills);
        recruitment.setNeedCount(needCount);
        recruitment.setDescription(description);
        recruitment.setStatus(1);
        recruitment.setCreateTime(LocalDateTime.now());

        boolean saved = teamService.addTeamRecruitment(recruitment);
        if (saved) {
            log.info("用户 {} 为团队 {} 发布招募需求", userId, teamId);
            return ResultVO.success("发布成功", null);
        }
        return ResultVO.error("发布失败");
    }

    /**
     * 关闭招募需求
     */
    @PutMapping("/recruitment/{recruitmentId}/close")
    public ResultVO<Void> closeRecruitment(@PathVariable Long recruitmentId,
                                           @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        // 获取招募需求所属团队
        TeamRecruitment recruitment = teamService.getRecruitmentById(recruitmentId);
        if (recruitment == null) {
            return ResultVO.error("招募需求不存在");
        }

        Team team = teamService.getById(recruitment.getTeamId());
        if (team == null || !team.getLeaderId().equals(userId)) {
            return ResultVO.error("无权限操作");
        }

        boolean closed = teamService.closeRecruitment(recruitmentId);
        if (closed) {
            log.info("用户 {} 关闭招募需求 {}", userId, recruitmentId);
            return ResultVO.success("已关闭", null);
        }
        return ResultVO.error("操作失败");
    }

    // ==================== 团队统计 ====================

    /**
     * 获取团队统计信息
     */
    @GetMapping("/statistics")
    public ResultVO<Map<String, Object>> getTeamStatistics(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);

        Map<String, Object> stats = new HashMap<>();

        if (role == 2) { // 学生
            // 学生参与的团队数
            long myTeamCount = teamService.getTeamsByUser(userId).size();
            // 学生创建的团队数
            long myCreatedTeamCount = teamService.getTeamsByUserLeader(userId).size();
            stats.put("myTeamCount", myTeamCount);
            stats.put("myCreatedTeamCount", myCreatedTeamCount);
        } else if (role == 1) { // 教师
            // 教师指导的团队数
            long guidanceTeamCount = teamService.getGuidanceTeamCount(userId);
            stats.put("guidanceTeamCount", guidanceTeamCount);
        }

        // 系统统计
        stats.put("totalTeams", teamService.getTeamCount());
        stats.put("activeTeams", teamService.getActiveTeamCount());
        stats.put("todayNewTeams", teamService.getTodayNewTeamCount());

        return ResultVO.success(stats);

    }
    /**
     * 获取团队列表（管理员）
     */
    @GetMapping("/admin/list")
    public ResultVO<Map<String, Object>> getAdminTeamList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {

        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限访问");
        }

        // 构建查询条件
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Team::getTeamName, keyword);
        }
        if (status != null) {
            wrapper.eq(Team::getStatus, status);
        }

        wrapper.orderByDesc(Team::getCreateTime);

        // 分页查询
        Page<Team> pageParam = new Page<>(page, size);
        Page<Team> teamPage = teamService.page(pageParam, wrapper);

        // 构建返回数据
        List<Map<String, Object>> records = new ArrayList<>();
        for (Team team : teamPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", team.getId());
            item.put("teamName", team.getTeamName());
            item.put("description", team.getDescription());
            item.put("currentMembers", team.getCurrentMembers());
            item.put("maxMembers", team.getMaxMembers());
            item.put("status", team.getStatus());
            item.put("createTime", team.getCreateTime());

            // 获取竞赛信息
            Competition competition = competitionService.getById(team.getCompetitionId());
            if (competition != null) {
                item.put("competitionTitle", competition.getTitle());
            }

            // 获取队长信息
            User leader = userService.getById(team.getLeaderId());
            if (leader != null) {
                item.put("leaderName", leader.getRealName());
                item.put("leaderId", leader.getId());
            }

            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", teamPage.getTotal());
        result.put("page", teamPage.getCurrent());
        result.put("size", teamPage.getSize());

        return ResultVO.success(result);
    }

    // 移除成员
    @DeleteMapping("/{teamId}/member/{userId}")
    public ResultVO<Void> removeMember(@PathVariable Long teamId,
                                       @PathVariable Long userId,
                                       @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        Team team = teamService.getById(teamId);

        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 只有队长可以移除成员
        if (!team.getLeaderId().equals(currentUserId)) {
            return ResultVO.error("只有队长可以移除成员");
        }

        // 不能移除自己（队长）
        if (team.getLeaderId().equals(userId)) {
            return ResultVO.error("不能移除队长");
        }

        // 查找成员关系
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, 1);
        TeamMember teamMember = teamMemberMapper.selectOne(wrapper);

        if (teamMember == null) {
            return ResultVO.error("成员不存在");
        }

        // 移除成员
        teamMember.setStatus(0);
        teamMemberMapper.updateById(teamMember);

        // 更新团队人数
        team.setCurrentMembers(team.getCurrentMembers() - 1);
        teamService.updateById(team);

        // 发送通知
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("团队移除通知");
        notification.setContent("您已被移出团队 " + team.getTeamName());
        notification.setType(1);
        notification.setRelatedId(teamId);
        notification.setCreateTime(LocalDateTime.now());
        teamService.saveNotification(notification);

        return ResultVO.success("已移除成员", null);
    }

    /**
     * 更新团队状态（队长）
     */
    @PutMapping("/{teamId}/status")
    public ResultVO<Void> updateTeamStatus(@PathVariable Long teamId,
                                           @RequestBody Map<String, Object> params,
                                           @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer status = Integer.valueOf(params.get("status").toString());

        // 验证状态值有效性
        if (status < 0 || status > 2) {
            return ResultVO.error("无效的状态值");
        }

        Team team = teamService.getById(teamId);
        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 只有队长可以修改状态
        if (!team.getLeaderId().equals(userId)) {
            return ResultVO.error("只有队长可以修改团队状态");
        }

        // 已报名的团队不能修改状态
        LambdaQueryWrapper<CompetitionRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitionRegistration::getTeamId, teamId);
        if (competitionRegistrationService.count(wrapper) > 0) {
            return ResultVO.error("团队已报名竞赛，无法修改状态");
        }

        team.setStatus(status);
        team.setUpdateTime(LocalDateTime.now());
        boolean updated = teamService.updateById(team);

        if (updated) {
            String statusText = status == 0 ? "招募中" : (status == 1 ? "已满员" : "已解散");
            log.info("用户 {} 更新团队 {} 状态为 {}", userId, teamId, statusText);
            return ResultVO.success("状态已更新为" + statusText, null);
        }
        return ResultVO.error("状态更新失败");
    }
}