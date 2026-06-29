<template>
  <div class="team-detail-container" v-loading="loading">
    <div class="back-btn">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
    </div>
    
    <div v-if="team" class="detail-card">
      <div class="detail-header">
        <div class="team-info">
          <h1>{{ team.teamName }}</h1>
          <el-tag :type="getTeamStatusType(team.status)" size="large">
            {{ getTeamStatusText(team.status) }}
          </el-tag>
          <el-tag type="info" size="large">
            {{ team.currentMembers }}/{{ team.maxMembers }}人
          </el-tag>
        </div>
        <div class="team-meta">
          <div class="meta-item">
            <el-icon><Trophy /></el-icon>
            <span>所属竞赛：{{ competitionTitle }}</span>
          </div>
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>队长：{{ leaderName }}</span>
          </div>
        </div>
      </div>
      
      <div class="team-description" v-if="team.description">
        <h3>团队简介</h3>
        <p>{{ team.description }}</p>
      </div>
      
      <!-- 团队成员 -->
      <div class="team-members">
        <div class="section-header">
          <h3>团队成员 <span class="count">({{ team.currentMembers }}/{{ team.maxMembers }})</span></h3>
          <el-button v-if="isLeader" type="primary" size="small" @click="showManageMembers = true">
            <el-icon><Edit /></el-icon> 管理成员
          </el-button>
        </div>
        <div class="members-list">
          <div v-for="member in members" :key="member.userId" class="member-item">
            <div class="member-avatar">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="member-info">
              <div class="member-name">
                {{ member.realName }}
                <el-tag v-if="member.role === 1" type="danger" size="small">队长</el-tag>
                <el-tag v-else type="info" size="small">队员</el-tag>
              </div>
              <div class="member-major" v-if="member.major">
                {{ member.major }} · {{ member.grade }}
              </div>
              <div class="member-skills" v-if="member.skillList && member.skillList.length">
                <el-tag v-for="skill in member.skillList" :key="skill" size="small" type="info">
                  {{ skill }}
                </el-tag>
              </div>
            </div>
            <div class="member-actions" v-if="isLeader && member.role !== 1">
              <el-button type="danger" size="small" @click="removeMember(member.userId)">
                <el-icon><Delete /></el-icon> 移除
              </el-button>
              <el-button type="primary" size="small" @click="transferLeader(member.userId)">
                <el-icon><Share /></el-icon> 转让队长
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 招募需求 -->
      <div class="recruitment-info">
        <div class="section-header">
          <h3>招募需求</h3>
          <div class="header-actions">
            <el-dropdown v-if="isLeader" trigger="click" @command="handleTeamStatusChange">
              <el-button type="primary" size="small">
                修改状态 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="0" :disabled="team.status === 0">招募中</el-dropdown-item>
                  <el-dropdown-item :command="1" :disabled="team.status === 1">已满员</el-dropdown-item>
                  <el-dropdown-item :command="2" :disabled="team.status === 2">已解散</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-if="isLeader" type="success" size="small" @click="showRecruitmentDialog = true">
              <el-icon><Plus /></el-icon> 发布招募
            </el-button>
          </div>
        </div>
        <div v-if="recruitments.length > 0" class="recruitments-list">
          <div v-for="recruit in recruitments" :key="recruit.id" class="recruit-item">
            <div class="recruit-header">
              <div class="recruit-skills">
                <span class="label">需要技能：</span>
                <el-tag v-for="skill in parseSkills(recruit.needSkills)" :key="skill" size="small" type="success">
                  {{ skill }}
                </el-tag>
              </div>
              <div class="recruit-actions" v-if="isLeader">
                <el-button type="danger" size="small" link @click="closeRecruitment(recruit.id)">
                  关闭
                </el-button>
              </div>
            </div>
            <div class="recruit-desc" v-if="recruit.description">
              {{ recruit.description }}
            </div>
            <div class="recruit-count">还需 {{ recruit.needCount }} 人</div>
          </div>
        </div>
        <div v-else class="no-recruit">
          <el-empty description="暂无招募需求" :image-size="60" />
          <el-button v-if="isLeader" type="primary" link @click="showRecruitmentDialog = true">发布招募</el-button>
        </div>
      </div>
      
      <!-- 指导申请区域 -->
      <div class="guidance-section" v-if="guidanceApplications.length > 0 || isLeader">
        <div class="section-header">
          <h3>👨‍🏫 指导申请</h3>
          <el-button v-if="isLeader" type="primary" size="small" @click="goToTeacherRecommend">
            <el-icon><Plus /></el-icon> 申请指导
          </el-button>
        </div>
        
        <div v-if="guidanceApplications.length > 0" class="guidance-list">
          <div v-for="app in guidanceApplications" :key="app.relationId" class="guidance-item">
            <div class="guidance-info">
              <div class="teacher-name">
                <el-icon><User /></el-icon>
                <span>{{ app.teacherName }}</span>
                <el-tag :type="getGuidanceStatusType(app.statusCode)" size="small">
                  {{ getGuidanceStatusText(app.statusCode) }}
                </el-tag>
              </div>
              <div class="apply-reason" v-if="app.applyReason">
                <span class="label">申请理由：</span>
                <span>{{ app.applyReason }}</span>
              </div>
              <div class="apply-time">
                <span class="label">申请时间：</span>
                <span>{{ formatDateTime(app.createTime) }}</span>
              </div>
              <div v-if="app.teacherComment" class="teacher-comment">
                <span class="label">教师回复：</span>
                <span>{{ app.teacherComment }}</span>
              </div>
            </div>
            <!-- 只有教师端（非队长）且状态为待确认时才显示操作按钮 -->
            <div class="guidance-actions" v-if="!isLeader && app.statusCode === 0">
              <el-button type="success" size="small" :loading="processingGuidanceId === app.relationId" @click="handleGuidance(app.relationId, 1)">
                接受
              </el-button>
              <el-button type="danger" size="small" :loading="processingGuidanceId === app.relationId" @click="rejectGuidance(app)">
                拒绝
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-else-if="isLeader" class="no-guidance">
          <el-empty description="暂无指导申请" :image-size="60" />
        </div>
      </div>
      
      <!-- 申请加入按钮 -->
      <div class="action-buttons" v-if="canJoin">
        <el-button type="primary" size="large" :loading="joining" @click="applyToJoin">
          <el-icon><Plus /></el-icon> 申请加入团队
        </el-button>
      </div>
      
      <!-- 不能加入的提示 -->
      <div v-if="!canJoin && !isLeader && userStore.isStudent" class="cannot-join-tip">
        <el-alert 
          v-if="team.status !== 0"
          title="该团队当前不招募成员" 
          type="warning" 
          :closable="false"
          show-icon 
        />
        <el-alert 
          v-else-if="team.currentMembers >= team.maxMembers"
          title="该团队已满员，无法加入" 
          type="warning" 
          :closable="false"
          show-icon 
        />
        <el-alert 
          v-else-if="alreadyInTeam"
          title="您已经是该团队成员" 
          type="info" 
          :closable="false"
          show-icon 
        />
        <el-alert 
          v-else-if="hasApplied"
          title="您已申请加入该团队，请等待队长审核" 
          type="info" 
          :closable="false"
          show-icon 
        />
      </div>
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
        <el-button type="primary" :loading="submitting" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
    
    <!-- 发布招募对话框 -->
    <el-dialog v-model="showRecruitmentDialog" title="发布招募需求" width="500px">
      <el-form :model="recruitmentForm" label-width="100px">
        <el-form-item label="需求技能">
          <el-select v-model="recruitmentForm.needSkills" multiple filterable allow-create placeholder="请选择或输入技能">
            <el-option v-for="skill in skillOptions" :key="skill" :label="skill" :value="skill" />
          </el-select>
          <div class="form-tip">可输入自定义技能，按回车确认</div>
        </el-form-item>
        <el-form-item label="需要人数">
          <el-input-number v-model="recruitmentForm.needCount" :min="1" :max="maxRecruitCount" />
          <span class="tip">（最多可招募 {{ maxRecruitCount }} 人）</span>
        </el-form-item>
        <el-form-item label="招募说明">
          <el-input v-model="recruitmentForm.description" type="textarea" :rows="3" placeholder="请填写招募说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecruitmentDialog = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="publishRecruitment">发布</el-button>
      </template>
    </el-dialog>
    
    <!-- 管理成员对话框 -->
    <el-dialog v-model="showManageMembers" title="管理成员" width="500px">
      <div class="manage-members-list">
        <div v-for="member in members" :key="member.userId" class="manage-member-item">
          <div class="member-avatar-small">
            <el-icon><User /></el-icon>
          </div>
          <div class="member-info-small">
            <div class="member-name">{{ member.realName }}</div>
            <div class="member-role">
              <el-tag v-if="member.role === 1" type="danger" size="small">队长</el-tag>
              <el-tag v-else type="info" size="small">队员</el-tag>
            </div>
          </div>
          <div class="member-actions" v-if="member.role !== 1">
            <el-button type="danger" size="small" @click="removeMember(member.userId)">
              <el-icon><Delete /></el-icon> 移除
            </el-button>
            <el-button type="primary" size="small" @click="transferLeader(member.userId)">
              <el-icon><Share /></el-icon> 设为队长
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showManageMembers = false">关闭</el-button>
          <el-button type="danger" @click="disbandTeam" v-if="isLeader">
            <el-icon><Warning /></el-icon> 解散团队
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { teamApi } from '@/api/team'
import { guidanceApi } from '@/api/guidance'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const joining = ref(false)
const submitting = ref(false)
const publishing = ref(false)
const processingGuidanceId = ref(null)
const team = ref(null)
const members = ref([])
const recruitments = ref([])
const guidanceApplications = ref([])
const competitionTitle = ref('')
const leaderName = ref('')
const showApplyDialog = ref(false)
const showRecruitmentDialog = ref(false)
const showManageMembers = ref(false)
const hasApplied = ref(false)

