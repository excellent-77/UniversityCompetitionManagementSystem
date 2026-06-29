<template>
  <div class="app-layout">
    <!-- 头部导航栏 -->
    <header class="header">
      <div class="header-left">
        <div class="logo" @click="goHome">
          <span class="logo-icon">🏆</span>
          <span class="logo-text">高校竞赛管理平台</span>
        </div>
      </div>
      <div class="header-center">
        <el-menu mode="horizontal" :default-active="activeMenu" router>
          <el-menu-item index="/home/index">首页</el-menu-item>
          <el-menu-item index="/home/competitions">竞赛列表</el-menu-item>
          <el-menu-item v-if="isStudent" index="/home/my-teams">我的团队</el-menu-item>
          <el-menu-item v-if="isTeacher" index="/home/my-guidance">我的指导</el-menu-item>
          <el-menu-item v-if="isStudent" index="/home/my-guidance">指导申请</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/home/admin/users">用户管理</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/home/admin/competitions">竞赛管理</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/home/admin/teams">团队管理</el-menu-item>
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
    </header>
    
    <!-- 主体布局 -->
    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar" :class="{ collapsed: isCollapsed }">
        <div class="collapse-btn" @click="toggleSidebar">
          <el-icon :size="20">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        
        <el-menu 
          :default-active="activeSideMenu" 
          :collapse="isCollapsed"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/home/index">
            <el-icon><Odometer /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/home/competitions">
            <el-icon><Trophy /></el-icon>
            <span>竞赛列表</span>
          </el-menu-item>
          <el-menu-item v-if="isStudent" index="/home/my-teams">
            <el-icon><UserFilled /></el-icon>
            <span>我的团队</span>
          </el-menu-item>
          <el-menu-item v-if="isTeacher" index="/home/my-guidance">
            <el-icon><Guide /></el-icon>
            <span>我的指导</span>
          </el-menu-item>
          <el-menu-item v-if="isStudent" index="/home/my-guidance">
            <el-icon><ChatLineSquare /></el-icon>
            <span>指导申请</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/home/admin/users">
            <el-icon><Setting /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/home/admin/competitions">
            <el-icon><Edit /></el-icon>
            <span>竞赛管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/home/admin/teams">
            <el-icon><UserFilled /></el-icon>
            <span>团队管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/home/admin/announcements">
          <el-icon><Notification /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        </el-menu>
      </aside>
      
      <!-- 内容区域 -->
      <main class="content" :class="{ expanded: isCollapsed }">
        <router-view />
      </main>
    </div>
    
    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <a href="#" @click.prevent>关于我们</a>
          <a href="#" @click.prevent>联系方式</a>
          <a href="#" @click.prevent>帮助中心</a>
          <a href="#" @click.prevent>隐私政策</a>
        </div>
        <div class="footer-copyright">
          <p>© 2026 高校竞赛管理平台 | 让竞赛组织更高效，让团队协作更轻松</p>
          <p>南通理工学院 软件工程专业 毕业设计</p>
        </div>
      </div>
    </footer>
    
    <!-- 通知抽屉 -->
    <el-drawer v-model="showNotifications" title="通知消息" direction="rtl" size="400px">
      <!-- 通知内容保持不变 -->
      <div class="notification-list" v-loading="notificationsLoading">
        <div v-for="item in notifications" :key="item.id" 
             class="notification-item" :class="{ unread: !item.isRead }" 
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
        <div v-if="notifications.length === 0 && !notificationsLoading" class="empty-notifications">
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
  </div>
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

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 通知相关
const showNotifications = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const notificationsLoading = ref(false)

let interval = null

const user = computed(() => userStore.user)
const isStudent = computed(() => userStore.isStudent)
const isTeacher = computed(() => userStore.isTeacher)
const isAdmin = computed(() => userStore.isAdmin)

// 顶部菜单激活状态
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/home/index') return '/home/index'
  if (path.startsWith('/home/competitions')) return '/home/competitions'
  if (path.startsWith('/home/my-teams')) return '/home/my-teams'
  if (path.startsWith('/home/my-guidance')) return '/home/my-guidance'
  if (path.startsWith('/home/admin')) return path
  return '/home/index'
})

