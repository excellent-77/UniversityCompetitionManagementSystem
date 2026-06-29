package com.competition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.entity.*;
import com.competition.mapper.*;
import com.competition.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private StudentDetailMapper studentDetailMapper;

    @Autowired
    private TeacherDetailMapper teacherDetailMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private CompetitionMapper competitionMapper;



    // ==================== 密码相关 ====================

    /**
     * 密码加密
     */
    public String encodePassword(String rawPassword) {
        return passwordUtil.encode(rawPassword);
    }

    /**
     * 密码验证
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordUtil.matches(rawPassword, encodedPassword);
    }

    // ==================== 学生相关 ====================

    /**
     * 获取学生详细信息
     */
    public StudentDetail getStudentDetail(Long userId) {
        LambdaQueryWrapper<StudentDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentDetail::getUserId, userId);
        return studentDetailMapper.selectOne(wrapper);
    }

    /**
     * 保存学生详细信息
     */
    public void saveStudentDetail(StudentDetail detail) {
        studentDetailMapper.insert(detail);
    }

    /**
     * 更新学生详细信息
     */
    public void updateStudentDetail(StudentDetail detail) {
        studentDetailMapper.updateById(detail);
    }

    /**
     * 获取所有学生
     */
    public List<User> getStudents() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 2)
                .eq(User::getStatus, 1);
        return this.list(wrapper);
    }

    /**
     * 获取所有学生（包含详细信息）
     */
    public List<Map<String, Object>> getStudentsWithDetail() {
        List<User> students = getStudents();
        return students.stream().map(student -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", student.getId());
            map.put("username", student.getUsername());
            map.put("realName", student.getRealName());
            map.put("phone", student.getPhone());
            map.put("email", student.getEmail());

            StudentDetail detail = getStudentDetail(student.getId());
            if (detail != null) {
                map.put("studentNo", detail.getStudentNo());
                map.put("major", detail.getMajor());
                map.put("grade", detail.getGrade());
                map.put("className", detail.getClassName());
                map.put("skills", detail.getSkills());
            }
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 根据专业获取学生
     */
    public List<User> getStudentsByMajor(String major) {
        LambdaQueryWrapper<StudentDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(StudentDetail::getMajor, major);
        List<StudentDetail> details = studentDetailMapper.selectList(detailWrapper);

        if (details.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = details.stream()
                .map(StudentDetail::getUserId)
                .toList();

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, userIds)
                .eq(User::getRole, 2)
                .eq(User::getStatus, 1);
        return this.list(userWrapper);
    }

    // ==================== 教师相关 ====================

    /**
     * 获取教师详细信息
     */
    public TeacherDetail getTeacherDetail(Long userId) {
        LambdaQueryWrapper<TeacherDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherDetail::getUserId, userId);
        return teacherDetailMapper.selectOne(wrapper);
    }

    /**
     * 保存教师详细信息
     */
    public void saveTeacherDetail(TeacherDetail detail) {
        teacherDetailMapper.insert(detail);
    }

    /**
     * 更新教师详细信息
     */
    public void updateTeacherDetail(TeacherDetail detail) {
        teacherDetailMapper.updateById(detail);
    }

    /**
     * 获取所有教师
     */
    public List<User> getTeachers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 1)
                .eq(User::getStatus, 1);
        return this.list(wrapper);
    }

    /**
     * 获取所有教师（包含详细信息）
     */
    public List<Map<String, Object>> getTeachersWithDetail() {
        List<User> teachers = getTeachers();
        return teachers.stream().map(teacher -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", teacher.getId());
            map.put("username", teacher.getUsername());
            map.put("realName", teacher.getRealName());
            map.put("phone", teacher.getPhone());
            map.put("email", teacher.getEmail());

            TeacherDetail detail = getTeacherDetail(teacher.getId());
            if (detail != null) {
                map.put("teacherNo", detail.getTeacherNo());
                map.put("department", detail.getDepartment());
                map.put("title", detail.getTitle());
                map.put("researchDirection", detail.getResearchDirection());
                map.put("maxTeams", detail.getMaxTeams());
                map.put("currentTeams", detail.getCurrentTeams());
                map.put("isAvailable", detail.getIsAvailable());
            }
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取可指导的教师
     */
    public List<User> getAvailableTeachers() {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, 1)
                .eq(User::getStatus, 1);
        List<User> teachers = this.list(userWrapper);

        teachers.removeIf(teacher -> {
            TeacherDetail detail = getTeacherDetail(teacher.getId());
            return detail == null || detail.getIsAvailable() != 1 ||
                    detail.getCurrentTeams() >= detail.getMaxTeams();
        });

        return teachers;
    }

    /**
     * 根据研究方向获取教师
     */
    public List<User> getTeachersByDirection(String direction) {
        LambdaQueryWrapper<TeacherDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.like(TeacherDetail::getResearchDirection, direction);
        List<TeacherDetail> details = teacherDetailMapper.selectList(detailWrapper);

        if (details.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = details.stream()
                .map(TeacherDetail::getUserId)
                .toList();

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, userIds)
                .eq(User::getRole, 1)
                .eq(User::getStatus, 1);
        return this.list(userWrapper);
    }

    // ==================== 统计相关 ====================

    /**
     * 获取用户总数
     */
    public long getUserCount() {
        return this.count();
    }

    /**
     * 获取学生数量
     */
    public long getStudentCount() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 2);
        return this.count(wrapper);
    }

    /**
     * 获取教师数量
     */
    public long getTeacherCount() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 1);
        return this.count(wrapper);
    }

    /**
     * 获取管理员数量
     */
    public long getAdminCount() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 0);
        return this.count(wrapper);
    }

    /**
     * 获取禁用用户数量
     */
    public long getDisabledUserCount() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 0);
        return this.count(wrapper);
    }

    /**
     * 获取今日新增用户数
     */
    public long getTodayNewUserCount() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getCreateTime, todayStart);
        return this.count(wrapper);
    }

    /**
     * 获取本周新增用户数
     */
    public long getWeekNewUserCount() {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getCreateTime, weekStart);
        return this.count(wrapper);
    }

    /**
     * 获取团队总数
     */
    public long getTeamCount() {
        return teamMapper.selectCount(null);
    }

    /**
     * 获取活跃团队数（招募中的团队）
     */
    public long getActiveTeamCount() {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getStatus, 0);
        return teamMapper.selectCount(wrapper);
    }

    /**
     * 获取今日新增团队数
     */
    public long getTodayNewTeamCount() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Team::getCreateTime, todayStart);
        return teamMapper.selectCount(wrapper);
    }

    /**
     * 获取竞赛总数
     */
    public long getCompetitionCount() {
        return competitionMapper.selectCount(null);
    }

    /**
     * 获取进行中的竞赛数
     */
    public long getOngoingCompetitionCount() {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 2);
        return competitionMapper.selectCount(wrapper);
    }

    /**
     * 获取报名中的竞赛数
     */
    public long getRegisteringCompetitionCount() {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1);
        return competitionMapper.selectCount(wrapper);
    }

    /**
     * 获取已结束的竞赛数
     */
    public long getEndedCompetitionCount() {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 3);
        return competitionMapper.selectCount(wrapper);
    }

    /**
     * 获取系统统计信息
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();

        // 用户统计
        stats.put("totalUsers", getUserCount());
        stats.put("studentCount", getStudentCount());
        stats.put("teacherCount", getTeacherCount());
        stats.put("adminCount", getAdminCount());
        stats.put("disabledCount", getDisabledUserCount());
        stats.put("todayNewUsers", getTodayNewUserCount());
        stats.put("weekNewUsers", getWeekNewUserCount());

        // 团队统计
        stats.put("teamCount", getTeamCount());
        stats.put("activeTeamCount", getActiveTeamCount());
        stats.put("todayNewTeams", getTodayNewTeamCount());

        // 竞赛统计
        stats.put("totalCompetitions", getCompetitionCount());
        stats.put("ongoingCompetitions", getOngoingCompetitionCount());
        stats.put("registeringCompetitions", getRegisteringCompetitionCount());
        stats.put("endedCompetitions", getEndedCompetitionCount());

        return stats;
    }

    // ==================== 个人信息更新 ====================

    /**
     * 更新用户个人信息
     */
    @Transactional
    public void updateUserProfile(Long userId, Map<String, Object> params) {
        User user = this.getById(userId);
        if (user == null) return;

        // 更新基本信息
        if (params.containsKey("realName")) {
            user.setRealName((String) params.get("realName"));
        }
        if (params.containsKey("phone")) {
            user.setPhone((String) params.get("phone"));
        }
        if (params.containsKey("email")) {
            user.setEmail((String) params.get("email"));
        }

        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);

        // 更新详细信息
        if (user.getRole() == 2) {
            StudentDetail detail = getStudentDetail(userId);
            if (detail != null) {
                if (params.containsKey("skills")) {
                    detail.setSkills((String) params.get("skills"));
                }
                if (params.containsKey("interests")) {
                    detail.setInterests((String) params.get("interests"));
                }
                if (params.containsKey("honors")) {
                    detail.setHonors((String) params.get("honors"));
                }
                studentDetailMapper.updateById(detail);
            }
        } else if (user.getRole() == 1) {
            TeacherDetail detail = getTeacherDetail(userId);
            if (detail != null) {
                if (params.containsKey("researchDirection")) {
                    detail.setResearchDirection((String) params.get("researchDirection"));
                }
                if (params.containsKey("maxTeams")) {
                    detail.setMaxTeams((Integer) params.get("maxTeams"));
                }
                if (params.containsKey("isAvailable")) {
                    detail.setIsAvailable((Integer) params.get("isAvailable"));
                }
                teacherDetailMapper.updateById(detail);
            }
        }
    }

    /**
     * 修改密码
     */
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }

        if (!verifyPassword(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(encodePassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    // ==================== 管理员操作 ====================

    /**
     * 禁用/启用用户
     */
    @Transactional
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    /**
     * 重置用户密码
     */
    @Transactional
    public boolean resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }

        user.setPassword(encodePassword("123456"));
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    /**
     * 批量删除用户
     */
    @Transactional
    public boolean batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        // 删除用户信息
        for (Long userId : userIds) {
            // 删除学生/教师详细信息
            StudentDetail studentDetail = getStudentDetail(userId);
            if (studentDetail != null) {
                studentDetailMapper.deleteById(studentDetail.getId());
            }

            TeacherDetail teacherDetail = getTeacherDetail(userId);
            if (teacherDetail != null) {
                teacherDetailMapper.deleteById(teacherDetail.getId());
            }

            // 删除用户
            this.removeById(userId);
        }

        return true;
    }

    /**
     * 根据ID获取用户完整信息（包含详情）
     */
    public Map<String, Object> getUserFullInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            return null;
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("status", user.getStatus());
        result.put("createTime", user.getCreateTime());

        if (user.getRole() == 2) {
            StudentDetail detail = getStudentDetail(userId);
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
            TeacherDetail detail = getTeacherDetail(userId);
            if (detail != null) {
                result.put("teacherNo", detail.getTeacherNo());
                result.put("department", detail.getDepartment());
                result.put("title", detail.getTitle());
                result.put("researchDirection", detail.getResearchDirection());
                result.put("maxTeams", detail.getMaxTeams());
                result.put("currentTeams", detail.getCurrentTeams());
                result.put("isAvailable", detail.getIsAvailable());
            }
        }

        return result;
    }

    // ==================== 搜索功能 ====================

    /**
     * 根据用户名或真实姓名搜索用户
     */
    public List<User> searchUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getRealName, keyword)
                .orderByDesc(User::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 根据角色获取用户列表
     */
    public List<User> getUsersByRole(Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, role)
                .orderByDesc(User::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 分页查询用户
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> getUserPage(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page,
            String keyword,
            Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword);
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        wrapper.orderByDesc(User::getCreateTime);

        return this.page(page, wrapper);
    }
}