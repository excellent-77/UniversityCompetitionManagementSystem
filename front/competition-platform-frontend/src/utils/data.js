// src/utils/date.js
import dayjs from 'dayjs'

// 格式化日期（用于发送到后端）
export const formatToBackend = (date) => {
  if (!date) return null
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化日期（用于前端显示）
export const formatToFrontend = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}