const teamId = computed(() => route.params.id)
const currentUserId = computed(() => userStore.user?.userId)
const isLeader = computed(() => team.value?.leaderId === currentUserId.value)
const maxRecruitCount = computed(() => {
  if (!team.value) return 1
  return team.value.maxMembers - team.value.currentMembers
})
const alreadyInTeam = computed(() => {
  return members.value.some(m => m.userId === currentUserId.value)
})

// 是否可以申请加入
const canJoin = computed(() => {
  if (!userStore.isStudent) return false
  if (isLeader.value) return false
  if (team.value?.status !== 0) return false
  if (team.value?.currentMembers >= team.value?.maxMembers) return false
  if (alreadyInTeam.value) return false
  if (hasApplied.value) return false
  return true
})

// 指导申请状态样式
const getGuidanceStatusType = (statusCode) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[statusCode] || 'info'
}

const getGuidanceStatusText = (statusCode) => {
  const texts = { 0: '待确认', 1: '已接受', 2: '已拒绝', 3: '已结束' }
  return texts[statusCode] || '未知'
}

const formatDateTime = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const applyForm = ref({ reason: '' })
const recruitmentForm = ref({
  needSkills: [],
  needCount: 1,
  description: ''
})

const skillOptions = ['Java', 'Python', 'Spring Boot', 'Vue', 'React', 'MySQL', '算法', '数据结构', '机器学习', '前端开发', '后端开发', 'UI设计', '产品经理']

const getTeamStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getTeamStatusText = (status) => {
  const texts = { 0: '招募中', 1: '已满员', 2: '已解散' }
  return texts[status] || '未知'
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
  router.go(-1)
}

const goToTeacherRecommend = () => {
  router.push(`/home/teacher-recommend/${team.value.id}`)
}

const applyToJoin = () => {
  showApplyDialog.value = true
}

const submitApply = async () => {
  submitting.value = true
  try {
    const res = await teamApi.applyToTeam({
      teamId: team.value.id,
      applyReason: applyForm.value.reason
    })
    if (res.code === 200) {
      ElMessage.success('申请已提交，请等待队长审核')
      hasApplied.value = true
      showApplyDialog.value = false
    } else {
      ElMessage.error(res.message || '申请失败')
    }
  } catch (error) {
    console.error('申请失败', error)
    const errorMsg = error.response?.data?.message || error.message || '申请失败'
    ElMessage.error(errorMsg)
  } finally {
    submitting.value = false
  }
}

// 修改团队状态
const handleTeamStatusChange = async (status) => {
  try {
    const res = await teamApi.updateTeamStatus(team.value.id, status)
    if (res.code === 200) {
      ElMessage.success(`团队状态已更新为${getTeamStatusText(status)}`)
      fetchTeamDetail()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const publishRecruitment = async () => {
  if (recruitmentForm.value.needSkills.length === 0) {
    ElMessage.warning('请至少选择一个技能')
    return
  }
  
  publishing.value = true
  try {
    const res = await teamApi.addRecruitment({
      teamId: team.value.id,
      needSkills: JSON.stringify(recruitmentForm.value.needSkills),
      needCount: recruitmentForm.value.needCount,
      description: recruitmentForm.value.description
    })
    if (res.code === 200) {
      ElMessage.success('招募需求发布成功')
      showRecruitmentDialog.value = false
      fetchTeamDetail()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const closeRecruitment = async (recruitmentId) => {
  ElMessageBox.confirm('确定要关闭此招募需求吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await teamApi.closeRecruitment(recruitmentId)
      if (res.code === 200) {
        ElMessage.success('已关闭')
        fetchTeamDetail()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const removeMember = async (userId) => {
  const member = members.value.find(m => m.userId === userId)
  ElMessageBox.confirm(`确定要将 ${member?.realName} 移出团队吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await teamApi.removeMember(team.value.id, userId)
      if (res.code === 200) {
        ElMessage.success('已移出团队')
        fetchTeamDetail()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const transferLeader = async (newLeaderId) => {
  const member = members.value.find(m => m.userId === newLeaderId)
  ElMessageBox.confirm(`确定要将队长权限转让给 ${member?.realName} 吗？转让后您将变为普通队员。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await teamApi.transferLeader({
        teamId: team.value.id,
        newLeaderId: newLeaderId
      })
      if (res.code === 200) {
        ElMessage.success('队长转让成功')
        fetchTeamDetail()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const disbandTeam = async () => {
  ElMessageBox.confirm('确定要解散该团队吗？此操作不可恢复！', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await teamApi.disbandTeam(team.value.id)
      if (res.code === 200) {
        ElMessage.success('团队已解散')
        router.push('/home/my-teams')
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const checkIfApplied = async () => {
  try {
    const res = await teamApi.getMyApplications()
    if (res.code === 200) {
      const applications = res.data || []
      hasApplied.value = applications.some(app => app.teamId === team.value?.id)
    }
  } catch (error) {
    console.error('检查申请状态失败', error)
  }
}

// 获取指导申请列表
const fetchGuidanceApplications = async () => {
  try {
    const res = await guidanceApi.getMyApplications()
    if (res.code === 200) {
      const applications = res.data || []
      guidanceApplications.value = applications.filter(app => app.teamId === team.value?.id)
    }
  } catch (error) {
    console.error('获取指导申请失败', error)
  }
}

// 处理指导申请
const handleGuidance = async (relationId, action) => {
  processingGuidanceId.value = relationId
  try {
    const res = await guidanceApi.handle({
      relationId,
      action,
      comment: ''
    })
    if (res.code === 200) {
      ElMessage.success(action === 1 ? '已接受指导申请' : '已拒绝指导申请')
      fetchTeamDetail()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processingGuidanceId.value = null
  }
}

const rejectGuidance = async (app) => {
  try {
    const { value: comment } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝指导申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    processingGuidanceId.value = app.relationId
    const res = await guidanceApi.handle({
      relationId: app.relationId,
      action: 2,
      comment: comment || ''
    })
    if (res.code === 200) {
      ElMessage.success('已拒绝指导申请')
      fetchTeamDetail()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 用户取消
  } finally {
    processingGuidanceId.value = null
  }
}

const fetchTeamDetail = async () => {
  const id = teamId.value
  if (!id || id === 'undefined' || id === 'null') {
    ElMessage.warning('团队ID无效')
    router.push('/home/my-teams')
    return
  }
  
  loading.value = true
  try {
    const res = await teamApi.getTeamDetail(id)
    if (res.code === 200) {
      team.value = res.data
      members.value = res.data.members || []
      recruitments.value = res.data.recruitments || []
      competitionTitle.value = res.data.competitionTitle || ''
      leaderName.value = res.data.leader?.realName || ''
      
      await checkIfApplied()
      await fetchGuidanceApplications()
    } else {
      ElMessage.warning(res.message || '团队不存在或已被解散')
      setTimeout(() => {
        router.push('/home/my-teams')
      }, 1500)
    }
  } catch (error) {
    console.error('获取团队详情失败', error)
    ElMessage.warning('团队可能已被解散，正在返回...')
    setTimeout(() => {
      router.push('/home/my-teams')
    }, 1500)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (teamId.value && teamId.value !== 'undefined') {
    fetchTeamDetail()
  } else {
    ElMessage.warning('参数错误')
    router.push('/home/my-teams')
  }
})
</script>

<style scoped lang="scss">
.team-detail-container {
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
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  
  .team-info {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 16px;
    
    h1 {
      font-size: 28px;
      font-weight: 600;
      color: #333;
      margin: 0;
    }
  }
  
  .team-meta {
    display: flex;
    gap: 24px;
    flex-wrap: wrap;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #666;
      
      .el-icon {
        color: #1e3c72;
      }
    }
  }
}

.team-description {
  margin-bottom: 32px;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 12px;
    color: #333;
  }
  
  p {
    line-height: 1.6;
    color: #666;
    background: #f8f9fa;
    padding: 16px;
    border-radius: 12px;
  }
}

.team-members {
  margin-bottom: 48px;
  
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
      
      .count {
        font-size: 14px;
        color: #999;
        font-weight: normal;
      }
    }
  }
  
  .members-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .member-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 12px;
      transition: all 0.2s;
      
      &:hover {
        background: #f0f2f5;
      }
      
      .member-avatar {
        width: 56px;
        height: 56px;
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
          font-size: 16px;
          color: #333;
          margin-bottom: 6px;
          
          .el-tag {
            margin-left: 8px;
          }
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
      
      .member-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
}

.recruitment-info {
  margin-bottom: 48px;
  
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
    
    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
  
  .recruitments-list {
    .recruit-item {
      background: #f8f9fa;
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 12px;
      
      .recruit-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 8px;
        
        .recruit-skills {
          flex: 1;
          
          .label {
            font-size: 14px;
            color: #666;
          }
          
          .el-tag {
            margin-left: 8px;
          }
        }
      }
      
      .recruit-desc {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
        padding-left: 8px;
        border-left: 3px solid #e6a23c;
      }
      
      .recruit-count {
        font-size: 13px;
        color: #e6a23c;
      }
    }
  }
  
  .no-recruit {
    text-align: center;
    padding: 32px;
    background: #f8f9fa;
    border-radius: 12px;
  }
}

.guidance-section {
  margin-top: 48px;
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
  
  .guidance-list {
    .guidance-item {
      background: #f8f9fa;
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 12px;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      
      .guidance-info {
        flex: 1;
        
        .teacher-name {
          font-weight: 600;
          color: #333;
          margin-bottom: 8px;
          display: flex;
          align-items: center;
          gap: 8px;
          flex-wrap: wrap;
        }
        
        .apply-reason, .apply-time, .teacher-comment {
          font-size: 13px;
          margin-bottom: 4px;
          
          .label {
            color: #999;
          }
        }
        
        .teacher-comment {
          color: #e6a23c;
        }
      }
      
      .guidance-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .no-guidance {
    text-align: center;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 12px;
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.manage-members-list {
  .manage-member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .member-avatar-small {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #e8f0fe;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #1e3c72;
    }
    
    .member-info-small {
      flex: 1;
      
      .member-name {
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }
    }
    
    .member-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

.cannot-join-tip {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .team-detail-container {
    padding: 16px;
  }
  
  .detail-card {
    padding: 20px;
  }
  
  .detail-header .team-info h1 {
    font-size: 22px;
  }
  
  .member-item {
    flex-direction: column;
    text-align: center;
  }
  
  .member-actions {
    justify-content: center;
  }
  
  .recruit-header {
    flex-direction: column;
    
    .recruit-actions {
      margin-top: 8px;
    }
  }
  
  .guidance-actions {
    margin-top: 12px;
    justify-content: flex-start;
  }
}
</style>