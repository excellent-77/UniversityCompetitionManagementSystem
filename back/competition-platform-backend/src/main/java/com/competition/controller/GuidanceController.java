package com.competition.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.competition.entity.*;
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
@RequestMapping("/guidance")
@CrossOrigin
public class GuidanceController {

    @Autowired
    private GuidanceService guidanceService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 智能推荐指导教师
     */
    @GetMapping("/recommend-teachers/{teamId}")
    public ResultVO<List<Map<String, Object>>> recommendTeachers(
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Team team = teamService.getById(teamId);

        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        if (!team.getLeaderId().equals(userId)) {
            return ResultVO.error("只有队长可以申请指导");
        }

        Competition competition = competitionService.getById(team.getCompetitionId());
        List<User> teachers = userService.getAvailableTeachers();

        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (User teacher : teachers) {
            TeacherDetail teacherDetail = userService.getTeacherDetail(teacher.getId());
            if (teacherDetail == null) continue;

            if (teacherDetail.getCurrentTeams() >= teacherDetail.getMaxTeams()) {
                continue;
            }

            int matchScore = 0;
            List<String> matchedDirections = new ArrayList<>();

            if (competition != null && competition.getCategory() != null
                    && teacherDetail.getResearchDirection() != null) {
                if (teacherDetail.getResearchDirection().contains(competition.getCategory())) {
                    matchScore += 3;
                    matchedDirections.add(competition.getCategory());
                }
            }

            if (competition != null && competition.getRequiredSkills() != null) {
                List<String> requiredSkills = cn.hutool.json.JSONUtil.toList(competition.getRequiredSkills(), String.class);
                for (String skill : requiredSkills) {
                    if (teacherDetail.getResearchDirection() != null
                            && teacherDetail.getResearchDirection().contains(skill)) {
                        matchScore += 1;
                        matchedDirections.add(skill);
                    }
                }
            }

            Map<String, Object> recommend = new HashMap<>();
            recommend.put("teacherId", teacher.getId());
            recommend.put("teacherName", teacher.getRealName());
            recommend.put("department", teacherDetail.getDepartment());
            recommend.put("title", teacherDetail.getTitle());
            recommend.put("researchDirection", teacherDetail.getResearchDirection());
            recommend.put("currentTeams", teacherDetail.getCurrentTeams());
            recommend.put("maxTeams", teacherDetail.getMaxTeams());
            recommend.put("matchScore", matchScore);
            recommend.put("matchedDirections", matchedDirections);

            recommendations.add(recommend);
        }

        recommendations.sort((a, b) ->
                Integer.compare((int) b.get("matchScore"), (int) a.get("matchScore")));

        return ResultVO.success(recommendations);
    }

    /**
     * 申请指导
     */
    @PostMapping("/apply")
    public ResultVO<Void> applyGuidance(@RequestBody Map<String, Object> params,
                                        @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Long teacherId = Long.valueOf(params.get("teacherId").toString());
        Long teamId = Long.valueOf(params.get("teamId").toString());
        String applyReason = params.get("applyReason") != null ?
                params.get("applyReason").toString() : "";

        Team team = teamService.getById(teamId);
        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        if (!team.getLeaderId().equals(userId)) {
            return ResultVO.error("只有队长可以申请指导");
        }

        // 检查是否已申请过（包括待确认、已接受、已拒绝）
        LambdaQueryWrapper<GuidanceRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuidanceRelation::getTeacherId, teacherId)
                .eq(GuidanceRelation::getTeamId, teamId);
        GuidanceRelation existing = guidanceService.getOne(wrapper);

        if (existing != null) {
            // 根据状态返回不同提示
            if (existing.getStatus() == 0) {
                return ResultVO.error("申请正在审核中，请耐心等待");
            } else if (existing.getStatus() == 1) {
                return ResultVO.error("指导申请已通过，无需重复申请");
            } else if (existing.getStatus() == 2) {
                // 如果被拒绝，允许重新申请
                // 更新状态为待确认，并更新申请理由
                existing.setStatus(0);
                existing.setApplyReason(applyReason);
                existing.setTeacherComment(null);
                existing.setUpdateTime(LocalDateTime.now());
                guidanceService.updateById(existing);

                // 发送通知给教师
                notificationService.sendGuidanceApplyNotification(teacherId, team.getTeamName(), teamId);

                log.info("团队 {} 重新向教师 {} 申请指导", team.getTeamName(), teacherId);
                return ResultVO.success("申请已重新发送", null);
            } else if (existing.getStatus() == 3) {
                // 已取消，允许重新申请
                existing.setStatus(0);
                existing.setApplyReason(applyReason);
                existing.setTeacherComment(null);
                existing.setUpdateTime(LocalDateTime.now());
                guidanceService.updateById(existing);

                notificationService.sendGuidanceApplyNotification(teacherId, team.getTeamName(), teamId);

                return ResultVO.success("申请已发送", null);
            }
            return ResultVO.error("已向该教师发送过申请");
        }

