package com.competition.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.competition.entity.Competition;
import com.competition.entity.CompetitionRegistration;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.service.CompetitionRegistrationService;
import com.competition.service.CompetitionService;
import com.competition.service.TeamService;
import com.competition.service.UserService;
import com.competition.utils.JwtUtil;
import com.competition.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/competition")
@CrossOrigin
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private CompetitionRegistrationService competitionRegistrationService;

    @Autowired
    private JwtUtil jwtUtil;

    // ==================== 竞赛基本操作 ====================

    /**
     * 获取竞赛列表
     */
    @GetMapping("/list")
    public ResultVO<Page<Competition>> getCompetitionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        Page<Competition> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Competition::getStatus, status);
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Competition::getCategory, category);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Competition::getTitle, keyword)
                    .or()
                    .like(Competition::getDescription, keyword);
        }

        wrapper.orderByDesc(Competition::getCreateTime);

        Page<Competition> result = competitionService.page(pageParam, wrapper);

        for (Competition c : result.getRecords()) {
            if (c.getPublishUserId() != null) {
                User user = userService.getById(c.getPublishUserId());
                if (user != null) {
                    c.setPublishUserName(user.getRealName());
                }
            }
        }

        return ResultVO.success(result);
    }

    /**
     * 获取竞赛详情
     */
    @GetMapping("/{id}")
    public ResultVO<Competition> getCompetitionDetail(@PathVariable Long id) {
        Competition competition = competitionService.getById(id);
        if (competition == null) {
            return ResultVO.error(400, "竞赛不存在");
        }

        if (competition.getPublishUserId() != null) {
            User user = userService.getById(competition.getPublishUserId());
            if (user != null) {
                competition.setPublishUserName(user.getRealName());
            }
        }

        return ResultVO.success(competition);
    }

    /**
     * 创建竞赛
     */
    @PostMapping("/create")
    public ResultVO<Void> createCompetition(@RequestBody Competition competition,
                                            @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);

        if (role != 0 && role != 1) {
            return ResultVO.error(403, "无权限创建竞赛");
        }

        competition.setPublishUserId(userId);
        competition.setStatus(0);
        competition.setCreateTime(LocalDateTime.now());

        boolean saved = competitionService.save(competition);
        if (saved) {
            return ResultVO.success("创建成功", null);
        }
        return ResultVO.error(400, "创建失败");
    }

    /**
     * 更新竞赛
     */
    @PutMapping("/update")
    public ResultVO<Void> updateCompetition(@RequestBody Competition competition,
                                            @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);

        Competition existing = competitionService.getById(competition.getId());
        if (existing == null) {
            return ResultVO.error(400, "竞赛不存在");
        }

        if (!existing.getPublishUserId().equals(userId) && role != 0) {
            return ResultVO.error(403, "无权限修改此竞赛");
        }

        competition.setUpdateTime(LocalDateTime.now());
        boolean updated = competitionService.updateById(competition);

        if (updated) {
            return ResultVO.success("更新成功", null);
        }
        return ResultVO.error(400, "更新失败");
    }

    /**
     * 删除竞赛
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteCompetition(@PathVariable Long id,
                                            @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Integer role = jwtUtil.getRoleFromToken(token);
        if (role != 0) {
            return ResultVO.error(403, "无权限删除竞赛");
        }

        boolean removed = competitionService.removeById(id);
        if (removed) {
            return ResultVO.success("删除成功", null);
        }
        return ResultVO.error(400, "删除失败");
    }

    /**
     * 获取竞赛统计信息
     */
    @GetMapping("/statistics")
    public ResultVO<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = competitionService.count();
        long registering = competitionService.count(new LambdaQueryWrapper<Competition>()
                .eq(Competition::getStatus, 1));
        long ongoing = competitionService.count(new LambdaQueryWrapper<Competition>()
                .eq(Competition::getStatus, 2));
        long ended = competitionService.count(new LambdaQueryWrapper<Competition>()
                .eq(Competition::getStatus, 3));

        stats.put("total", total);
        stats.put("registering", registering);
        stats.put("ongoing", ongoing);
        stats.put("ended", ended);

        return ResultVO.success(stats);
    }

    // ==================== 报名相关 ====================

    /**
     * 团队报名竞赛
     */
    @PostMapping("/register")
    public ResultVO<Void> registerCompetition(@RequestBody Map<String, Object> params,
                                              @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (params.get("competitionId") == null || params.get("teamId") == null) {
            return ResultVO.error(400, "参数不能为空：competitionId、teamId");
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            Long competitionId = Long.valueOf(params.get("competitionId").toString());
            Long teamId = Long.valueOf(params.get("teamId").toString());

            Team team = teamService.getById(teamId);
            if (team == null) {
                return ResultVO.error(400, "团队不存在");
            }

            if (!team.getLeaderId().equals(userId)) {
                return ResultVO.error(403, "只有队长可以报名竞赛");
            }

            Competition competition = competitionService.getById(competitionId);
            if (competition == null) {
                return ResultVO.error(400, "竞赛不存在");
            }

            LocalDateTime now = LocalDateTime.now();
            if (competition.getStartTime() != null && now.isBefore(competition.getStartTime())) {
                return ResultVO.error(400, "报名尚未开始");
            }
            if (competition.getEndTime() != null && now.isAfter(competition.getEndTime())) {
                return ResultVO.error(400, "报名已结束");
            }

            // 重复报名判断
            LambdaQueryWrapper<CompetitionRegistration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CompetitionRegistration::getCompetitionId, competitionId)
                    .eq(CompetitionRegistration::getTeamId, teamId);
            boolean exists = competitionRegistrationService.count(wrapper) > 0;
            if (exists) {
                // ✅ 关键修复：返回 code=400，不是500！
                return ResultVO.error(400, "该团队已报名此竞赛，请勿重复报名");
            }

            CompetitionRegistration registration = new CompetitionRegistration();
            registration.setCompetitionId(competitionId);
            registration.setTeamId(teamId);
            registration.setStatus(0);
            registration.setSubmitTime(LocalDateTime.now());
            boolean saved = competitionRegistrationService.save(registration);

            if (saved) {
                return ResultVO.success("报名成功，等待审核", null);
            } else {
                return ResultVO.error(400, "报名失败，请稍后重试");
            }

        } catch (NumberFormatException e) {
            log.error("参数格式错误", e);
            return ResultVO.error(400, "请求参数错误");
        } catch (Exception e) {
            log.error("报名服务异常", e);
            return ResultVO.error(500, "报名服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 获取团队报名状态
     */
    @GetMapping("/{competitionId}/team/{teamId}/registration-status")
    public ResultVO<Map<String, Object>> getTeamRegistrationStatus(
            @PathVariable Long competitionId,
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(token);

            Team team = teamService.getById(teamId);
            if (team == null || !team.getLeaderId().equals(userId)) {
                return ResultVO.error(403, "无权限查看该团队报名状态");
            }

            LambdaQueryWrapper<CompetitionRegistration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CompetitionRegistration::getCompetitionId, competitionId)
                    .eq(CompetitionRegistration::getTeamId, teamId);

            CompetitionRegistration registration = competitionRegistrationService.getOne(wrapper);

            Map<String, Object> result = new HashMap<>();
            if (registration != null) {
                result.put("status", registration.getStatus());
                result.put("statusText", getRegistrationStatusText(registration.getStatus()));
                result.put("submitTime", registration.getSubmitTime());
                result.put("isRegistered", true);
            } else {
                result.put("status", null);
                result.put("statusText", "未报名");
                result.put("submitTime", null);
                result.put("isRegistered", false);
            }

            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("获取报名状态失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("isRegistered", false);
            result.put("status", null);
            result.put("statusText", "未报名");
            result.put("submitTime", null);
            return ResultVO.success(result);
        }
    }

    /**
     * 获取报名状态文本
     */
    private String getRegistrationStatusText(Integer status) {
        if (status == null) return "未报名";
        switch (status) {
            case 0: return "报名待审核";
            case 1: return "报名已通过";
            case 2: return "报名已拒绝";
            case 3: return "报名已取消";
            default: return "报名状态未知";
        }
    }
}