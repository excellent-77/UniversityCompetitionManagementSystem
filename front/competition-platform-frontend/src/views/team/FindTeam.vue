<template>
  <div class="find-team-container">
    <div class="page-header">
      <el-button @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <div class="header-title">
        <h2>寻找团队</h2>
        <p>竞赛：{{ competitionTitle }}</p>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索团队名称" clearable style="width: 240px" @clear="fetchTeams" @keyup.enter="fetchTeams">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="fetchTeams">搜索</el-button>
      <el-button type="success" @click="showCreateTeamDialog = true">
        <el-icon><Plus /></el-icon> 创建新团队
      </el-button>
    </div>
    
    <div class="teams-list" v-loading="loading">
      <div v-for="team in teams" :key="team.id" class="team-card">
        <div class="team-header">
          <div class="team-info">
            <h3>{{ team.teamName }}</h3>
            <el-tag :type="getTeamStatusType(team.status)" size="small">
              {{ getTeamStatusText(team.status) }}
            </el-tag>
            <el-tag type="info" size="small">
              {{ team.currentMembers }}/{{ team.maxMembers }}人
            </el-tag>
          </div>
          <div class="team-actions">
            <el-button type="primary" size="small" @click="viewTeamDetail(team.id)">
              查看详情
            </el-button>
            <el-button 
              v-if="team.status === 0 && team.currentMembers < team.maxMembers && !isInTeam(team.id) && !hasApplied(team.id)"
              type="success" 
              size="small" 
              :loading="applyingTeamId === team.id"
              @click="applyToTeam(team.id)"
            >
              申请加入
            </el-button>
            <el-tag v-if="isInTeam(team.id)" type="success" size="small">已加入</el-tag>
            <el-tag v-if="hasApplied(team.id)" type="warning" size="small">已申请</el-tag>
          </div>
        </div>
        
        <div class="team-description">
          <p>{{ team.description || '暂无简介' }}</p>
        </div>
        
        <div class="team-footer">
          <div class="team-leader">
            <el-icon><User /></el-icon>
            <span>队长：{{ team.leaderName }}</span>
          </div>
          <div class="team-skills" v-if="team.recruitments && team.recruitments.length > 0">
            <span class="label">招募需求：</span>
            <el-tag 
              v-for="recruit in team.recruitments" 
              :key="recruit.id" 
              size="small" 
              type="warning"
            >
              需要 {{ parseSkillsText(recruit.needSkills) }} ({{ recruit.needCount }}人)
            </el-tag>
          </div>
        </div>
      </div>
      
      <div v-if="teams.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无团队，快去创建第一个团队吧！">
          <el-button type="primary" @click="showCreateTeamDialog = true">创建团队</el-button>
        </el-empty>
      </div>
    </div>
    
    <div class="pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchTeams"
      />
    </div>
    
    <!-- 申请加入对话框 -->
    <el-dialog v-model="showApplyDialog" title="申请加入团队" width="500px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="申请理由">
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="请简单介绍自己，展示您的技能和优势" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingApply" @click="submitApply">
          提交申请
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { competitionApi } from '@/api/competition'
import { teamApi } from '@/api/team'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const creating = ref(false)
const submittingApply = ref(false)  // 申请提交的加载状态
const applyingTeamId = ref(null)     // 正在申请的团队ID
const teams = ref([])
const competitionTitle = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const showApplyDialog = ref(false)
const showCreateTeamDialog = ref(false)
const teamFormRef = ref()
const currentTeamId = ref(null)

const minMembers = ref(1)
const maxMembers = ref(5)
const competitionId = computed(() => route.params.competitionId)
const userId = computed(() => userStore.user?.userId)
const userTeams = ref([])        // 用户已加入的团队ID列表
const userApplications = ref([]) // 用户已申请的团队ID列表

const applyForm = ref({
  reason: ''
})

const teamForm = ref({
  teamName: '',
  description: '',
  maxMembers: 3
})

const teamRules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 30, message: '团队名称长度为2-30个字符', trigger: 'blur' }
  ]
}

const getTeamStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getTeamStatusText = (status) => {
  const texts = { 0: '招募中', 1: '已满员', 2: '已解散' }
  return texts[status] || '未知'
}

