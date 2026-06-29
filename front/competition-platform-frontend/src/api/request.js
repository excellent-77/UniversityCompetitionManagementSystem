import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求队列，用于处理 token 刷新
let isRefreshing = false
let requests = []

// 处理请求队列
function processQueue(error, token = null) {
  requests.forEach(callback => {
    if (error) {
      callback(error)
    } else {
      callback(null, token)
    }
  })
  requests = []
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 显示加载状态
    if (config.showLoading !== false) {
      config.loading = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
    
    // 从 sessionStorage 读取 token（每个标签页独立）
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 如果是 FormData，删除 Content-Type 让浏览器自动设置
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  async response => {
    // 关闭加载状态
    if (response.config.loading) {
      response.config.loading.close()
    }
    
    const res = response.data
    
    // 处理业务状态码
    if (res.code !== 200) {
      // token 过期
      if (res.code === 401) {
        const originalRequest = response.config
        
        if (!isRefreshing) {
          isRefreshing = true
          
          try {
            // 从 sessionStorage 获取 refreshToken
            const refreshToken = sessionStorage.getItem('refreshToken')
            if (refreshToken) {
              const refreshRes = await axios.post('/api/user/refresh-token', {
                refreshToken
              })
              if (refreshRes.data.code === 200) {
                const newToken = refreshRes.data.data.token
                sessionStorage.setItem('token', newToken)
                
                // 重试队列中的请求
                processQueue(null, newToken)
                
                // 重试当前请求
                originalRequest.headers.Authorization = `Bearer ${newToken}`
                return request(originalRequest)
              } else {
                throw new Error('刷新 token 失败')
              }
            } else {
              throw new Error('无 refresh token')
            }
          } catch (error) {
            processQueue(error, null)
            // 清除 sessionStorage 中的登录信息
            sessionStorage.removeItem('token')
            sessionStorage.removeItem('refreshToken')
            sessionStorage.removeItem('user')
            ElMessage.error('登录已过期，请重新登录')
            router.push('/login')
            return Promise.reject(error)
          } finally {
            isRefreshing = false
          }
        } else {
          // 等待 token 刷新
          return new Promise((resolve, reject) => {
            requests.push((error, token) => {
              if (error) {
                reject(error)
              } else {
                originalRequest.headers.Authorization = `Bearer ${token}`
                resolve(request(originalRequest))
              }
            })
          })
        }
      } else if (res.code === 403) {
        ElMessage.error('没有权限执行此操作')
      } else if (res.code === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (res.code === 500) {
        ElMessage.error('服务器内部错误，请稍后重试')
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    // 关闭加载状态
    if (error.config && error.config.loading) {
      error.config.loading.close()
    }
    
    // 处理网络错误
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          ElMessage.error(data?.message || '请求参数错误')
          break
        case 401:
          // 清除 sessionStorage 中的登录信息
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('refreshToken')
          sessionStorage.removeItem('user')
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限执行此操作')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 408:
          ElMessage.error('请求超时')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        case 502:
          ElMessage.error('网关错误')
          break
        case 503:
          ElMessage.error('服务不可用')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络设置')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

// 导出 request 实例
export default request

// 导出常用的请求方法
export const get = (url, params, config) => {
  return request.get(url, { params, ...config })
}

export const post = (url, data, config) => {
  return request.post(url, data, config)
}

export const put = (url, data, config) => {
  return request.put(url, data, config)
}

export const del = (url, config) => {
  return request.delete(url, config)
}

export const upload = (url, file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: progressEvent => {
      if (onProgress) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}