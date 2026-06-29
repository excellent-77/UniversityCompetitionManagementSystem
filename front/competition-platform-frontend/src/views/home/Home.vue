<template>
  <div class="home-content">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <h2>欢迎回来，{{ user.realName || user.username }}！</h2>
      <p>这里是高校竞赛管理平台，让竞赛组织更高效，让团队协作更轻松。</p>
    </div>
    
    <!-- 公告栏 -->
    <div class="announcement-section">
      <div class="section-header">
        <h3>📢 系统公告</h3>
        <el-button type="primary" link @click="goToAnnouncements">更多公告</el-button>
      </div>
      <div class="announcement-list" v-loading="announcementLoading">
        <div v-for="item in announcements" :key="item.id" class="announcement-item" @click="viewAnnouncement(item)">
          <div class="announcement-tag" v-if="item.isTop === 1">
            <el-tag type="danger" size="small">置顶</el-tag>
          </div>
          <div class="announcement-title">{{ item.title }}</div>
          <div class="announcement-time">{{ formatDate(item.publishTime) }}</div>
        </div>
        <div v-if="announcements.length === 0 && !announcementLoading" class="empty-announcement">
          <el-empty description="暂无公告" :image-size="60" />
        </div>
      </div>
    </div>
    
    <!-- 统计数据卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" @click="goToCompetitions()">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.total }}</div>
          <div class="stat-label">总竞赛数</div>
        </div>
      </el-card>
      <el-card class="stat-card" @click="goToCompetitions(1)">
        <div class="stat-icon">📝</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.registering }}</div>
          <div class="stat-label">报名中</div>
        </div>
      </el-card>
      <el-card class="stat-card" @click="goToCompetitions(2)">
        <div class="stat-icon">⚡</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.ongoing }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </el-card>
      <el-card class="stat-card" @click="goToCompetitions(3)">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.ended }}</div>
          <div class="stat-label">已结束</div>
        </div>
      </el-card>
    </div>
    
    <!-- 热门竞赛推荐 -->
    <div class="section">
      <div class="section-header">
        <h3>🔥 热门竞赛推荐</h3>
        <el-button type="primary" link @click="goToCompetitions">查看更多</el-button>
      </div>
      <div class="competition-grid" v-loading="competitionsLoading">
        <CompetitionCard 
          v-for="item in hotCompetitions" 
          :key="item.id" 
          :competition="item"
          @click="goToCompetition"
        />
      </div>
      <div v-if="hotCompetitions.length === 0 && !competitionsLoading" class="empty-state">
        <el-empty description="暂无竞赛信息" />
      </div>
    </div>
    
    <!-- 学生专属：智能组队推荐 -->
    <div v-if="isStudent" class="section">
      <div class="section-header">
        <h3>💡 智能组队推荐</h3>
      </div>
      <div class="tip-card">
        <div class="tip-icon">🤝</div>
        <div class="tip-content">
          <p>根据您的技能标签 <strong>{{ userSkills.join(', ') || '未设置' }}</strong>，系统将为您推荐合适的队友！</p>
          <p class="tip-hint">完善个人技能档案可获得更精准的匹配推荐</p>
        </div>
        <el-button type="primary" @click="goToProfile">完善个人资料</el-button>
      </div>
      
      <!-- 推荐队友列表 -->
      <div v-if="recommendedMembers.length > 0" class="recommend-members">
        <h4>可能感兴趣的队友</h4>
        <div class="members-list">
          <div v-for="member in recommendedMembers" :key="member.userId" class="member-item">
            <div class="member-avatar">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="member-info">
              <div class="member-name">{{ member.realName }}</div>
              <div class="member-major">{{ member.major }} · {{ member.grade }}</div>
              <div class="member-skills">
                <el-tag v-for="skill in member.skills" :key="skill" size="small" type="info">
                  {{ skill }}
                </el-tag>
              </div>
            </div>
            <el-button type="primary" size="small" @click="viewUserProfile(member.userId)">
              <el-icon><View /></el-icon> 查看个人信息
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 教师专属：指导建议 -->
    <div v-if="isTeacher" class="section">
      <div class="section-header">
        <h3>👨‍🏫 指导建议</h3>
      </div>
      <div class="tip-card">
        <div class="tip-icon">📚</div>
        <div class="tip-content">
          <p>您当前可指导 <strong>{{ teacherAvailableSlots }}</strong> 个团队</p>
          <p>系统将根据您的研究方向 <strong>{{ teacherDirections.join(', ') || '未设置' }}</strong> 为您匹配合适的团队</p>
        </div>
        <el-button type="primary" @click="goToMyGuidance">查看指导申请</el-button>
      </div>
      
      <!-- 待处理指导申请 -->
      <div v-if="pendingGuidance.length > 0" class="pending-guidance">
        <h4>待处理的指导申请</h4>
        <div class="guidance-list">
          <div v-for="item in pendingGuidance" :key="item.id" class="guidance-item">
            <div class="guidance-info">
              <div class="team-name">{{ item.teamName }}</div>
              <div class="competition-name">{{ item.competitionTitle }}</div>
            </div>
            <div class="guidance-actions">
              <el-button type="success" size="small" @click="acceptGuidance(item)">接受</el-button>
              <el-button type="danger" size="small" @click="rejectGuidance(item)">拒绝</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 系统概览（管理员） -->
    <div v-if="isAdmin" class="section">
      <div class="section-header">
        <h3>📊 系统概览</h3>
      </div>
      <div class="admin-stats" v-loading="adminStatsLoading">
        <el-card class="admin-stat-card" @click="goToUserManage()">
          <div class="stat-value">{{ systemStats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </el-card>
        <el-card class="admin-stat-card" @click="goToUserManage('student')">
          <div class="stat-value">{{ systemStats.studentCount || 0 }}</div>
          <div class="stat-label">学生用户</div>
        </el-card>
        <el-card class="admin-stat-card" @click="goToUserManage('teacher')">
          <div class="stat-value">{{ systemStats.teacherCount || 0 }}</div>
          <div class="stat-label">教师用户</div>
        </el-card>
        <el-card class="admin-stat-card" @click="goToTeamManage">
          <div class="stat-value">{{ systemStats.activeTeamCount || 0 }}</div>
          <div class="stat-label">活跃团队</div>
        </el-card>
      </div>
      
      <!-- 更多统计数据 -->
      <div class="more-stats">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-title">今日新增用户</div>
              <div class="stat-number">{{ systemStats.todayNewUsers || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-title">今日新增团队</div>
              <div class="stat-number">{{ systemStats.todayNewTeams || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-title">进行中竞赛</div>
              <div class="stat-number">{{ systemStats.ongoingCompetitions || 0 }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    
    <!-- 用户信息弹窗 -->
    <el-dialog v-model="showUserProfileDialog" :title="`${selectedUser?.realName || '用户'} 的个人信息`" width="550px">
      <div v-loading="profileLoading" class="user-profile">
        <div class="profile-avatar">
          <el-icon :size="80"><User /></el-icon>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">
            {{ selectedUser?.realName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleType(selectedUser?.role)">{{ getRoleText(selectedUser?.role) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号" v-if="selectedUser?.phone">
            <el-link :href="`tel:${selectedUser.phone}`" type="primary">{{ selectedUser.phone }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱" v-if="selectedUser?.email">
            <el-link :href="`mailto:${selectedUser.email}`" type="primary">{{ selectedUser.email }}</el-link>
          </el-descriptions-item>
          <template v-if="selectedUser?.role === 2">
            <el-descriptions-item label="专业" v-if="selectedUser?.major">
              {{ selectedUser.major }}
            </el-descriptions-item>
            <el-descriptions-item label="年级" v-if="selectedUser?.grade">
              {{ selectedUser.grade }}
            </el-descriptions-item>
            <el-descriptions-item label="技能标签" v-if="selectedUser?.skills && selectedUser.skills.length">
              <div class="skill-tags">
                <el-tag v-for="skill in selectedUser.skills" :key="skill" size="small" type="info">
                  {{ skill }}
                </el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="竞赛兴趣" v-if="selectedUser?.interests && selectedUser.interests.length">
              <div class="interest-tags">
                <el-tag v-for="interest in selectedUser.interests" :key="interest" size="small" type="success">
                  {{ interest }}
                </el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="获奖经历" v-if="selectedUser?.honors">
              {{ selectedUser.honors }}
            </el-descriptions-item>
          </template>
          <template v-if="selectedUser?.role === 1">
            <el-descriptions-item label="所属学院" v-if="selectedUser?.department">
              {{ selectedUser.department }}
            </el-descriptions-item>
            <el-descriptions-item label="职称" v-if="selectedUser?.title">
              {{ selectedUser.title }}
            </el-descriptions-item>
            <el-descriptions-item label="研究方向" v-if="selectedUser?.researchDirection && selectedUser.researchDirection.length">
              <div class="direction-tags">
                <el-tag v-for="dir in selectedUser.researchDirection" :key="dir" size="small" type="warning">
                  {{ dir }}
                </el-tag>
              </div>
            </el-descriptions-item>
          </template>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showUserProfileDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="showAnnouncementDialog" :title="selectedAnnouncement?.title || '公告详情'" width="600px">
      <div class="announcement-detail" v-if="selectedAnnouncement">
        <div class="detail-meta">
          <span>发布人：{{ selectedAnnouncement.publishUserName || '系统管理员' }}</span>
          <span>发布时间：{{ formatDateTime(selectedAnnouncement.publishTime) }}</span>
        </div>
        <div class="detail-content" v-html="selectedAnnouncement.content"></div>
      </div>
      <template #footer>
        <el-button @click="showAnnouncementDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { competitionApi } from '@/api/competition'
import { teamApi } from '@/api/team'
import { guidanceApi } from '@/api/guidance'
import { userApi } from '@/api/user'
import { announcementApi } from '@/api/announcement'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import CompetitionCard from '@/components/competition/CompetitionCard.vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

// 用户相关
const user = computed(() => userStore.user)
const isStudent = computed(() => userStore.isStudent)
const isTeacher = computed(() => userStore.isTeacher)
const isAdmin = computed(() => userStore.isAdmin)

// 用户技能和方向
const userSkills = ref([])
const teacherDirections = ref([])
const teacherAvailableSlots = ref(0)

// 统计数据
const statistics = ref({ total: 0, registering: 0, ongoing: 0, ended: 0 })
const hotCompetitions = ref([])
const competitionsLoading = ref(false)

// 推荐队友
const recommendedMembers = ref([])

// 待处理指导申请
const pendingGuidance = ref([])

// 管理员统计数据
const adminStatsLoading = ref(false)
const systemStats = ref({
  totalUsers: 0,
  studentCount: 0,
  teacherCount: 0,
  activeTeamCount: 0,
  todayNewUsers: 0,
  todayNewTeams: 0,
  ongoingCompetitions: 0
})

// 用户信息弹窗
const showUserProfileDialog = ref(false)
const profileLoading = ref(false)
const selectedUser = ref(null)

// 公告相关
const announcements = ref([])
const announcementLoading = ref(false)
const showAnnouncementDialog = ref(false)
const selectedAnnouncement = ref(null)

// 角色映射
const getRoleType = (role) => {
  const types = { 0: 'danger', 1: 'success', 2: 'primary' }
  return types[role] || 'info'
}

const getRoleText = (role) => {
  const texts = { 0: '管理员', 1: '教师', 2: '学生' }
  return texts[role] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('MM-DD')
}

const formatDateTime = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await competitionApi.getStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取热门竞赛
const fetchHotCompetitions = async () => {
  competitionsLoading.value = true
  try {
    const res = await competitionApi.getList({ page: 1, size: 6, status: 1 })
    if (res.code === 200) {
      hotCompetitions.value = res.data.records
    }
  } catch (error) {
    console.error('获取竞赛列表失败', error)
  } finally {
    competitionsLoading.value = false
  }
}

// 获取推荐队友
const fetchRecommendedMembers = async () => {
  if (!isStudent.value) return
  try {
    const res = await competitionApi.getList({ page: 1, size: 1, status: 1 })
    if (res.code === 200 && res.data.records.length > 0) {
      const competitionId = res.data.records[0].id
      const teamRes = await teamApi.recommendMembers(competitionId)
      if (teamRes.code === 200) {
        recommendedMembers.value = teamRes.data.slice(0, 3)
      }
    }
  } catch (error) {
    console.error('获取推荐队友失败', error)
  }
}

// 获取公告列表 - 首页只显示已发布的公告
const fetchAnnouncements = async () => {
  announcementLoading.value = true
  try {
    // 只获取已发布的公告
    const res = await announcementApi.getList({ page: 1, size: 5, status: 1 })
    if (res.code === 200) {
      announcements.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取公告失败', error)
  } finally {
    announcementLoading.value = false
  }
}

// 查看公告详情
const viewAnnouncement = async (item) => {
  try {
    const res = await announcementApi.getDetail(item.id)
    if (res.code === 200) {
      selectedAnnouncement.value = res.data
      showAnnouncementDialog.value = true
    }
  } catch (error) {
    console.error('获取公告详情失败', error)
    ElMessage.error('获取公告详情失败')
  }
}

// 跳转到公告列表页
const goToAnnouncements = () => {
  router.push('/home/announcements')
}
// 查看用户个人信息
const viewUserProfile = async (userId) => {
  profileLoading.value = true
  showUserProfileDialog.value = true
  try {
    const res = await userApi.getUserDetail(userId)
    if (res.code === 200) {
      selectedUser.value = res.data
      
      // 解析技能
      if (selectedUser.value.skills && typeof selectedUser.value.skills === 'string') {
        try {
          selectedUser.value.skills = JSON.parse(selectedUser.value.skills)
        } catch {
          selectedUser.value.skills = selectedUser.value.skills.split(',')
        }
      }
      // 解析兴趣
      if (selectedUser.value.interests && typeof selectedUser.value.interests === 'string') {
        try {
          selectedUser.value.interests = JSON.parse(selectedUser.value.interests)
        } catch {
          selectedUser.value.interests = selectedUser.value.interests.split(',')
        }
      }
      // 解析研究方向
      if (selectedUser.value.researchDirection && typeof selectedUser.value.researchDirection === 'string') {
        try {
          selectedUser.value.researchDirection = JSON.parse(selectedUser.value.researchDirection)
        } catch {
          selectedUser.value.researchDirection = selectedUser.value.researchDirection.split(',')
        }
      }
    } else {
      ElMessage.error(res.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    profileLoading.value = false
  }
}

// 获取待处理指导申请（教师）
const fetchPendingGuidance = async () => {
  if (!isTeacher.value) return
  try {
    const res = await guidanceApi.getMyApplications()
    if (res.code === 200) {
      pendingGuidance.value = res.data.filter(item => item.statusCode === 0)
    }
  } catch (error) {
    console.error('获取待处理指导申请失败', error)
  }
}

// 获取系统统计（管理员）
const fetchSystemStatistics = async () => {
  if (!isAdmin.value) return
  
  adminStatsLoading.value = true
  try {
    const res = await userApi.getSystemStatistics()
    if (res.code === 200) {
      systemStats.value = res.data
    }
  } catch (error) {
    console.error('获取系统统计失败', error)
  } finally {
    adminStatsLoading.value = false
  }
}

// 处理教师指导申请
const acceptGuidance = async (item) => {
  try {
    const res = await guidanceApi.handle({
      relationId: item.relationId,
      action: 1,
      comment: ''
    })
    if (res.code === 200) {
      ElMessage.success('已接受指导申请')
      fetchPendingGuidance()
      fetchSystemStatistics()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const rejectGuidance = async (item) => {
  try {
    const { value: comment } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝指导申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    const res = await guidanceApi.handle({
      relationId: item.relationId,
      action: 2,
      comment: comment || ''
    })
    if (res.code === 200) {
      ElMessage.success('已拒绝指导申请')
      fetchPendingGuidance()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 用户取消
  }
}

// 导航函数
const goToCompetition = (id) => {
  router.push(`/home/competition/${id}`)
}

const goToCompetitions = (status) => {
  if (status !== undefined && status !== null) {
    router.push({ path: '/home/competitions', query: { status: status } })
  } else {
    router.push('/home/competitions')
  }
}

const goToMyGuidance = () => {
  router.push('/home/my-guidance')
}

const goToProfile = () => {
  router.push('/home/profile')
}

const goToUserManage = (role) => {
  if (role === 'student') {
    router.push({ path: '/home/admin/users', query: { role: 2 } })
  } else if (role === 'teacher') {
    router.push({ path: '/home/admin/users', query: { role: 1 } })
  } else {
    router.push('/home/admin/users')
  }
}

const goToTeamManage = () => {
  router.push('/home/admin/teams')
}

// 获取用户详细信息
const fetchUserDetails = async () => {
  try {
    await userStore.fetchUserInfo()
    if (isStudent.value) {
      const userInfo = user.value
      if (userInfo.skills) {
        try {
          userSkills.value = JSON.parse(userInfo.skills)
        } catch {
          userSkills.value = userInfo.skills ? userInfo.skills.split(',') : []
        }
      }
    }
    if (isTeacher.value) {
      const userInfo = user.value
      if (userInfo.researchDirection) {
        try {
          teacherDirections.value = JSON.parse(userInfo.researchDirection)
        } catch {
          teacherDirections.value = userInfo.researchDirection ? userInfo.researchDirection.split(',') : []
        }
      }
      teacherAvailableSlots.value = (userInfo.maxTeams || 5) - (userInfo.currentTeams || 0)
    }
  } catch (error) {
    console.error('获取用户详情失败', error)
  }
}

onMounted(() => {
  fetchStatistics()
  fetchHotCompetitions()
  fetchUserDetails()
  fetchRecommendedMembers()
  fetchPendingGuidance()
  fetchSystemStatistics()
  fetchAnnouncements()  // 获取公告
})
</script>

<style scoped lang="scss">
.home-content {
  min-height: calc(100vh - 60px);
}

// 欢迎卡片
.welcome-card {
  background: linear-gradient(135deg, #1e3c72 0%, #2b4c7c 100%);
  border-radius: 16px;
  padding: 32px;
  color: #fff;
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    margin-bottom: 12px;
  }
  
  p {
    opacity: 0.9;
  }
}

// 统计卡片
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
  
  .stat-card {
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
    
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
    }
    
    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      background: #e8f0fe;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
    }
    
    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #333;
      }
      
      .stat-label {
        font-size: 14px;
        color: #999;
        margin-top: 4px;
      }
    }
  }
}

// 区块
.section {
  margin-bottom: 32px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
    }
  }
}

// 竞赛网格
.competition-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

// 提示卡片
.tip-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  border: 1px solid #e8eef4;
  
  .tip-icon {
    font-size: 48px;
  }
  
  .tip-content {
    flex: 1;
    
    p {
      margin: 0 0 8px;
      color: #666;
      
      strong {
        color: #1e3c72;
      }
    }
    
    .tip-hint {
      font-size: 12px;
      color: #999;
      margin: 0;
    }
  }
}

// 推荐队友
.recommend-members, .pending-guidance {
  margin-top: 20px;
  
  h4 {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 16px;
    color: #333;
  }
  
  .members-list, .guidance-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .member-item {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 16px;
    border: 1px solid #e8eef4;
    
    .member-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #e8f0fe;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #1e3c72;
    }
    
    .member-info {
      flex: 1;
      
      .member-name {
        font-weight: 600;
        color: #333;
        margin-bottom: 4px;
      }
      
      .member-major {
        font-size: 12px;
        color: #999;
        margin-bottom: 8px;
      }
      
      .member-skills {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
      }
    }
  }
  
  .guidance-item {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border: 1px solid #e8eef4;
    
    .guidance-info {
      .team-name {
        font-weight: 600;
        color: #333;
        margin-bottom: 4px;
      }
      
      .competition-name {
        font-size: 12px;
        color: #999;
      }
    }
    
    .guidance-actions {
      display: flex;
      gap: 8px;
    }
  }
}

// 管理员统计
.admin-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  
  .admin-stat-card {
    text-align: center;
    padding: 16px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
    
    .stat-value {
      font-size: 32px;
      font-weight: 600;
      color: #1e3c72;
    }
    
    .stat-label {
      font-size: 14px;
      color: #999;
      margin-top: 8px;
    }
  }
}

// 更多统计
.more-stats {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  
  .stat-item {
    text-align: center;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 12px;
    
    .stat-title {
      font-size: 14px;
      color: #999;
      margin-bottom: 8px;
    }
    
    .stat-number {
      font-size: 24px;
      font-weight: 600;
      color: #1e3c72;
    }
  }
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

// 用户信息弹窗样式
.user-profile {
  .profile-avatar {
    text-align: center;
    margin-bottom: 20px;
    color: #1e3c72;
  }
  
  .skill-tags,
  .interest-tags,
  .direction-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

// 公告栏样式
.announcement-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0;
    }
  }
  
  .announcement-list {
    .announcement-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;
      cursor: pointer;
      border-bottom: 1px solid #f5f5f5;
      transition: all 0.2s;
      
      &:hover {
        background: #f8f9fa;
        padding-left: 8px;
      }
      
      .announcement-tag {
        flex-shrink: 0;
      }
      
      .announcement-title {
        flex: 1;
        font-size: 14px;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .announcement-time {
        font-size: 12px;
        color: #999;
        flex-shrink: 0;
      }
    }
    
    .empty-announcement {
      padding: 20px 0;
    }
  }
}

// 公告详情弹窗样式
.announcement-detail {
  .detail-meta {
    display: flex;
    justify-content: space-between;
    padding-bottom: 12px;
    margin-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    font-size: 13px;
    color: #999;
  }
  
  .detail-content {
    line-height: 1.8;
    font-size: 14px;
    color: #555;
    
    :deep(h1), :deep(h2), :deep(h3) {
      margin: 16px 0 8px;
    }
    
    :deep(p) {
      margin-bottom: 12px;
    }
    
    :deep(ul), :deep(ol) {
      padding-left: 20px;
      margin-bottom: 12px;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .competition-grid {
    grid-template-columns: 1fr;
  }
  
  .tip-card {
    flex-direction: column;
    text-align: center;
  }
  
  .admin-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .member-item {
    flex-direction: column;
    text-align: center;
  }
  
  .member-info {
    text-align: center;
  }
  
  .member-skills {
    justify-content: center;
  }
}
</style>