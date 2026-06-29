// src/api/guidance.js
import request from './request'

export const guidanceApi = {
  // 推荐指导教师
  recommendTeachers(teamId) {
    return request.get(`/guidance/recommend-teachers/${teamId}`)
  },
  
  // 申请指导
  apply(data) {
    return request.post('/guidance/apply', data)
  },
  
  // 处理指导申请（教师）
  handle(data) {
    return request.put('/guidance/handle', data)
  },
  
  // 获取教师待处理的指导申请
  getPendingApplications() {
    return request.get('/guidance/pending-applications')
  },
  
  // 获取教师指导的团队列表（已接受）
  getMyGuidanceTeams() {
    return request.get('/guidance/my-teams')
  },
  
  // 获取学生指导申请
  getMyApplications() {
    return request.get('/guidance/my-applications')
  },
  
  // 取消指导申请（学生）
  cancelApplication(relationId) {
    return request.delete(`/guidance/cancel/${relationId}`)
  }
}