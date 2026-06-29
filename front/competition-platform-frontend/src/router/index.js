import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import MainLayout from '@/layout/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/home',
    component: MainLayout,
    redirect: '/home/index',
    children: [
      {
        path: 'index',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'competitions',
        name: 'CompetitionList',
        component: () => import('@/views/competition/CompetitionList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'competition/:id',
        name: 'CompetitionDetail',
        component: () => import('@/views/competition/CompetitionDetail.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'competition/create',
        name: 'CompetitionCreate',
        component: () => import('@/views/competition/CompetitionCreate.vue'),
        meta: { requiresAuth: true, roles: [0, 1] }
      },
      {
        path: 'my-teams',
        name: 'MyTeams',
        component: () => import('@/views/team/MyTeams.vue'),
        meta: { requiresAuth: true, roles: [2] }
      },
      {
        path: 'team/create/:competitionId',
        name: 'TeamCreate',
        component: () => import('@/views/team/TeamCreate.vue'),
        meta: { requiresAuth: true, roles: [2] }
      },
      {
        path: 'team/:id',
        name: 'TeamDetail',
        component: () => import('@/views/team/TeamDetail.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'find-team/:competitionId',
        name: 'FindTeam',
        component: () => import('@/views/team/FindTeam.vue'),
        meta: { requiresAuth: true, roles: [2] }
      },
      {
        path: 'teacher-recommend/:teamId',
        name: 'TeacherRecommend',
        component: () => import('@/views/guidance/TeacherRecommend.vue'),
        meta: { requiresAuth: true, roles: [2] }
      },
      {
        path: 'my-guidance',
        name: 'MyGuidance',
        component: () => import('@/views/guidance/MyGuidance.vue'),
        meta: { requiresAuth: true, roles: [1, 2] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { requiresAuth: true, roles: [0] }
      },
      {
        path: 'admin/competitions',
        name: 'CompetitionManage',
        component: () => import('@/views/admin/CompetitionManage.vue'),
        meta: { requiresAuth: true, roles: [0] }
      },
      {
        path: 'admin/teams',
        name: 'TeamManage',
        component: () => import('@/views/admin/TeamManage.vue'),
        meta: { requiresAuth: true, roles: [0] }
      },
      {
        path: 'announcements',
        name: 'AnnouncementList',
        component: () => import('@/views/announcement/AnnouncementList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'admin/announcements',
        name: 'AnnouncementManage',
        component: () => import('@/views/admin/AnnouncementManage.vue'),
        meta: { requiresAuth: true, roles: [0] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 使用 sessionStorage（每个标签页独立）
router.beforeEach(async (to, from, next) => {
  const token = sessionStorage.getItem('token')
  const userStr = sessionStorage.getItem('user')
  let userRole = null
  
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      userRole = user.role
    } catch (e) {}
  }

  // 如果是团队详情页，可以预先检查团队是否存在（可选）
  if (to.path.match(/^\/home\/team\/\d+$/)) {
    const teamId = to.path.split('/').pop()
    // 可以在这里预检查团队是否存在
    // 但为了性能，建议在组件内处理
  }

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
    } else if (to.meta.roles && !to.meta.roles.includes(userRole)) {
      next('/home/index')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router