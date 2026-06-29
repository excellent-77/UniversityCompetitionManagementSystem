<template>
  <div class="competition-detail-container" v-loading="loading">
    <div class="back-btn">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
    </div>
    
    <div v-if="competition" class="detail-card">
      <div class="detail-header">
        <h1>{{ competition.title }}</h1>
        <el-tag :type="getStatusType(competition.status)" size="large">{{ getStatusText(competition.status) }}</el-tag>
      </div>
      
      <div class="detail-info">
        <div class="info-row">
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <div>
              <div class="label">报名时间</div>
              <div class="value">{{ formatDateTime(competition.startTime) }} ~ {{ formatDateTime(competition.endTime) }}</div>
            </div>
          </div>
          <div class="info-item">
            <el-icon><Timer /></el-icon>
            <div>
              <div class="label">竞赛时间</div>
              <div class="value">{{ formatDateTime(competition.competitionTime) || '待定' }}</div>
            </div>
          </div>
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <div>
              <div class="label">竞赛地点</div>
              <div class="value">{{ competition.location || '待定' }}</div>
            </div>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <div>
              <div class="label">队伍规模</div>
              <div class="value">{{ competition.minTeamMembers }}-{{ competition.maxTeamMembers }}人/队</div>
            </div>
          </div>
          <div class="info-item">
            <el-icon><Folder /></el-icon>
            <div>
              <div class="label">竞赛类别</div>
              <div class="value">{{ competition.category }}</div>
            </div>
          </div>
          <div class="info-item">
            <el-icon><UserFilled /></el-icon>
            <div>
              <div class="label">发布人</div>
              <div class="value">{{ competition.publishUserName }}</div>
            </div>
          </div>
        </div>
        
        <div class="skills-section">
          <span class="label">所需技能：</span>
          <el-tag v-for="skill in parseSkills(competition.requiredSkills)" :key="skill" size="small" class="skill-tag">
            {{ skill }}
          </el-tag>
        </div>
      </div>
      
      <el-divider />
      
      <div class="detail-content">
        <h3>竞赛详情</h3>
        <div v-html="competition.content" class="content-html"></div>
      </div>
      
      <!-- 已报名提示 -->
      <div v-if="hasRegistered" class="registered-tip">
        <el-alert 
          title="您的团队已报名该竞赛" 
          type="success" 
          :closable="false"
          show-icon 
        />
      </div>
      
      <!-- 创建团队/寻找团队按钮（无团队时显示） -->
      <div class="action-buttons" v-if="isStudent && competition.status === 1 && !hasTeamInCompetition">
        <el-button type="primary" size="large" @click="showCreateTeamDialog = true">
          <el-icon><Plus /></el-icon> 创建团队
        </el-button>
        <el-button type="success" size="large" @click="goToTeams">
          <el-icon><Search /></el-icon> 寻找团队
        </el-button>
      </div>
      
      <!-- 报名参赛按钮（队长且未报名时显示） -->
      <div class="action-buttons" v-if="canShowRegisterButton">
        <el-button type="primary" size="large" :loading="registering" @click="showTeamDialog = true">
          <el-icon><Edit /></el-icon> 报名参赛
        </el-button>
      </div>

      <!-- 已报名提示 - 底部 -->
      <div v-if="hasRegistered" class="registered-footer-tip">
        <el-result
          icon="success"
          title="报名成功"
          sub-title="您的团队已成功报名该竞赛，祝您取得好成绩！"
        >
          <template #extra>
            <el-button type="primary" @click="goToMyTeams">查看我的团队</el-button>
          </template>
        </el-result>
      </div>
    </div>
    
    <!-- 选择团队报名对话框 -->
    <el-dialog v-model="showTeamDialog" title="选择团队报名" width="500px">
      <div class="team-select" v-loading="teamsLoading">
        <div v-if="availableTeams.length === 0" class="no-team-tip">
          <el-empty description="暂无可用团队" :image-size="80">
            <el-button type="primary" @click="goToCreateTeam">去创建团队</el-button>
          </el-empty>
        </div>
        <div v-else>
          <el-radio-group v-model="selectedTeamId">
            <div v-for="team in availableTeams" :key="team.teamId" class="team-option">
              <el-radio :label="team.teamId">
                <div class="team-info">
                  <span class="team-name">{{ team.teamName }}</span>
                  <span class="team-members">{{ team.memberCount }}/{{ team.maxMembers }}人</span>
                  <el-tag v-if="team.role === '队长'" type="danger" size="small">队长</el-tag>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="showTeamDialog = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="submitRegistration" :disabled="!selectedTeamId">
          确认报名
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 创建团队对话框 -->
    <el-dialog v-model="showCreateTeamDialog" title="创建团队" width="500px">
      <el-form :model="teamForm" :rules="teamRules" ref="teamFormRef" label-width="100px">
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="teamForm.teamName" placeholder="请输入团队名称" />
        </el-form-item>
        <el-form-item label="团队简介" prop="description">
          <el-input v-model="teamForm.description" type="textarea" :rows="3" placeholder="请输入团队简介" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxMembers">
          <el-input-number v-model="teamForm.maxMembers" :min="minMembers" :max="maxMembers" />
          <span class="tip">（{{ minMembers }}-{{ maxMembers }}人/队）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTeamDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createTeam">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { competitionApi } from '@/api/competition'
