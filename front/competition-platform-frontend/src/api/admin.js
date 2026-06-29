import request from './request'

export const adminApi = {
  // ==================== 用户管理 ====================
  
  /**
   * 获取所有用户
   */
  getUsers(params) {
    return request.get('/user/admin/list', { params })
  },
  
  /**
   * 创建用户
   */
  createUser(data) {
    const { role, ...userData } = data
    if (role === 0) {
      return request.post('/user/register/admin', userData)
    } else if (role === 1) {
      return request.post('/user/register/teacher', userData)
    } else {
      return request.post('/user/register/student', userData)
    }
  },
  
  /**
   * 更新用户
   */
  updateUser(data) {
    return request.put('/user/admin/update', data)
  },
  
  /**
   * 删除用户
   */
  deleteUser(id) {
    return request.delete(`/user/admin/delete/${id}`)
  },
  
  /**
   * 批量删除用户
   */
  batchDeleteUsers(ids) {
    return request.delete('/user/admin/batch-delete', { data: { ids } })
  },
  
  /**
   * 重置密码
   */
  resetPassword(id) {
    return request.put(`/user/admin/reset-password/${id}`)
  },
  
  /**
   * 更新用户状态
   */
  updateUserStatus(id, status) {
    return request.put('/user/admin/update-status', { id, status })
  },
  
  /**
   * 获取用户统计
   */
  getUserStatistics() {
    return request.get('/user/admin/statistics')
  },
  
  // ==================== 竞赛管理 ====================
  
  /**
   * 获取所有竞赛
   */
  getCompetitions(params) {
    return request.get('/competition/admin/list', { params })
  },
  
  /**
   * 创建竞赛
   */
  createCompetition(data) {
    return request.post('/competition/create', data)
  },
  
  /**
   * 更新竞赛
   */
  updateCompetition(data) {
    return request.put('/competition/update', data)
  },
  
  /**
   * 删除竞赛
   */
  deleteCompetition(id) {
    return request.delete(`/competition/${id}`)
  },
  
  /**
   * 审核竞赛报名
   */
  auditRegistration(id, status, comment) {
    return request.put('/competition/audit', { id, status, comment })
  },
  
  // ==================== 系统管理 ====================
  
  /**
   * 获取系统公告
   */
  getAnnouncements(params) {
    return request.get('/announcement/list', { params })
  },
  
  /**
   * 创建公告
   */
  createAnnouncement(data) {
    return request.post('/announcement/create', data)
  },
  
  /**
   * 更新公告
   */
  updateAnnouncement(data) {
    return request.put('/announcement/update', data)
  },
  
  /**
   * 删除公告
   */
  deleteAnnouncement(id) {
    return request.delete(`/announcement/${id}`)
  },
  
  /**
   * 获取操作日志
   */
  getOperationLogs(params) {
    return request.get('/logs/operations', { params })
  },
  
  /**
   * 获取系统统计
   */
  getSystemStatistics() {
    return request.get('/admin/statistics')
  }
}