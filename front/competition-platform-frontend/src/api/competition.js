import request from './request'

export const competitionApi = {
  // 获取竞赛列表
  getList(params) {
    return request.get('/competition/list', { params })
  },
  
  // 获取竞赛详情
  getDetail(id) {
    return request.get(`/competition/${id}`)
  },
  
  // 创建竞赛
  create(data) {
    return request.post('/competition/create', data)
  },
  
  // 更新竞赛
  update(data) {
    return request.put('/competition/update', data)
  },
  
  // 删除竞赛
  delete(id) {
    return request.delete(`/competition/${id}`)
  },
  
  // 获取统计信息
  getStatistics() {
    return request.get('/competition/statistics')
  },
  
  // 团队报名竞赛
  register(data) {
    return request.post('/competition/register', data)
  },
  
  // 获取竞赛报名列表（管理员）
  getRegistrations(competitionId, params) {
    return request.get(`/competition/${competitionId}/registrations`, { params })
  },
  
  // 审核报名（管理员）
  auditRegistration(data) {
    return request.put('/competition/registration/audit', data)
  },
    //获取团队报名状态
  getTeamRegistrationStatus(competitionId, teamId) {
    return request.get(`/competition/${competitionId}/team/${teamId}/registration-status`)
  }
}