import { teamApi } from '@/api/team'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const creating = ref(false)
const registering = ref(false)
const teamsLoading = ref(false)
const competition = ref(null)
const myTeams = ref([])
const selectedTeamId = ref(null)
const showCreateTeamDialog = ref(false)
const showTeamDialog = ref(false)
const teamFormRef = ref()
const minMembers = ref(1)
const maxMembers = ref(5)
const hasRegistered = ref(false)

const isStudent = computed(() => userStore.isStudent)
const currentUserId = computed(() => userStore.user?.userId)

// 获取用户在该竞赛中已创建的团队
const userTeamsInCompetition = computed(() => {
  return myTeams.value.filter(team => team.competitionId === competition.value?.id)
})

// 是否已有团队在该竞赛中
const hasTeamInCompetition = computed(() => {
  return userTeamsInCompetition.value.length > 0
})

// 可用报名的团队（该竞赛中的团队）
const availableTeams = computed(() => {
  return myTeams.value.filter(team => team.competitionId === competition.value?.id)
})

// 用户是否是队长
const isTeamLeader = computed(() => {
  const leaderTeam = userTeamsInCompetition.value.find(team => team.role === '队长')
  return !!leaderTeam
})

// 是否可以显示报名按钮（只有队长且未报名才能看到）
const canShowRegisterButton = computed(() => {
  if (!isStudent.value) return false
  if (competition.value?.status !== 1) return false
  if (!hasTeamInCompetition.value) return false
  if (!isTeamLeader.value) return false
  if (hasRegistered.value) return false
  return true
})

const goToMyTeams = () => {
  router.push('/home/my-teams')
}

const teamForm = ref({
  teamName: '',
  description: '',
  maxMembers: 3
})

const teamRules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 30, message: '团队名称长度为2-30个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '简介不能超过200个字符', trigger: 'blur' }
  ]
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '未开始', 1: '报名中', 2: '进行中', 3: '已结束' }
  return texts[status] || '未知'
}

