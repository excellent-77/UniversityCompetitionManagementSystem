<template>
  <header class="app-header">
    <div class="header-left">
      <div class="logo" @click="goHome">
        <span class="logo-icon">🏆</span>
        <span class="logo-text">高校竞赛管理平台</span>
      </div>
    </div>
    
    <div class="header-center">
      <el-menu mode="horizontal" :default-active="activeMenu" router>
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/competitions">竞赛列表</el-menu-item>
        <el-menu-item v-if="isStudent" index="/my-teams">我的团队</el-menu-item>
        <el-menu-item v-if="isTeacher" index="/my-guidance">我的指导</el-menu-item>
        <el-menu-item v-if="isStudent" index="/my-guidance">指导申请</el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admin/users">用户管理</el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admin/competitions">竞赛管理</el-menu-item>
      </el-menu>
    </div>
    
    <div class="header-right">
      <!-- 通知图标 -->
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-icon :size="20" class="notification-icon" @click="showNotifications = true">
          <Bell />
        </el-icon>
      </el-badge>
      
      <!-- 用户下拉菜单 -->
      <el-dropdown @command="handleCommand">
        <div class="user-dropdown">
          <span class="username">{{ user.realName || user.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> 个人中心
            </el-dropdown-item>
            <el-dropdown-item command="notifications">
              <el-icon><Bell /></el-icon> 通知中心
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <!-- 通知抽屉 -->
    <el-drawer v-model="showNotifications" title="通知消息" direction="rtl" size="400px">
      <div class="notification-list" v-loading="loading">
        <div v-for="item in notifications" :key="item.id" 
             class="notification-item" 
             :class="{ unread: !item.isRead }" 
             @click="handleNotificationClick(item)">
          <div class="notification-icon">
            <el-icon :size="20">
              <Message v-if="item.type === 0" />
              <UserFilled v-else-if="item.type === 1" />
              <Guide v-else-if="item.type === 2" />
              <Trophy v-else />
            </el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
          </div>
        </div>
        <div v-if="notifications.length === 0 && !loading" class="empty-notifications">
          <el-empty description="暂无通知" />
        </div>
      </div>
      <template #footer>
        <div class="notification-footer" v-if="notifications.length > 0">
          <el-button type="primary" link @click="markAllAsRead">全部标记已读</el-button>
          <el-button type="danger" link @click="clearReadNotifications">清空已读通知</el-button>
        </div>
      </template>
    </el-drawer>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { notificationApi } from '@/api/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 通知相关
const loading = ref(false)
const showNotifications = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

// 定时器
let interval = null

const user = computed(() => userStore.user)
const isStudent = computed(() => userStore.isStudent)
const isTeacher = computed(() => userStore.isTeacher)
const isAdmin = computed(() => userStore.isAdmin)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/competitions')) return '/competitions'
  if (path.startsWith('/my-teams')) return '/my-teams'
  if (path.startsWith('/my-guidance')) return '/my-guidance'
  if (path.startsWith('/admin')) return path
  return '/home'
})

const goHome = () => {
  router.push('/home')
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

/**
 * 获取通知列表
 */
const fetchNotifications = async () => {
  if (!userStore.isLoggedIn) return

  loading.value = true
  try {
    const res = await notificationApi.getList({ page: 1, size: 50 })
    if (res.code === 200) {
      // 兼容两种后端返回格式
      if (Array.isArray(res.data)) {
        // 格式: { data: [...] }
        notifications.value = res.data
        unreadCount.value = res.data.filter(n => !n.isRead).length
      } else {
        // 格式: { data: { list: [...], unreadCount: 2 } }
        notifications.value = res.data.list || []
        unreadCount.value = res.data.unreadCount || 0
      }
    }
  } catch (error) {
    console.error('获取通知失败', error)
  } finally {
    loading.value = false
  }
}

/**
 * 获取未读数量
 */
const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) return

  try {
    const res = await notificationApi.getUnreadCount()
    if (res.code === 200) {
      // 兼容两种后端返回格式：
      // 格式1: { data: { count: 2 } }
      // 格式2: { data: 2 }
      const count = typeof res.data === 'object' ? res.data.count : res.data
      unreadCount.value = count || 0
    }
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

/**
 * 标记单个通知为已读
 */
const markAsRead = async (item) => {
  if (item.isRead) return
  
  try {
    await notificationApi.markAsRead(item.id)
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

/**
 * 全部标记已读
 */
const markAllAsRead = async () => {
  try {
    const res = await notificationApi.markAllAsRead()
    if (res.code === 200) {
      notifications.value.forEach(n => n.isRead = true)
      unreadCount.value = 0
      ElMessage.success(res.message || '已全部标记为已读')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

/**
 * 清空已读通知
 */
const clearReadNotifications = async () => {
  ElMessageBox.confirm('确定要清空所有已读通知吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await notificationApi.clearRead()
      if (res.code === 200) {
        notifications.value = notifications.value.filter(n => !n.isRead)
        ElMessage.success(res.message || '已清空已读通知')
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

/**
 * 点击通知处理
 */
const handleNotificationClick = async (item) => {
  // 标记为已读
  await markAsRead(item)
  
  // 根据类型跳转
  if (item.relatedId) {
    if (item.type === 1) {
      // 团队相关通知，跳转到团队详情
      router.push(`/team/${item.relatedId}`)
    } else if (item.type === 2) {
      // 指导相关通知，跳转到指导页面
      router.push(`/teacher-recommend/${item.relatedId}`)
    } else if (item.type === 3) {
      // 竞赛相关通知，跳转到竞赛详情
      router.push(`/competition/${item.relatedId}`)
    }
  }
  
  showNotifications.value = false
}

/**
 * 用户菜单命令
 */
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'notifications') {
    showNotifications.value = true
    fetchNotifications()
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    }).catch(() => {})
  }
}

onMounted(() => {
  // 获取未读数量
  fetchUnreadCount()
  
  // 每30秒刷新一次未读数量
  interval = setInterval(() => {
    if (userStore.isLoggedIn) {
      fetchUnreadCount()
    }
  }, 30000)
})

onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }
})
</script>

<style scoped lang="scss">
.app-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  
  .header-left {
    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .logo-icon {
        font-size: 28px;
      }
      
      .logo-text {
        font-size: 18px;
        font-weight: 600;
        color: #1e3c72;
      }
    }
  }
  
  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;
    
    :deep(.el-menu) {
      border-bottom: none;
      background: transparent;
      
      .el-menu-item {
        font-size: 15px;
        
        &:hover {
          background: #f5f7fa;
        }
        
        &.is-active {
          color: #1e3c72;
          border-bottom-color: #1e3c72;
        }
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .notification-badge {
      cursor: pointer;
      
      .notification-icon {
        font-size: 20px;
        color: #666;
        
        &:hover {
          color: #1e3c72;
        }
      }
    }
    
    .user-dropdown {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .username {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

.notification-list {
  .notification-item {
    display: flex;
    gap: 12px;
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s;
    
    &:hover {
      background: #f8f9fa;
    }
    
    &.unread {
      background: #f0f7ff;
      
      .notification-title {
        font-weight: 600;
      }
    }
    
    .notification-icon {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: #f0f2f5;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #1e3c72;
    }
    
    .notification-content {
      flex: 1;
      
      .notification-title {
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
      }
      
      .notification-desc {
        font-size: 12px;
        color: #999;
        margin-bottom: 4px;
      }
      
      .notification-time {
        font-size: 11px;
        color: #ccc;
      }
    }
  }
  
  .empty-notifications {
    padding: 40px 0;
  }
}

.notification-footer {
  padding: 12px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>