const parseSkillsText = (skills) => {
  if (!skills) return ''
  try {
    const list = JSON.parse(skills)
    return list.join('、')
  } catch {
    return skills
  }
}

const isInTeam = (teamId) => {
  return userTeams.value.includes(teamId)
}

const hasApplied = (teamId) => {
  return userApplications.value.includes(teamId)
}

const goBack = () => {
  router.push(`/home/competition/${competitionId.value}`)
}

const viewTeamDetail = (teamId) => {
  router.push(`/home/team/${teamId}`)
}

const applyToTeam = (teamId) => {
  currentTeamId.value = teamId
  applyForm.value.reason = ''
  showApplyDialog.value = true
}

const submitApply = async () => {
  if (!currentTeamId.value) {
    ElMessage.warning('请选择要申请的团队')
    return
  }
  
  submittingApply.value = true
  try {
    const res = await teamApi.applyToTeam({
      teamId: currentTeamId.value,
      applyReason: applyForm.value.reason
    })
    if (res.code === 200) {
      ElMessage.success('申请已提交，请等待队长审核')
      // 添加到已申请列表
      userApplications.value.push(currentTeamId.value)
      showApplyDialog.value = false
    } else {
      ElMessage.error(res.message || '申请失败')
    }
  } catch (error) {
    console.error('申请失败', error)
    const errorMsg = error.response?.data?.message || error.message || '申请失败'
    ElMessage.error(errorMsg)
  } finally {
    submittingApply.value = false
  }
}

const fetchCompetitionInfo = async () => {
  try {
    const res = await competitionApi.getDetail(competitionId.value)
    if (res.code === 200) {
      competitionTitle.value = res.data.title
      minMembers.value = res.data.minTeamMembers || 1
      maxMembers.value = res.data.maxTeamMembers || 5
      teamForm.value.maxMembers = Math.min(3, maxMembers.value)
    }
  } catch (error) {
    console.error('获取竞赛信息失败', error)
  }
}

const fetchTeams = async () => {
  loading.value = true
  try {
    const res = await teamApi.getTeamsByCompetition(competitionId.value, {
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value
    })
    if (res.code === 200) {
      teams.value = res.data.records || res.data || []
      total.value = res.data.total || teams.value.length
    }
  } catch (error) {
    console.error('获取团队列表失败', error)
    ElMessage.error('获取团队列表失败')
  } finally {
    loading.value = false
  }
}

const fetchUserTeams = async () => {
  try {
    const res = await teamApi.getMyTeams()
    if (res.code === 200) {
      userTeams.value = res.data.map(t => t.teamId)
    }
  } catch (error) {
    console.error('获取用户团队失败', error)
  }
}

const fetchUserApplications = async () => {
  try {
    const res = await teamApi.getMyApplications()
    if (res.code === 200) {
      userApplications.value = res.data.map(a => a.teamId)
    }
  } catch (error) {
    console.error('获取用户申请失败', error)
  }
}

const createTeam = async () => {
  if (!teamFormRef.value) return
  await teamFormRef.value.validate(async (valid) => {
    if (valid) {
      creating.value = true
      try {
        const res = await teamApi.create({
          competitionId: parseInt(competitionId.value),
          teamName: teamForm.value.teamName,
          description: teamForm.value.description,
          maxMembers: teamForm.value.maxMembers
        })
        if (res.code === 200) {
          ElMessage.success('团队创建成功！')
          showCreateTeamDialog.value = false
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

onMounted(() => {
  if (competitionId.value) {
    fetchCompetitionInfo()
    fetchTeams()
    fetchUserTeams()
    fetchUserApplications()
  } else {
    ElMessage.error('参数错误')
    router.push('/home/competitions')
  }
})
</script>

<style scoped lang="scss">
.find-team-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  .back-btn {
    margin-bottom: 16px;
  }
  
  .header-title {
    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
    }
    
    p {
      color: #999;
      font-size: 14px;
    }
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
}

.teams-list {
  .team-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }
    
    .team-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
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
        gap: 8px;
      }
    }
    
    .team-description {
      margin-bottom: 16px;
      
      p {
        color: #666;
        font-size: 14px;
        line-height: 1.5;
      }
    }
    
    .team-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
      
      .team-leader {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #999;
      }
      
      .team-skills {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
        
        .label {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}
</style>