import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 使用 sessionStorage（每个标签页独立）
  const token = ref(sessionStorage.getItem('token') || '')
  const user = ref(JSON.parse(sessionStorage.getItem('user') || '{}'))
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value.role === 0)
  const isTeacher = computed(() => user.value.role === 1)
  const isStudent = computed(() => user.value.role === 2)
  
  const login = async (username, password) => {
    try {
      const res = await userApi.login({ username, password })
      if (res.code === 200) {
        const data = res.data
        token.value = data.token
        user.value = {
          userId: data.userId,
          username: data.username,
          realName: data.realName,
          role: data.role
        }
        sessionStorage.setItem('token', data.token)
        sessionStorage.setItem('user', JSON.stringify(user.value))
        return { success: true }
      }
      return { success: false, message: res.message }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }
  
  const logout = () => {
    token.value = ''
    user.value = {}
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }
  
  const updateUserInfo = (info) => {
    user.value = { ...user.value, ...info }
    sessionStorage.setItem('user', JSON.stringify(user.value))
  }
  
  const fetchUserInfo = async () => {
    try {
      const res = await userApi.getUserInfo()
      if (res.code === 200) {
        updateUserInfo(res.data)
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }
  
  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    login,
    logout,
    updateUserInfo,
    fetchUserInfo
  }
})