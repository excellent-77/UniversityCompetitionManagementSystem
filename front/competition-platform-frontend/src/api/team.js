import request from './request'

export const teamApi = {
  // ==================== 团队基本操作 ====================
  
  /**
   * 创建团队
   * @param {Object} data - { competitionId, teamName, description, maxMembers }
   */
  create(data) {
    return request.post('/team/create', data)
  },
  
  /**
   * 获取我的团队
   */
  getMyTeams() {
    return request.get('/team/my-teams')
  },
  
  /**
   * 获取团队详情
   * @param {Number} id - 团队ID
   */
  getTeamDetail(id) {
    return request.get(`/team/${id}`)
  },
  
  /**
   * 退出团队
   * @param {Number} teamId - 团队ID
   */
  quitTeam(teamId) {
    return request.delete(`/team/quit/${teamId}`)
  },
  
  /**
   * 解散团队（队长）
   * @param {Number} teamId - 团队ID
   */
  disbandTeam(teamId) {
    return request.delete(`/team/disband/${teamId}`)
  },
  
  // ==================== 团队成员管理 ====================
  
  /**
   * 移除成员（队长）
   * @param {Number} teamId - 团队ID
   * @param {Number} userId - 用户ID
   */
  removeMember(teamId, userId) {
    return request.delete(`/team/${teamId}/member/${userId}`)
  },
  
  /**
   * 转让队长（队长）
   * @param {Object} data - { teamId, newLeaderId }
   */
  transferLeader(data) {
    return request.put('/team/transfer-leader', data)
  },
  
  // ==================== 团队申请相关 ====================
  
  /**
   * 申请加入团队
   * @param {Object} data - { teamId, applyReason }
   */
  applyToTeam(data) {
    return request.post('/team/apply', data)
  },
  
  /**
   * 处理申请（队长）
   * @param {Object} data - { teamMemberId, action, comment } action: 1-同意, 2-拒绝
   */
  handleApply(data) {
    return request.put('/team/handle-apply', data)
  },
  
  /**
   * 获取待处理申请（队长）
   * @param {Number} teamId - 团队ID
   */
  getPendingApplications(teamId) {
    return request.get(`/team/pending-applications/${teamId}`)
  },
  
  /**
   * 获取用户申请的团队列表
   */
  getMyApplications() {
    return request.get('/team/my-applications')
  },
  
  // ==================== 招募需求管理 ====================
  
  /**
   * 发布招募需求（队长）
   * @param {Object} data - { teamId, needSkills, needCount, description }
   */
  addRecruitment(data) {
    return request.post('/team/recruitment', data)
  },
  
  /**
   * 关闭招募需求（队长）
   * @param {Number} recruitmentId - 招募需求ID
   */
  closeRecruitment(recruitmentId) {
    return request.put(`/team/recruitment/${recruitmentId}/close`)
  },

    /**
   * 更新团队状态（队长）
   * @param {Number} teamId - 团队ID
   * @param {Number} status - 状态: 0-招募中, 1-已满员, 2-已解散
   */
  updateTeamStatus(teamId, status) {
    return request.put(`/team/${teamId}/status`, { status })
  },
  
  // ==================== 智能推荐 ====================
  
  /**
   * 推荐队友
   * @param {Number} competitionId - 竞赛ID
   */
  recommendMembers(competitionId) {
    return request.get(`/team/recommend/${competitionId}`)
  },
  
  // ==================== 团队查询 ====================
  
  /**
   * 获取竞赛的所有团队（分页）
   * @param {Number} competitionId - 竞赛ID
   * @param {Object} params - { page, size, keyword }
   */
  getTeamsByCompetition(competitionId, params) {
    return request.get(`/team/competition/${competitionId}`, { params })
  },
  
  /**
   * 获取团队列表（管理员）
   * @param {Object} params - { page, size, keyword, status }
   */
  getAdminTeamList(params) {
    return request.get('/team/admin/list', { params })
  },

    /**
   * 更新团队状态（队长）
   * @param {Number} teamId - 团队ID
   * @param {Number} status - 状态: 0-招募中, 1-已满员, 2-已解散
   */
  updateTeamStatus(teamId, status) {
    return request.put(`/team/${teamId}/status`, { status })
  },
  
  // ==================== 团队统计 ====================
  
  /**
   * 获取团队统计信息
   */
  getTeamStatistics() {
    return request.get('/team/statistics')
  }
}