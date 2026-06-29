<template>
  <div class="my-guidance-container">
    <div class="page-header">
      <h2>{{ isTeacher ? '我的指导团队' : '我的指导申请' }}</h2>
    </div>
    
    <!-- 教师视角：显示待处理的指导申请和已接受的指导团队 -->
    <div v-if="isTeacher" v-loading="loading">
      <!-- 待处理的指导申请 -->
      <div class="pending-section" v-if="pendingApplications.length > 0">
        <h3>待处理的指导申请</h3>
        <div class="applications-list">
          <div v-for="app in pendingApplications" :key="app.relationId" class="application-card">
            <div class="app-header">
              <div class="app-info">
                <h4>{{ app.teamName }}</h4>
                <el-tag type="warning" size="small">待处理</el-tag>
              </div>
              <div class="app-time">{{ formatDate(app.applyTime) }}</div>
            </div>
            <div class="app-detail">
              <div class="detail-row">
                <span class="label">竞赛名称：</span>
                <span>{{ app.competitionTitle }}</span>
              </div>
              <div class="detail-row">
                <span class="label">队长：</span>
                <span>{{ app.leaderName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">申请理由：</span>
                <span>{{ app.applyReason || '无' }}</span>
              </div>
            </div>
            <div class="app-actions">
              <el-button type="success" size="small" :loading="processingId === app.relationId" @click="handleGuidance(app.relationId, 1)">
                接受
              </el-button>
              <el-button type="danger" size="small" :loading="processingId === app.relationId" @click="rejectGuidance(app)">
                拒绝
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 已接受的指导团队 -->
      <div class="accepted-section" v-if="guidanceTeams.length > 0">
        <h3>已指导的团队</h3>
        <div class="teams-list">
          <el-card v-for="team in guidanceTeams" :key="team.teamId" class="team-card">
            <div class="team-header">
              <h3>{{ team.teamName }}</h3>
              <el-tag type="success">已接受指导</el-tag>
            </div>
            <div class="team-detail">
              <div class="detail-item">
                <span class="label">竞赛名称：</span>
                <span>{{ team.competitionTitle }}</span>
              </div>
              <div class="detail-item">
                <span class="label">队长：</span>
                <span>{{ team.leaderName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">团队人数：</span>
                <span>{{ team.memberCount }}人</span>
              </div>
            </div>
            <div class="team-actions">
              <el-button type="primary" link @click="viewTeam(team.teamId)">查看团队详情</el-button>
            </div>
          </el-card>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="pendingApplications.length === 0 && guidanceTeams.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无指导申请和指导团队" />
      </div>
    </div>
    
    <!-- 学生视角：指导申请记录 -->
    <div v-else class="applications-list" v-loading="loading">
      <div v-for="app in applications" :key="app.relationId" class="application-card">
        <div class="app-header">
          <div class="app-title">
            <h3>{{ app.teamName }}</h3>
            <el-tag :type="getStatusType(app.statusCode)" size="small">
              {{ app.status }}
            </el-tag>
          </div>
          <div class="app-time">{{ formatDate(app.createTime) }}</div>
        </div>
        
        <div class="app-detail">
          <div class="detail-row">
            <span class="label">申请教师：</span>
            <span class="value">{{ app.teacherName }}</span>
          </div>
          <div class="detail-row">
            <span class="label">申请理由：</span>
            <span class="value">{{ app.applyReason || '无' }}</span>
          </div>
          <div v-if="app.teacherComment" class="detail-row">
            <span class="label">教师回复：</span>
            <span class="value comment">{{ app.teacherComment }}</span>
          </div>
        </div>
        
        <div class="app-footer" v-if="app.statusCode === 0">
          <el-button type="danger" size="small" :loading="cancellingId === app.relationId" @click="cancelApplication(app.relationId)">
            <el-icon><Close /></el-icon> 取消申请
          </el-button>
        </div>
      </div>
      
      <div v-if="applications.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无指导申请记录">
          <el-button type="primary" @click="goToTeams">去我的团队申请指导</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { guidanceApi } from '@/api/guidance'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const processingId = ref(null)
const cancellingId = ref(null)
const guidanceTeams = ref([])
const applications = ref([])
const pendingApplications = ref([])

const isTeacher = computed(() => userStore.isTeacher)
const isStudent = computed(() => userStore.isStudent)

const getStatusType = (statusCode) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[statusCode] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取教师待处理的指导申请
const fetchPendingApplications = async () => {
  if (!isTeacher.value) return
  
  try {
    const res = await guidanceApi.getPendingApplications()
    if (res.code === 200) {
      pendingApplications.value = res.data || []
      console.log('待处理指导申请:', pendingApplications.value)
    }
  } catch (error) {
    console.error('获取待处理指导申请失败', error)
  }
}

// 获取教师已接受的指导团队
const fetchGuidanceTeams = async () => {
  if (!isTeacher.value) return
  
  try {
    const res = await guidanceApi.getMyGuidanceTeams()
    if (res.code === 200) {
      guidanceTeams.value = res.data || []
      console.log('指导团队:', guidanceTeams.value)
    }
  } catch (error) {
    console.error('获取指导团队失败', error)
  }
}

// 获取学生指导申请
const fetchApplications = async () => {
  if (!isStudent.value) return
  
  loading.value = true
  try {
    const res = await guidanceApi.getMyApplications()
    if (res.code === 200) {
      applications.value = res.data || []
      console.log('指导申请:', applications.value)
    }
  } catch (error) {
    console.error('获取指导申请失败', error)
  } finally {
    loading.value = false
  }
}

// 处理指导申请（教师）
const handleGuidance = async (relationId, action) => {
  processingId.value = relationId
  try {
    const res = await guidanceApi.handle({
      relationId,
      action,
      comment: ''
    })
    if (res.code === 200) {
      ElMessage.success(action === 1 ? '已接受指导申请' : '已拒绝指导申请')
      // 刷新列表
      fetchPendingApplications()
      fetchGuidanceTeams()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processingId.value = null
  }
}

// 拒绝指导申请（带理由）
const rejectGuidance = async (app) => {
  try {
    const { value: comment } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝指导申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    processingId.value = app.relationId
    const res = await guidanceApi.handle({
      relationId: app.relationId,
      action: 2,
      comment: comment || ''
    })
    if (res.code === 200) {
      ElMessage.success('已拒绝指导申请')
      fetchPendingApplications()
      fetchGuidanceTeams()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 用户取消
  } finally {
    processingId.value = null
  }
}

// 取消申请（学生）
const cancelApplication = async (relationId) => {
  ElMessageBox.confirm('确定要取消该指导申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    cancellingId.value = relationId
    try {
      const res = await guidanceApi.cancelApplication(relationId)
      if (res.code === 200) {
        ElMessage.success('已取消申请')
        fetchApplications()
      } else {
        ElMessage.error(res.message || '取消申请失败')
      }
    } catch (error) {
      console.error('取消申请失败', error)
      ElMessage.error('取消申请失败')
    } finally {
      cancellingId.value = null
    }
  }).catch(() => {})
}

const viewTeam = (teamId) => {
  router.push(`/home/team/${teamId}`)
}

const goToTeams = () => {
  router.push('/home/my-teams')
}

onMounted(() => {
  if (isTeacher.value) {
    fetchPendingApplications()
    fetchGuidanceTeams()
  } else {
    fetchApplications()
  }
})
</script>

<style scoped lang="scss">
.my-guidance-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}

// 待处理区域标题
.pending-section h3,
.accepted-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #1e3c72;
}

.pending-section {
  margin-bottom: 32px;
}

// 申请卡片样式
.application-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  .app-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;
    
    .app-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0;
      }
    }
    
    .app-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .app-detail {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
    
    .detail-row {
      display: flex;
      margin-bottom: 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        width: 80px;
        font-size: 13px;
        color: #999;
        flex-shrink: 0;
      }
      
      span:not(.label) {
        font-size: 14px;
        color: #333;
      }
    }
  }
  
  .app-actions {
    text-align: right;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

// 团队卡片样式
.team-card {
  margin-bottom: 20px;
  border-radius: 12px;
  
  .team-header {
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
  
  .team-detail {
    display: flex;
    flex-wrap: wrap;
    gap: 24px;
    padding: 12px 0;
    border-top: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
    
    .detail-item {
      .label {
        font-size: 13px;
        color: #999;
      }
      
      span:not(.label) {
        font-size: 14px;
        color: #333;
        margin-left: 4px;
      }
    }
  }
  
  .team-actions {
    margin-top: 12px;
    text-align: right;
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

// 响应式
@media (max-width: 768px) {
  .my-guidance-container {
    padding: 16px;
  }
  
  .application-card {
    padding: 16px;
  }
  
  .app-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .app-detail .detail-row {
    flex-direction: column;
    
    .label {
      width: auto;
      margin-bottom: 4px;
    }
  }
  
  .team-detail {
    flex-direction: column;
    gap: 12px;
  }
}
</style>