        GuidanceRelation relation = new GuidanceRelation();
        relation.setTeacherId(teacherId);
        relation.setTeamId(teamId);
        relation.setStatus(0);
        relation.setApplyReason(applyReason);
        relation.setCreateTime(LocalDateTime.now());

        boolean saved = guidanceService.save(relation);
        if (saved) {
            // 发送通知给教师
            User teacher = userService.getById(teacherId);
            notificationService.sendGuidanceApplyNotification(teacherId, team.getTeamName(), teamId);

            log.info("团队 {} 向教师 {} 申请指导", team.getTeamName(), teacher.getRealName());
            return ResultVO.success("申请已发送", null);
        }

        return ResultVO.error("申请失败");
    }

    /**
     * 处理指导申请（教师）
     */
    @PutMapping("/handle")
    public ResultVO<Void> handleGuidance(@RequestBody Map<String, Object> params,
                                         @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long teacherId = jwtUtil.getUserIdFromToken(token);
        Long relationId = Long.valueOf(params.get("relationId").toString());
        Integer action = Integer.valueOf(params.get("action").toString());
        String comment = params.get("comment") != null ?
                params.get("comment").toString() : "";

        GuidanceRelation relation = guidanceService.getById(relationId);
        if (relation == null) {
            return ResultVO.error("申请记录不存在");
        }

        if (!relation.getTeacherId().equals(teacherId)) {
            return ResultVO.error("无权限处理此申请");
        }

        TeacherDetail teacherDetail = userService.getTeacherDetail(teacherId);

        if (action == 1) { // 接受
            if (teacherDetail.getCurrentTeams() >= teacherDetail.getMaxTeams()) {
                return ResultVO.error("您的指导名额已满");
            }

            relation.setStatus(1);
            relation.setTeacherComment(comment);
            relation.setUpdateTime(LocalDateTime.now());
            guidanceService.updateById(relation);

            teacherDetail.setCurrentTeams(teacherDetail.getCurrentTeams() + 1);
            userService.updateTeacherDetail(teacherDetail);

            Team team = teamService.getById(relation.getTeamId());
            User teacher = userService.getById(teacherId);
            notificationService.sendGuidanceResultNotification(team.getLeaderId(), teacher.getRealName(), true, null, team.getId());

            log.info("教师 {} 接受了团队 {} 的指导申请", teacher.getRealName(), team.getTeamName());
            return ResultVO.success("已接受指导申请", null);

        } else { // 拒绝
            relation.setStatus(2);
            relation.setTeacherComment(comment);
            relation.setUpdateTime(LocalDateTime.now());
            guidanceService.updateById(relation);

            Team team = teamService.getById(relation.getTeamId());
            User teacher = userService.getById(teacherId);
            notificationService.sendGuidanceResultNotification(team.getLeaderId(), teacher.getRealName(), false, comment, team.getId());

            log.info("教师 {} 拒绝了团队 {} 的指导申请", teacher.getRealName(), team.getTeamName());
            return ResultVO.success("已拒绝指导申请", null);
        }
    }

    /**
     * 取消指导申请（学生）
     */
    @DeleteMapping("/cancel/{relationId}")
    public ResultVO<Void> cancelApplication(@PathVariable Long relationId,
                                            @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        GuidanceRelation relation = guidanceService.getById(relationId);

        if (relation == null) {
            return ResultVO.error("申请记录不存在");
        }

        // 获取团队信息
        Team team = teamService.getById(relation.getTeamId());
        if (team == null) {
            return ResultVO.error("团队不存在");
        }

        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            return ResultVO.error("只有队长可以取消申请");
        }

        // 只有待确认的申请可以取消
        if (relation.getStatus() != 0) {
            return ResultVO.error("该申请已处理，无法取消");
        }

        // 更新状态为已取消（状态码3表示已结束/已取消）
        relation.setStatus(3);
        relation.setUpdateTime(LocalDateTime.now());
        boolean updated = guidanceService.updateById(relation);

        if (updated) {
            log.info("用户 {} 取消指导申请 {}", userId, relationId);
            return ResultVO.success("已取消申请", null);
        }

        return ResultVO.error("取消申请失败");
    }

    /**
     * 获取我的指导团队（教师）
     */
    @GetMapping("/my-teams")
    public ResultVO<List<Map<String, Object>>> getMyGuidanceTeams(
            @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long teacherId = jwtUtil.getUserIdFromToken(token);

        LambdaQueryWrapper<GuidanceRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuidanceRelation::getTeacherId, teacherId)
                .eq(GuidanceRelation::getStatus, 1);

        List<GuidanceRelation> relations = guidanceService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (GuidanceRelation relation : relations) {
            Team team = teamService.getById(relation.getTeamId());
            if (team != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("teamId", team.getId());
                item.put("teamName", team.getTeamName());
                item.put("competitionId", team.getCompetitionId());

                Competition competition = competitionService.getById(team.getCompetitionId());
                item.put("competitionTitle", competition != null ? competition.getTitle() : "");

                User leader = userService.getById(team.getLeaderId());
                item.put("leaderName", leader != null ? leader.getRealName() : "");

                item.put("memberCount", team.getCurrentMembers());
                item.put("status", team.getStatus());

                result.add(item);
            }
        }

        return ResultVO.success(result);
    }

    /**
     * 获取我的指导申请（学生）
     */
    @GetMapping("/my-applications")
    public ResultVO<List<Map<String, Object>>> getMyGuidanceApplications(
            @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        // 获取用户所在的团队
        List<Team> userTeams = teamService.getTeamsByUserLeader(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Team team : userTeams) {
            LambdaQueryWrapper<GuidanceRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GuidanceRelation::getTeamId, team.getId());
            List<GuidanceRelation> relations = guidanceService.list(wrapper);

            for (GuidanceRelation relation : relations) {
                Map<String, Object> item = new HashMap<>();
                item.put("relationId", relation.getId());
                item.put("teamId", team.getId());
                item.put("teamName", team.getTeamName());

                User teacher = userService.getById(relation.getTeacherId());
                item.put("teacherName", teacher != null ? teacher.getRealName() : "");

                String statusText = "";
                if (relation.getStatus() == 0) statusText = "待确认";
                else if (relation.getStatus() == 1) statusText = "已接受";
                else if (relation.getStatus() == 2) statusText = "已拒绝";
                else if (relation.getStatus() == 3) statusText = "已取消";
                item.put("status", statusText);
                item.put("statusCode", relation.getStatus());
                item.put("applyReason", relation.getApplyReason());
                item.put("teacherComment", relation.getTeacherComment());
                item.put("createTime", relation.getCreateTime());

                result.add(item);
            }
        }

        return ResultVO.success(result);
    }



    /**
     * 获取教师待处理的指导申请
     */
    @GetMapping("/pending-applications")
    public ResultVO<List<Map<String, Object>>> getPendingApplications(
            @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long teacherId = jwtUtil.getUserIdFromToken(token);

        LambdaQueryWrapper<GuidanceRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuidanceRelation::getTeacherId, teacherId)
                .eq(GuidanceRelation::getStatus, 0)
                .orderByDesc(GuidanceRelation::getCreateTime);

        List<GuidanceRelation> relations = guidanceService.list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (GuidanceRelation relation : relations) {
            Team team = teamService.getById(relation.getTeamId());
            if (team != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("relationId", relation.getId());
                item.put("teamId", team.getId());
                item.put("teamName", team.getTeamName());
                item.put("applyReason", relation.getApplyReason());
                item.put("applyTime", relation.getCreateTime());

                Competition competition = competitionService.getById(team.getCompetitionId());
                item.put("competitionTitle", competition != null ? competition.getTitle() : "");

                User leader = userService.getById(team.getLeaderId());
                item.put("leaderName", leader != null ? leader.getRealName() : "");

                result.add(item);
            }
        }

        return ResultVO.success(result);
    }
}