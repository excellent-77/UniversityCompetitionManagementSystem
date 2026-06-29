<template>
  <div class="my-teams-container">
    <div class="page-header">
      <h2>我的团队</h2>
      <el-button type="primary" @click="goToCompetitions">
        <el-icon><Plus /></el-icon> 加入/创建团队
      </el-button>
    </div>
    
    <div class="teams-list" v-loading="loading">
      <el-card v-for="team in teams" :key="team.teamId" class="team-card">
        <div class="team-header">
          <div class="team-info">
            <h3>{{ team.teamName }}</h3>
            <el-tag :type="team.role === '队长' ? 'danger' : 'info'" size="small">
              {{ team.role }}
            </el-tag>
            <el-tag :type="getTeamStatusType(team.status)" size="small">
              {{ team.statusText }}
            </el-tag>
            <el-tag v-if="team.registered" type="success" size="small">
              已报名
            </el-tag>
          </div>
          <div class="team-actions">
            <el-button type="primary" link @click="viewTeam(team.teamId)">
              <el-icon><View /></el-icon> 查看详情
            </el-button>
            <el-button 
              v-if="team.role === '队长' && !team.registered && team.competitionId" 
              type="success" 
              link 
              @click="registerCompetition(team)"
            >
              <el-icon><Edit /></el-icon> 报名竞赛
            </el-button>
            <el-button 
              v-if="team.role !== '队长' && !team.registered" 
              type="warning" 
              link 
              @click="quitTeam(team.teamId)"
            >
              <el-icon><SwitchButton /></el-icon> 退出团队
            </el-button>
            <span v-if="team.role !== '队长' && team.registered" class="registered-hint">
              已报名，无法退出
            </span>
            <el-button v-if="team.role === '队长'" type="info" link @click="viewApplications(team.teamId)">
              <el-icon><Message /></el-icon> 查看申请
            </el-button>
          </div>
        </div>
        
        <div class="team-detail">
          <div class="detail-item">
            <span class="label">所属竞赛：</span>
            <span class="value">{{ team.competitionTitle }}</span>
          </div>
          <div class="detail-item">
            <span class="label">队长：</span>
            <span class="value">{{ team.leaderName || '未知' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">团队人数：</span>
            <span class="value">{{ team.memberCount }}/{{ team.maxMembers }}</span>
          </div>
        </div>
        
        <div class="team-description" v-if="team.description">
          <p>{{ team.description }}</p>
        </div>
      </el-card>
      
      <div v-if="teams.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无团队，快去创建或加入团队吧！">
          <el-button type="primary" @click="goToCompetitions">查看竞赛</el-button>
        </el-empty>
      </div>
    </div>
    
    <!-- 申请列表对话框 -->
    <el-dialog v-model="showApplicationsDialog" title="团队加入申请" width="500px">
      <div v-loading="applicationsLoading">
        <div v-for="app in applications" :key="app.teamMemberId" class="application-item">
          <div class="app-info">
            <span class="app-name">{{ app.realName }}</span>
            <span class="app-username">{{ app.username }}</span>
            <div class="app-time">{{ formatDate(app.applyTime) }}</div>
          </div>
          <div class="app-actions">
            <el-button type="success" size="small" :loading="processingId === app.teamMemberId" @click="handleApplication(app.teamMemberId, 1)">
              同意
            </el-button>
            <el-button type="danger" size="small" :loading="processingId === app.teamMemberId" @click="rejectApplication(app)">
              拒绝
            </el-button>
          </div>
        </div>
        <div v-if="applications.length === 0" class="empty-apps">暂无待处理申请</div>
      </div>
    </el-dialog>
    
    <!-- 报名竞赛对话框 -->
    <el-dialog v-model="showRegisterDialog" title="报名竞赛" width="500px">
      <div class="register-info">
        <p>团队：<strong>{{ selectedTeam?.teamName }}</strong></p>
        <p>当前人数：{{ selectedTeam?.memberCount }}/{{ selectedTeam?.maxMembers }}</p>
        <el-divider />
        <p>请确认团队信息无误后报名</p>
      </div>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="confirmRegister">
          确认报名
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teamApi } from '@/api/team'
import { competitionApi } from '@/api/competition'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const applicationsLoading = ref(false)
const registering = ref(false)
const processingId = ref(null)
const teams = ref([])
const showApplicationsDialog = ref(false)
const showRegisterDialog = ref(false)
const currentTeamId = ref(null)
const selectedTeam = ref(null)
const applications = ref([])

const getTeamStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取我的团队
const fetchMyTeams = async () => {
  loading.value = true
  try {
    const res = await teamApi.getMyTeams()
    if (res.code === 200) {
      teams.value = res.data || []
      console.log('我的团队:', teams.value)
    }
  } catch (error) {
    console.error('获取我的团队失败', error)
    ElMessage.error('获取团队信息失败')
  } finally {
    loading.value = false
  }
}

// 查看团队详情
const viewTeam = (teamId) => {
  router.push(`/home/team/${teamId}`)
}

// 退出团队
const quitTeam = async (teamId) => {
  const team = teams.value.find(t => t.teamId === teamId)
  if (team && team.registered) {
    ElMessage.warning('该团队已报名竞赛，无法退出')
    return
  }
  
  ElMessageBox.confirm('确定要退出该团队吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await teamApi.quitTeam(teamId)
      if (res.code === 200) {
        ElMessage.success('已退出团队')
        fetchMyTeams()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 报名竞赛
const registerCompetition = (team) => {
  if (!team.competitionId) {
    ElMessage.warning('该团队没有关联竞赛，无法报名')
    return
  }
  if (team.registered) {
    ElMessage.warning('该团队已报名此竞赛，请勿重复报名')
    return
  }
  selectedTeam.value = team
  showRegisterDialog.value = true
}

// 确认报名
const confirmRegister = async () => {
  if (!selectedTeam.value) return
  
  if (selectedTeam.value.registered) {
    ElMessage.warning('该团队已报名此竞赛，请勿重复报名')
    showRegisterDialog.value = false
    return
  }
  
  registering.value = true
  try {
    const res = await competitionApi.register({
      competitionId: selectedTeam.value.competitionId,
      teamId: selectedTeam.value.teamId
    })
    if (res.code === 200) {
      ElMessage.success('报名成功')
      selectedTeam.value.registered = true
      showRegisterDialog.value = false
      await fetchMyTeams()
    } else {
      ElMessage.warning(res.message || '报名失败')
      if (res.message && res.message.includes('已报名')) {
        selectedTeam.value.registered = true
        showRegisterDialog.value = false
        await fetchMyTeams()
      }
    }
  } catch (error) {
    console.error('报名失败', error)
    const errorMsg = error.response?.data?.message || error.message || '报名失败'
    if (errorMsg.includes('已报名')) {
      ElMessage.warning('该团队已报名此竞赛，请勿重复报名')
      selectedTeam.value.registered = true
      showRegisterDialog.value = false
      await fetchMyTeams()
    } else {
      ElMessage.warning(errorMsg)
    }
  } finally {
    registering.value = false
  }
}

// 查看申请列表
const viewApplications = async (teamId) => {
  currentTeamId.value = teamId
  applicationsLoading.value = true
  showApplicationsDialog.value = true
  try {
    const res = await teamApi.getPendingApplications(teamId)
    if (res.code === 200) {
      applications.value = res.data
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
  } finally {
    applicationsLoading.value = false
  }
}

// 处理申请（同意）
const handleApplication = async (teamMemberId, action) => {
  processingId.value = teamMemberId
  try {
    const res = await teamApi.handleApply({
      teamMemberId,
      action
    })
    if (res.code === 200) {
      ElMessage.success(res.message)
      viewApplications(currentTeamId.value)
      fetchMyTeams()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processingId.value = null
  }
}

// 拒绝申请（带理由）
const rejectApplication = async (app) => {
  try {
    const { value: comment } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    processingId.value = app.teamMemberId
    const res = await teamApi.handleApply({
      teamMemberId: app.teamMemberId,
      action: 2,
      comment: comment || ''
    })
    if (res.code === 200) {
      ElMessage.success('已拒绝申请')
      viewApplications(currentTeamId.value)
      fetchMyTeams()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 用户取消
  } finally {
    processingId.value = null
  }
}

// 跳转到竞赛页面
const goToCompetitions = () => {
  router.push('/home/competitions')
}

onMounted(() => {
  fetchMyTeams()
})
</script>

<style scoped lang="scss">
.my-teams-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}

.team-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  .team-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;
    
    .team-info {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
      
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin: 0;
      }
    }
    
    .team-actions {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      align-items: center;
      
      .registered-hint {
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .team-detail {
    display: flex;
    gap: 32px;
    margin-bottom: 12px;
    flex-wrap: wrap;
    
    .detail-item {
      .label {
        font-size: 13px;
        color: #999;
      }
      
      .value {
        font-size: 14px;
        color: #333;
        margin-left: 4px;
      }
    }
  }
  
  .team-description {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    
    p {
      color: #666;
      font-size: 14px;
      line-height: 1.5;
      margin: 0;
    }
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.application-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  
  .app-info {
    .app-name {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }
    
    .app-username {
      font-size: 12px;
      color: #999;
      margin-left: 8px;
    }
    
    .app-time {
      font-size: 11px;
      color: #ccc;
      margin-top: 4px;
    }
  }
  
  .app-actions {
    display: flex;
    gap: 8px;
  }
}

.empty-apps {
  text-align: center;
  padding: 40px;
  color: #999;
}

.register-info {
  text-align: center;
  padding: 10px;
  
  p {
    margin: 10px 0;
  }
  
  strong {
    color: #1e3c72;
  }
}
</style>