// 侧边栏菜单激活状态
const activeSideMenu = computed(() => {
  const path = route.path
  if (path === '/home/index') return '/home/index'
  if (path.startsWith('/home/competitions')) return '/home/competitions'
  if (path.startsWith('/home/my-teams')) return '/home/my-teams'
  if (path.startsWith('/home/my-guidance')) return '/home/my-guidance'
  if (path.startsWith('/home/admin')) return path
  return '/home/index'
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebarCollapsed', isCollapsed.value)
}

const goHome = () => {
  router.push('/home/index')
}

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

const fetchNotifications = async () => {
  notificationsLoading.value = true
  try {
    const res = await notificationApi.getList({ page: 1, size: 50 })
    if (res.code === 200) {
      if (Array.isArray(res.data)) {
        notifications.value = res.data
        unreadCount.value = res.data.filter(n => !n.isRead).length
      } else {
        notifications.value = res.data.list || []
        unreadCount.value = res.data.unreadCount || 0
      }
    }
  } catch (error) {
    console.error('获取通知失败', error)
  } finally {
    notificationsLoading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await notificationApi.getUnreadCount()
    if (res.code === 200) {
      const count = typeof res.data === 'object' ? res.data.count : res.data
      unreadCount.value = count || 0
    }
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

const markAllAsRead = async () => {
  try {
    const res = await notificationApi.markAllAsRead()
    if (res.code === 200) {
      notifications.value.forEach(n => n.isRead = true)
      unreadCount.value = 0
      ElMessage.success('已全部标记为已读')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

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
        ElMessage.success('已清空已读通知')
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const handleNotificationClick = async (item) => {
  if (!item.isRead) {
    try {
      await notificationApi.markAsRead(item.id)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
  
  if (item.relatedId) {
    if (item.type === 1) {
      router.push(`/home/team/${item.relatedId}`)
    } else if (item.type === 2) {
      router.push(`/home/teacher-recommend/${item.relatedId}`)
    } else if (item.type === 3) {
      router.push(`/home/competition/${item.relatedId}`)
    }
  }
  showNotifications.value = false
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/home/profile')
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

const initSidebarState = () => {
  const saved = localStorage.getItem('sidebarCollapsed')
  if (saved !== null) {
    isCollapsed.value = saved === 'true'
  }
}

onMounted(() => {
  initSidebarState()
  fetchUnreadCount()
  
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
.app-layout {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

// 头部样式
.header {
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

// 主体布局
.main-layout {
  display: flex;
  margin-top: 60px;
  min-height: calc(100vh - 60px - 120px);
}

// 侧边栏
.sidebar {
  width: 240px;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 0;
  transition: width 0.3s ease;
  overflow: hidden;
  z-index: 99;
  
  &.collapsed {
    width: 64px;
  }
  
  .collapse-btn {
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #666;
    border-bottom: 1px solid #f0f0f0;
    transition: all 0.3s;
    
    &:hover {
      color: #1e3c72;
      background: #f5f7fa;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    height: calc(100% - 48px);
    overflow-y: auto;
    
    &:deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      
      .el-icon {
        font-size: 18px;
      }
      
      span {
        margin-left: 10px;
      }
    }
    
    &:deep(.el-menu-item.is-active) {
      background-color: #e8f0fe;
      color: #1e3c72;
    }
  }
}

// 内容区域
.content {
  flex: 1;
  margin-left: 240px;
  padding: 24px;
  transition: margin-left 0.3s ease;
  
  &.expanded {
    margin-left: 64px;
  }
}

// 页脚
.footer {
  background: #1e3c72;
  color: #fff;
  padding: 24px;
  margin-top: auto;
  
  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    text-align: center;
    
    .footer-links {
      display: flex;
      justify-content: center;
      gap: 32px;
      margin-bottom: 16px;
      
      a {
        color: rgba(255, 255, 255, 0.7);
        text-decoration: none;
        font-size: 14px;
        transition: color 0.2s;
        
        &:hover {
          color: #fff;
        }
      }
    }
    
    .footer-copyright {
      p {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.5);
        margin: 4px 0;
      }
    }
  }
}

// 通知样式
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

// 响应式
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    
    &.collapsed {
      transform: translateX(0);
      width: 240px;
    }
  }
  
  .content {
    margin-left: 0;
    
    &.expanded {
      margin-left: 0;
    }
  }
}
</style>