const formatDateTime = (date) => {
  if (!date) return '待定'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const parseSkills = (skills) => {
  if (!skills) return []
  try {
    return JSON.parse(skills)
  } catch {
    return skills.split(',').filter(s => s.trim())
  }
}

const goBack = () => {
  router.push('/home/competitions')
}

const goToTeams = () => {
  router.push(`/home/find-team/${competition.value?.id}`)
}

const goToCreateTeam = () => {
  showTeamDialog.value = false
  router.push(`/home/team/create/${competition.value?.id}`)
}

// 获取我的团队列表
const fetchMyTeams = async () => {
  if (!isStudent.value) return
  
  teamsLoading.value = true
  try {
    const res = await teamApi.getMyTeams()
    if (res.code === 200) {
      myTeams.value = res.data || []
      console.log('我的团队:', myTeams.value)
    }
  } catch (error) {
    console.error('获取我的团队失败', error)
  } finally {
    teamsLoading.value = false
  }
}

// 提交报名
const submitRegistration = async () => {
  if (!selectedTeamId.value) {
    ElMessage.warning('请选择要报名的团队')
    return
  }
  
  const selectedTeam = availableTeams.value.find(t => t.teamId === selectedTeamId.value)
  if (selectedTeam?.role !== '队长') {
    ElMessage.warning('只有队长可以报名竞赛')
    showTeamDialog.value = false
    return
  }
  
  if (hasRegistered.value) {
    ElMessage.warning('您的团队已报名，请勿重复报名')
    showTeamDialog.value = false
    return
  }
  
  registering.value = true
  try {
    const res = await competitionApi.register({
      competitionId: competition.value.id,
      teamId: selectedTeamId.value
    })
    if (res.code === 200) {
      ElMessage.success('报名成功')
      hasRegistered.value = true
      showTeamDialog.value = false
      selectedTeamId.value = null
    } else {
      ElMessage.warning(res.message || '报名失败')
      if (res.message && res.message.includes('已报名')) {
        hasRegistered.value = true
        showTeamDialog.value = false
      }
    }
  } catch (error) {
    console.error('报名失败', error)
    const errorMsg = error.response?.data?.message || error.message || '报名失败'
    if (errorMsg.includes('已报名')) {
      ElMessage.warning('您的团队已报名，请勿重复报名')
      hasRegistered.value = true
      showTeamDialog.value = false
    } else if (errorMsg.includes('队长')) {
      ElMessage.warning('只有队长可以报名竞赛')
      showTeamDialog.value = false
    } else {
      ElMessage.warning(errorMsg)
    }
  } finally {
    registering.value = false
  }
}

// 创建团队
const createTeam = async () => {
  if (!teamFormRef.value) return
  await teamFormRef.value.validate(async (valid) => {
    if (valid) {
      creating.value = true
      try {
        const res = await teamApi.create({
          competitionId: competition.value.id,
          teamName: teamForm.value.teamName,
          description: teamForm.value.description,
          maxMembers: teamForm.value.maxMembers
        })
        if (res.code === 200) {
          ElMessage.success('团队创建成功！')
          showCreateTeamDialog.value = false
          await fetchMyTeams()
          router.push('/home/my-teams')
        } else {
          ElMessage.error(res.message)
        }
      } catch (error) {
        console.error('创建团队失败', error)
        ElMessage.error('创建失败，请稍后重试')
      } finally {
        creating.value = false
      }
    }
  })
}

// 获取竞赛详情
const fetchDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await competitionApi.getDetail(id)
    if (res.code === 200) {
      competition.value = res.data
      minMembers.value = competition.value.minTeamMembers || 1
      maxMembers.value = competition.value.maxTeamMembers || 5
      teamForm.value.maxMembers = Math.min(3, maxMembers.value)
    } else {
      ElMessage.error('获取竞赛详情失败')
      router.push('/home/competitions')
    }
  } catch (error) {
    console.error('获取竞赛详情失败', error)
    ElMessage.error('获取竞赛详情失败')
    router.push('/home/competitions')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchDetail()
  await fetchMyTeams()
  // 注意：不再调用 checkRegistrationStatus，避免404错误
  // 报名状态只在报名成功时设置
})
</script>

<style scoped lang="scss">
.competition-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.back-btn {
  margin-bottom: 20px;
}

.detail-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  h1 {
    font-size: 28px;
    font-weight: 600;
    color: #333;
  }
}

.detail-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  
  .info-row {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    margin-bottom: 16px;
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .el-icon {
        font-size: 24px;
        color: #1e3c72;
      }
      
      .label {
        font-size: 12px;
        color: #999;
        margin-bottom: 4px;
      }
      
      .value {
        font-size: 14px;
        color: #333;
        font-weight: 500;
      }
    }
  }
  
  .skills-section {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid #e8eef4;
    
    .label {
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }
    
    .skill-tag {
      background: #e8f0fe;
      border: none;
    }
  }
}

.detail-content {
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #333;
  }
  
  .content-html {
    line-height: 1.8;
    color: #555;
    
    :deep(h3) {
      font-size: 18px;
      margin: 20px 0 12px;
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

.registered-tip {
  margin-top: 20px;
  margin-bottom: 20px;
}

.registered-footer-tip {
  margin-top: 32px;
  padding: 24px;
  background: #f0f9eb;
  border-radius: 16px;
  text-align: center;
  
  :deep(.el-result__title) {
    color: #67c23a;
  }
  
  :deep(.el-result__subtitle) {
    color: #666;
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

.team-select {
  max-height: 400px;
  overflow-y: auto;
  
  .team-option {
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s;
    
    &:hover {
      background: #f8f9fa;
    }
    
    .team-info {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
      
      .team-name {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
      
      .team-members {
        font-size: 13px;
        color: #999;
      }
    }
  }
  
  .no-team-tip {
    padding: 20px;
    text-align: center;
  }
}
</style>