package com.competition.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.competition.dto.LoginDTO;
import com.competition.dto.RegisterDTO;
import com.competition.entity.*;
import com.competition.service.CompetitionService;
import com.competition.service.TeamService;
import com.competition.service.UserService;
import com.competition.utils.JwtUtil;
import com.competition.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.competition.service.TeamService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户登录、注册、个人信息管理等功能
 */
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TeamService teamService;

    @Autowired
    private CompetitionService competitionService;

    // ==================== 基础功能 ====================

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResultVO<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());

        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, loginDTO.getUsername());
            User user = userService.getOne(wrapper);

            if (user == null) {
                log.warn("用户不存在: {}", loginDTO.getUsername());
                return ResultVO.error("用户名或密码错误");
            }

            if (!userService.verifyPassword(loginDTO.getPassword(), user.getPassword())) {
                log.warn("密码错误: {}", loginDTO.getUsername());
                return ResultVO.error("用户名或密码错误");
            }

            if (user.getStatus() != 1) {
                return ResultVO.error("账号已被禁用");
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());


            log.info("用户 {} 登录成功", user.getUsername());
            return ResultVO.success(result);

        } catch (Exception e) {
            log.error("登录失败", e);
            return ResultVO.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public ResultVO<Map<String, Object>> refreshToken(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refreshToken");
        if (StrUtil.isBlank(refreshToken)) {
            return ResultVO.error("refresh token 不能为空");
        }

        try {
            io.jsonwebtoken.Claims claims = jwtUtil.parseToken(refreshToken);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            Integer role = claims.get("role", Integer.class);

            String newToken = jwtUtil.generateToken(userId, username, role);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username, role);

            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("refreshToken", newRefreshToken);

            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("刷新 token 失败", e);
            return ResultVO.error("refresh token 无效或已过期");
        }
    }

    /**
     * 学生注册
     */
    @PostMapping("/register/student")
    public ResultVO<Void> registerStudent(@RequestBody RegisterDTO registerDTO) {
        log.info("学生注册: {}", registerDTO.getUsername());

        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userService.count(wrapper) > 0) {
            return ResultVO.error("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(userService.encodePassword(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(2); // 学生角色
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);

        // 创建学生详细信息
        StudentDetail studentDetail = new StudentDetail();
        studentDetail.setUserId(user.getId());
        studentDetail.setStudentNo(registerDTO.getStudentNo());
        studentDetail.setMajor(registerDTO.getMajor());
        studentDetail.setGrade(registerDTO.getGrade());
        studentDetail.setClassName(registerDTO.getClassName());
        studentDetail.setSkills(registerDTO.getSkills());
        studentDetail.setInterests(registerDTO.getInterests());
        studentDetail.setHonors(registerDTO.getHonors());
        userService.saveStudentDetail(studentDetail);

        log.info("学生注册成功: {} (学号: {})", registerDTO.getUsername(), registerDTO.getStudentNo());
        return ResultVO.success("注册成功", null);
    }

    /**
     * 教师注册
     */
    @PostMapping("/register/teacher")
    public ResultVO<Void> registerTeacher(@RequestBody RegisterDTO registerDTO) {
        log.info("教师注册: {}", registerDTO.getUsername());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userService.count(wrapper) > 0) {
            return ResultVO.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(userService.encodePassword(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(1); // 教师角色
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);

        TeacherDetail teacherDetail = new TeacherDetail();
        teacherDetail.setUserId(user.getId());
        teacherDetail.setTeacherNo(registerDTO.getTeacherNo());
        teacherDetail.setDepartment(registerDTO.getDepartment());
        teacherDetail.setTitle(registerDTO.getTitle());
        teacherDetail.setResearchDirection(registerDTO.getResearchDirection());
        teacherDetail.setMaxTeams(5);
        teacherDetail.setCurrentTeams(0);
        teacherDetail.setIsAvailable(1);
        userService.saveTeacherDetail(teacherDetail);

        log.info("教师注册成功: {} (工号: {})", registerDTO.getUsername(), registerDTO.getTeacherNo());
        return ResultVO.success("注册成功", null);
    }

    /**
     * 管理员注册（仅超级管理员可用）
     */
    @PostMapping("/register/admin")
    public ResultVO<Void> registerAdmin(@RequestBody RegisterDTO registerDTO,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        // 验证权限（非强制，首次注册时可能没有token）
        if (StrUtil.isNotBlank(token)) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer currentRole = jwtUtil.getRoleFromToken(token);
            if (currentRole != null && currentRole != 0) {
                return ResultVO.error("无权限操作");
            }
        }

        log.info("管理员注册: {}", registerDTO.getUsername());

        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userService.count(wrapper) > 0) {
            return ResultVO.error("用户名已存在");
        }

        // 创建管理员用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(userService.encodePassword(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(0); // 管理员角色
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);

        log.info("管理员注册成功: {}", registerDTO.getUsername());
        return ResultVO.success("添加成功", null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ResultVO<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        if (StrUtil.isBlank(token)) {
            return ResultVO.error("未登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getById(userId);
        if (user == null) {
            return ResultVO.error("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());

        if (user.getRole() == 2) {
            StudentDetail studentDetail = userService.getStudentDetail(userId);
            if (studentDetail != null) {
                result.put("studentNo", studentDetail.getStudentNo());
                result.put("major", studentDetail.getMajor());
                result.put("grade", studentDetail.getGrade());
                result.put("className", studentDetail.getClassName());
                result.put("skills", studentDetail.getSkills());
                result.put("interests", studentDetail.getInterests());
                result.put("honors", studentDetail.getHonors());
            }
        } else if (user.getRole() == 1) {
            TeacherDetail teacherDetail = userService.getTeacherDetail(userId);
            if (teacherDetail != null) {
                result.put("teacherNo", teacherDetail.getTeacherNo());
                result.put("department", teacherDetail.getDepartment());
                result.put("title", teacherDetail.getTitle());
                result.put("researchDirection", teacherDetail.getResearchDirection());
                result.put("maxTeams", teacherDetail.getMaxTeams());
                result.put("currentTeams", teacherDetail.getCurrentTeams());
                result.put("isAvailable", teacherDetail.getIsAvailable());
            }
        }

        return ResultVO.success(result);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public ResultVO<Void> updateProfile(@RequestBody Map<String, Object> params,
                                        @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);

        userService.updateUserProfile(userId, params);
        return ResultVO.success("更新成功", null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public ResultVO<Void> changePassword(@RequestBody Map<String, String> params,
                                         @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (StrUtil.isBlank(oldPassword) || StrUtil.isBlank(newPassword)) {
            return ResultVO.error("原密码和新密码不能为空");
        }

        User user = userService.getById(userId);
        if (user == null || !userService.verifyPassword(oldPassword, user.getPassword())) {
            return ResultVO.error("原密码错误");
        }

        user.setPassword(userService.encodePassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);

        log.info("用户 {} 修改密码成功", user.getUsername());
        return ResultVO.success("密码修改成功", null);
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取用户列表（管理员）
     */
    @GetMapping("/admin/list")
    public ResultVO<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestHeader("Authorization") String token) {

        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限访问");
        }

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword);
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userService.page(pageParam, wrapper);
        return ResultVO.success(result);
    }

    /**
     * 获取用户详情（管理员）
     */
    @GetMapping("/admin/detail/{id}")
    public ResultVO<Map<String, Object>> getUserDetail(@PathVariable Long id,
                                                       @RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限访问");
        }

        User user = userService.getById(id);
        if (user == null) {
            return ResultVO.error("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("status", user.getStatus());
        result.put("createTime", user.getCreateTime());

        if (user.getRole() == 2) {
            StudentDetail studentDetail = userService.getStudentDetail(id);
            if (studentDetail != null) {
                result.put("studentNo", studentDetail.getStudentNo());
                result.put("major", studentDetail.getMajor());
                result.put("grade", studentDetail.getGrade());
                result.put("className", studentDetail.getClassName());
                result.put("skills", studentDetail.getSkills());
            }
        } else if (user.getRole() == 1) {
            TeacherDetail teacherDetail = userService.getTeacherDetail(id);
            if (teacherDetail != null) {
                result.put("teacherNo", teacherDetail.getTeacherNo());
                result.put("department", teacherDetail.getDepartment());
                result.put("title", teacherDetail.getTitle());
                result.put("researchDirection", teacherDetail.getResearchDirection());
            }
        }

        return ResultVO.success(result);
    }

    /**
     * 更新用户信息（管理员）
     */
    @PutMapping("/admin/update")
    public ResultVO<Void> updateUser(@RequestBody User user,
                                     @RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        user.setUpdateTime(LocalDateTime.now());
        boolean updated = userService.updateById(user);
        if (updated) {
            log.info("管理员更新用户: {}", user.getId());
            return ResultVO.success("更新成功", null);
        }
        return ResultVO.error("更新失败");
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/admin/delete/{id}")
    public ResultVO<Void> deleteUser(@PathVariable Long id,
                                     @RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        // 不能删除自己
        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        if (currentUserId.equals(id)) {
            return ResultVO.error("不能删除当前登录账号");
        }

        boolean removed = userService.removeById(id);
        if (removed) {
            log.info("管理员删除用户: {}", id);
            return ResultVO.success("删除成功", null);
        }
        return ResultVO.error("删除失败");
    }

    /**
     * 重置密码（管理员）
     */
    @PutMapping("/admin/reset-password/{id}")
    public ResultVO<Void> resetPassword(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(userService.encodePassword("123456"));
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);
            log.info("管理员重置用户 {} 的密码", user.getUsername());
            return ResultVO.success("密码已重置为 123456", null);
        }
        return ResultVO.error("用户不存在");
    }

    /**
     * 更新用户状态（管理员）
     */
    @PutMapping("/admin/update-status")
    public ResultVO<Void> updateUserStatus(@RequestBody Map<String, Object> params,
                                           @RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        // 不能禁用自己
        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        if (currentUserId.equals(id) && status == 0) {
            return ResultVO.error("不能禁用当前登录账号");
        }

        User user = userService.getById(id);
        if (user != null) {
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);
            log.info("管理员更新用户 {} 状态为: {}", user.getUsername(), status == 1 ? "启用" : "禁用");
            return ResultVO.success("状态更新成功", null);
        }
        return ResultVO.error("用户不存在");
    }

    /**
     * 批量删除用户（管理员）
     */
    @DeleteMapping("/admin/batch-delete")
    public ResultVO<Void> batchDeleteUsers(@RequestBody Map<String, Object> params,
                                           @RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限操作");
        }

        @SuppressWarnings("unchecked")
        java.util.List<Long> ids = (java.util.List<Long>) params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResultVO.error("请选择要删除的用户");
        }

        // 不能删除自己
        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        ids.remove(currentUserId);

        boolean removed = userService.removeByIds(ids);
        if (removed) {
            log.info("管理员批量删除用户: {}", ids);
            return ResultVO.success("批量删除成功", null);
        }
        return ResultVO.error("批量删除失败");
    }

    /**
     * 获取用户统计信息（管理员）
     */
    @GetMapping("/admin/statistics")
    public ResultVO<Map<String, Object>> getUserStatistics(@RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限访问");
        }

        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        long totalUsers = userService.count();
        // 学生数量
        long studentCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, 2));
        // 教师数量
        long teacherCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, 1));
        // 管理员数量
        long adminCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, 0));
        // 禁用用户数量
        long disabledCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 0));
        // 今日新增用户数量
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewCount = userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart));

        stats.put("totalUsers", totalUsers);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("adminCount", adminCount);
        stats.put("disabledCount", disabledCount);
        stats.put("todayNewCount", todayNewCount);

        return ResultVO.success(stats);
    }

    /**
     * 获取系统统计信息（管理员）
     */
    @GetMapping("/admin/system-statistics")
    public ResultVO<Map<String, Object>> getSystemStatistics(@RequestHeader("Authorization") String token) {
        // 验证管理员权限
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer currentRole = jwtUtil.getRoleFromToken(token);
        if (currentRole == null || currentRole != 0) {
            return ResultVO.error("无权限访问");
        }

        Map<String, Object> stats = new HashMap<>();

        // 用户统计 - 从数据库实时查询
        long totalUsers = userService.count();
        long studentCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, 2));
        long teacherCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, 1));
        long adminCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, 0));

        // 团队统计
        long teamCount = teamService.count();
        long activeTeamCount = teamService.count(new LambdaQueryWrapper<Team>().eq(Team::getStatus, 0));

        // 今日新增
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewUsers = userService.count(new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart));
        long todayNewTeams = teamService.count(new LambdaQueryWrapper<Team>().ge(Team::getCreateTime, todayStart));

        // 竞赛统计
        long ongoingCompetitions = competitionService.count(
                new LambdaQueryWrapper<Competition>().eq(Competition::getStatus, 2)
        );

        stats.put("totalUsers", totalUsers);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("adminCount", adminCount);
        stats.put("teamCount", teamCount);
        stats.put("activeTeamCount", activeTeamCount);
        stats.put("todayNewUsers", todayNewUsers);
        stats.put("todayNewTeams", todayNewTeams);
        stats.put("ongoingCompetitions", ongoingCompetitions);

        log.info("系统统计: 总用户={}, 学生={}, 教师={}, 今日新增={}",
                totalUsers, studentCount, teacherCount, todayNewUsers);

        return ResultVO.success(stats);
    }

    /**
     * 获取用户公开信息（用于查看队友信息）
     */
    @GetMapping("/detail/{userId}")
    public ResultVO<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResultVO.error("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());

        // 添加调试日志
        log.info("返回用户详情 - phone: {}, email: {}", user.getPhone(), user.getEmail());

        if (user.getRole() == 2) {
            StudentDetail detail = userService.getStudentDetail(userId);
            if (detail != null) {
                result.put("studentNo", detail.getStudentNo());
                result.put("major", detail.getMajor());
                result.put("grade", detail.getGrade());
                result.put("className", detail.getClassName());
                result.put("skills", detail.getSkills());
                result.put("interests", detail.getInterests());
                result.put("honors", detail.getHonors());
            }
        } else if (user.getRole() == 1) {
            TeacherDetail detail = userService.getTeacherDetail(userId);
            if (detail != null) {
                result.put("teacherNo", detail.getTeacherNo());
                result.put("department", detail.getDepartment());
                result.put("title", detail.getTitle());
                result.put("researchDirection", detail.getResearchDirection());
            }
        }

        return ResultVO.success(result);
    }
}