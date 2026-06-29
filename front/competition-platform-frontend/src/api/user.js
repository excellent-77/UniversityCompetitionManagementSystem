import request from './request'

export const userApi = {
  // ==================== 基础功能 ====================
  
  /**
   * 用户登录
   * @param {Object} data - { username, password }
   */
  login(data) {
    return request.post('/user/login', data)
  },
  
  /**
   * 学生注册
   * @param {Object} data - 学生注册信息
   */
  registerStudent(data) {
    return request.post('/user/register/student', data)
  },
  
  /**
   * 教师注册
   * @param {Object} data - 教师注册信息
   */
  registerTeacher(data) {
    return request.post('/user/register/teacher', data)
  },
  
  /**
   * 获取当前用户信息
   */
  getUserInfo() {
    return request.get('/user/info')
  },
  
  /**
   * 更新个人信息
   * @param {Object} data - 要更新的字段
   */
  updateProfile(data) {
    return request.put('/user/profile', data)
  },
  
  // ==================== 管理员功能 ====================
  
  /**
   * 管理员注册（仅超级管理员可用）
   * @param {Object} data - 管理员注册信息
   */
  registerAdmin(data) {
    return request.post('/user/register/admin', data)
  },
  
  /**
   * 获取用户列表（管理员）
   * @param {Object} params - { page, size, keyword, role }
   */
  getUserList(params) {
    return request.get('/user/admin/list', { params })
  },
  
  /**
   * 更新用户信息（管理员）
   * @param {Object} data - 用户信息
   */
  updateUser(data) {
    return request.put('/user/admin/update', data)
  },
  
  /**
   * 删除用户（管理员）
   * @param {Number} id - 用户ID
   */
  deleteUser(id) {
    return request.delete(`/user/admin/delete/${id}`)
  },
  
  /**
   * 重置密码（管理员）
   * @param {Number} id - 用户ID
   */
  resetPassword(id) {
    return request.put(`/user/admin/reset-password/${id}`)
  },
  
  /**
   * 更新用户状态（管理员）
   * @param {Number} id - 用户ID
   * @param {Number} status - 状态: 0-禁用, 1-启用
   */
  updateUserStatus(id, status) {
    return request.put('/user/admin/update-status', { id, status })
  },
  
  /**
   * 批量删除用户（管理员）
   * @param {Array} ids - 用户ID数组
   */
  batchDeleteUsers(ids) {
    return request.delete('/user/admin/batch-delete', { data: { ids } })
  },
  
  /**
   * 获取用户统计信息（管理员）
   */
  getUserStatistics() {
    return request.get('/user/admin/statistics')
  },
  
  /**
   * 获取系统统计信息（管理员）
   */
  getSystemStatistics() {
    return request.get('/user/admin/system-statistics')
  },
  
  // ==================== 扩展功能 ====================
  
  /**
   * 修改密码
   * @param {Object} data - { oldPassword, newPassword }
   */
  changePassword(data) {
    return request.put('/user/change-password', data)
  },
  
  /**
   * 获取用户通知列表
   * @param {Object} params - { page, size, isRead }
   */
  getNotifications(params) {
    return request.get('/user/notifications', { params })
  },
  
  /**
   * 标记通知已读
   * @param {Number} id - 通知ID
   */
  markNotificationRead(id) {
    return request.put(`/user/notifications/${id}/read`)
  },
  
  /**
   * 标记所有通知已读
   */
  markAllNotificationsRead() {
    return request.put('/user/notifications/read-all')
  },
  
  // ==================== 教师专用功能 ====================
  
  /**
   * 获取教师指导的团队列表
   */
  getMyGuidanceTeams() {
    return request.get('/user/teacher/guidance-teams')
  },
  
  /**
   * 更新教师指导状态
   * @param {Object} data - { isAvailable, maxTeams }
   */
  updateTeacherStatus(data) {
    return request.put('/user/teacher/status', data)
  },
  
  // ==================== 学生专用功能 ====================
  
  /**
   * 获取学生参与的团队列表
   */
  getMyTeams() {
    return request.get('/user/student/my-teams')
  },
  
  /**
   * 更新学生技能档案
   * @param {Object} data - { skills, interests, honors }
   */
  updateStudentSkills(data) {
    return request.put('/user/student/skills', data)
  },
  
  /**
   * 获取推荐队友
   * @param {Number} competitionId - 竞赛ID
   */
  getRecommendedTeammates(competitionId) {
    return request.get(`/user/student/recommend/${competitionId}`)
  },

  /**
 * 获取用户详细信息（公开信息）
 * @param {Number} userId - 用户ID
 */
  getUserDetail(userId) {
    return request.get(`/user/detail/${userId}`)
  }
}