import request from './request'

export const notificationApi = {
  /**
   * 获取通知列表
   * @param {Object} params - { page, size }
   */
  getList(params) {
    return request.get('/notification/list', { params })
  },
  
  /**
   * 获取未读通知数量
   */
  getUnreadCount() {
    return request.get('/notification/unread-count')
  },
  
  /**
   * 标记通知已读
   * @param {Number} id - 通知ID
   */
  markAsRead(id) {
    return request.put(`/notification/read/${id}`)
  },
  
  /**
   * 标记所有通知已读
   */
  markAllAsRead() {
    return request.put('/notification/read-all')
  },
  
  /**
   * 清空已读通知
   */
  clearRead() {
    return request.delete('/notification/clear-read')
  }
}