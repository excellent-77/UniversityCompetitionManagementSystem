import request from './request'

export const announcementApi = {
  /**
   * 获取公告列表
   * @param {Object} params - { page, size, keyword, status }
   */
  getList(params) {
    return request.get('/announcement/list', { params })
  },
  
  /**
   * 获取置顶公告
   */
  getTop() {
    return request.get('/announcement/top')
  },
  
  /**
   * 获取公告详情
   * @param {Number} id - 公告ID
   */
  getDetail(id) {
    return request.get(`/announcement/${id}`)
  },
  
  /**
   * 创建公告（管理员）
   * @param {Object} data - 公告信息
   */
  create(data) {
    return request.post('/announcement/create', data)
  },
  
  /**
   * 更新公告（管理员）
   * @param {Object} data - 公告信息
   */
  update(data) {
    return request.put('/announcement/update', data)
  },
  
  /**
   * 删除公告（管理员）
   * @param {Number} id - 公告ID
   */
  delete(id) {
    return request.delete(`/announcement/delete/${id}`)
  },
  
  /**
   * 更新公告状态（管理员）
   * @param {Object} data - { id, status }
   */
  updateStatus(data) {
    return request.put('/announcement/update-status', data)
  },
  
  /**
   * 置顶/取消置顶（管理员）
   * @param {Number} id - 公告ID
   */
  toggleTop(id) {
    return request.put(`/announcement/toggle-top/${id}`)
  }
}