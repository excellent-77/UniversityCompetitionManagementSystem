<template>
  <div class="teacher-recommend-container">
    <div class="page-header">
      <el-button @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <div class="header-title">
        <h2>智能推荐指导教师</h2>
        <p>根据竞赛类别和所需技能，为您推荐最匹配的指导教师</p>
      </div>
    </div>
    
    <div class="recommend-list" v-loading="loading">
      <div v-for="teacher in teachers" :key="teacher.teacherId" class="teacher-card">
        <div class="teacher-header">
          <div class="teacher-avatar">
            <el-icon :size="48"><User /></el-icon>
          </div>
          <div class="teacher-info">
            <div class="teacher-name">
              {{ teacher.teacherName }}
              <el-tag :type="getTitleType(teacher.title)" size="small">{{ teacher.title }}</el-tag>
            </div>
            <div class="teacher-department">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ teacher.department }}</span>
            </div>
          </div>
        </div>
        
        <div class="teacher-detail">
          <div class="detail-row">
            <div class="detail-label">
              <el-icon><DataAnalysis /></el-icon>
              <span>研究方向</span>
            </div>
            <div class="detail-value">
              <div class="research-tags">
                <el-tag 
                  v-for="dir in parseDirections(teacher.researchDirection)" 
                  :key="dir" 
                  size="small" 
                  type="primary"
                >
                  {{ dir }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="detail-row">
            <div class="detail-label">
              <el-icon><UserFilled /></el-icon>
              <span>指导状态</span>
            </div>
            <div class="detail-value">
              <span class="status-text">{{ teacher.currentTeams }}/{{ teacher.maxTeams }} 团队</span>
              <el-progress 
                :percentage="(teacher.currentTeams / teacher.maxTeams) * 100" 
                :show-text="false"
                :stroke-width="6"
                :color="getProgressColor(teacher.currentTeams, teacher.maxTeams)"
              />
            </div>
          </div>
        </div>
        
        <div class="apply-section">
          <el-input 
            v-model="applyReasons[teacher.teacherId]" 
            type="textarea" 
            :rows="2" 
            placeholder="请填写申请理由（可选），例如：我对您的研究方向非常感兴趣，希望能得到您的指导..." 
            class="apply-input"
          />
          <el-button 
            type="primary" 
            :loading="applying && currentTeacherId === teacher.teacherId" 
            @click="applyGuidance(teacher.teacherId)"
            class="apply-btn"
            :disabled="!canApply(teacher.teacherId)"
          >
            <el-icon><Promotion /></el-icon> 
            {{ getButtonText(teacher.teacherId) }}
          </el-button>
          <div v-if="getApplicationStatus(teacher.teacherId) === 'pending'" class="pending-tip">
            <el-icon><Loading /></el-icon> 申请待审核，请耐心等待
          </div>
          <div v-if="getApplicationStatus(teacher.teacherId) === 'accepted'" class="accepted-tip">
            <el-icon><SuccessFilled /></el-icon> 指导申请已通过
          </div>
          <div v-if="getApplicationStatus(teacher.teacherId) === 'rejected'" class="rejected-tip">
            <el-icon><CircleClose /></el-icon> 申请已被拒绝，可重新申请
          </div>
        </div>
      </div>
      
      <div v-if="teachers.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无合适的指导教师，请稍后再试">
          <el-button type="primary" @click="goBack">返回团队详情</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { guidanceApi } from '@/api/guidance'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const applying = ref(false)
const currentTeacherId = ref(null)
const teachers = ref([])
const existingApplications = ref([]) // 存储完整的申请记录 { teacherId, status }

// 使用对象存储每个教师的申请理由
const applyReasons = reactive({})

// 从路由参数获取团队ID
const teamId = computed(() => {
  const id = route.params.teamId
  if (!id || id === 'undefined') {
    console.error('团队ID无效:', id)
    return null
  }
  return parseInt(id)
})

const getTitleType = (title) => {
  const types = {
    '教授': 'danger',
    '副教授': 'warning',
    '讲师': 'primary',
    '助教': 'info'
  }
  return types[title] || 'info'
}

const getProgressColor = (current, max) => {
  if (max === 0) return '#67c23a'
  const percentage = (current / max) * 100
  if (percentage >= 80) return '#f56c6c'
  if (percentage >= 50) return '#e6a23c'
  return '#67c23a'
}

const parseDirections = (directions) => {
  if (!directions) return []
  try {
    return JSON.parse(directions)
  } catch {
    return directions.split(',').filter(d => d.trim())
  }
}

// 获取申请状态
const getApplicationStatus = (teacherId) => {
  const app = existingApplications.value.find(a => a.teacherId === teacherId)
  if (!app) return null
  if (app.status === 0) return 'pending'
  if (app.status === 1) return 'accepted'
  if (app.status === 2) return 'rejected'
  if (app.status === 3) return 'cancelled'
  return null
}

// 是否可以申请
const canApply = (teacherId) => {
  const status = getApplicationStatus(teacherId)
  // 待审核、已通过时不能申请；已拒绝、已取消、无记录时可以申请
  return status !== 'pending' && status !== 'accepted'
}

// 获取按钮文本
const getButtonText = (teacherId) => {
  const status = getApplicationStatus(teacherId)
  if (status === 'pending') return '待审核'
  if (status === 'accepted') return '已通过'
  if (status === 'rejected') return '重新申请'
  if (status === 'cancelled') return '重新申请'
  return '申请指导'
}

const goBack = () => {
  if (teamId.value) {
    router.push(`/home/team/${teamId.value}`)
  } else {
    router.push('/home/my-teams')
  }
}

// 获取已申请的教师记录
const fetchExistingApplications = async () => {
  if (!teamId.value) return
  
  try {
    const res = await guidanceApi.getMyApplications()
    console.log('获取申请记录响应:', res)
    
    if (res.code === 200) {
      let applications = []
      // 兼容多种数据格式
      if (Array.isArray(res.data)) {
        applications = res.data
      } else if (res.data.list) {
        applications = res.data.list
      } else {
        applications = res.data || []
      }
      
      console.log('申请记录数据:', applications)
      
      // 筛选当前团队的申请记录
      const appList = []
      applications.forEach(app => {
        const appTeamId = app.teamId || app.team_id
        if (appTeamId === teamId.value) {
          const teacherId = app.teacherId || app.teacher_id
          const status = app.statusCode !== undefined ? app.statusCode : app.status
          if (teacherId !== undefined && teacherId !== null) {
            appList.push({
              teacherId: teacherId,
              status: status,
              relationId: app.relationId || app.id
            })
          }
        }
      })
      
      existingApplications.value = appList
      console.log('已申请记录:', existingApplications.value)
    }
  } catch (error) {
    console.error('获取已申请记录失败', error)
  }
}

const fetchRecommendTeachers = async () => {
  if (!teamId.value) {
    ElMessage.error('团队ID无效，请返回重试')
    return
  }
  
  loading.value = true
  try {
    const res = await guidanceApi.recommendTeachers(teamId.value)
    console.log('推荐教师响应:', res)
    
    if (res.code === 200) {
      teachers.value = res.data || []
      // 初始化申请理由对象
      teachers.value.forEach(teacher => {
        if (!applyReasons[teacher.teacherId]) {
          applyReasons[teacher.teacherId] = ''
        }
      })
      console.log('推荐教师:', teachers.value)
    } else {
      ElMessage.error(res.message || '获取推荐教师失败')
    }
  } catch (error) {
    console.error('获取推荐教师失败', error)
    if (error.response?.status === 400) {
      ElMessage.error('团队ID无效，请返回重试')
    } else {
      ElMessage.error('获取推荐教师失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

const applyGuidance = async (teacherId) => {
  // 检查团队ID是否有效
  if (!teamId.value) {
    ElMessage.error('团队信息无效，请返回重试')
    return
  }
  
  // 检查是否可以申请
  if (!canApply(teacherId)) {
    const status = getApplicationStatus(teacherId)
    if (status === 'pending') {
      ElMessage.warning('申请正在审核中，请耐心等待')
    } else if (status === 'accepted') {
      ElMessage.warning('指导申请已通过，无需重复申请')
    }
    return
  }
  
  currentTeacherId.value = teacherId
  applying.value = true
  
  const reason = applyReasons[teacherId] || ''
  
  try {
    const res = await guidanceApi.apply({
      teacherId,
      teamId: teamId.value,
      applyReason: reason
    })
    console.log('申请响应:', res)
    
    if (res.code === 200) {
      ElMessage.success(res.message || '申请已发送，等待教师确认')
      
      // 更新本地申请记录
      const existingIndex = existingApplications.value.findIndex(a => a.teacherId === teacherId)
      if (existingIndex >= 0) {
        existingApplications.value[existingIndex].status = 0
      } else {
        existingApplications.value.push({
          teacherId: teacherId,
          status: 0
        })
      }
      // 清空申请理由
      applyReasons[teacherId] = ''
    } else {
      ElMessage.error(res.message || '申请失败，请稍后重试')
    }
  } catch (error) {
    console.error('申请失败:', error)
    
    const errorData = error.response?.data
    const errorMsg = errorData?.message || error.message || '申请失败，请稍后重试'
    
    if (errorMsg.includes('审核中') || errorMsg.includes('正在审核')) {
      ElMessage.warning('申请正在审核中，请耐心等待')
      // 更新状态为待审核
      const existingIndex = existingApplications.value.findIndex(a => a.teacherId === teacherId)
      if (existingIndex >= 0) {
        existingApplications.value[existingIndex].status = 0
      } else {
        existingApplications.value.push({
          teacherId: teacherId,
          status: 0
        })
      }
    } else if (errorMsg.includes('已通过')) {
      ElMessage.warning('指导申请已通过')
      const existingIndex = existingApplications.value.findIndex(a => a.teacherId === teacherId)
      if (existingIndex >= 0) {
        existingApplications.value[existingIndex].status = 1
      }
    } else if (errorMsg.includes('重新') || errorMsg.includes('再次')) {
      ElMessage.success('申请已重新发送')
      const existingIndex = existingApplications.value.findIndex(a => a.teacherId === teacherId)
      if (existingIndex >= 0) {
        existingApplications.value[existingIndex].status = 0
      }
      applyReasons[teacherId] = ''
    } else {
      ElMessage.error(errorMsg)
    }
  } finally {
    applying.value = false
    currentTeacherId.value = null
  }
}

onMounted(() => {
  // 检查团队ID是否有效
  if (!teamId.value || teamId.value === 'undefined') {
    ElMessage.error('团队ID无效，请返回重试')
    setTimeout(() => {
      router.push('/home/my-teams')
    }, 1500)
    return
  }
  
  fetchRecommendTeachers()
  fetchExistingApplications()
})
</script>

<style scoped lang="scss">
.teacher-recommend-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 32px;
  
  .back-btn {
    margin-bottom: 16px;
  }
  
  .header-title {
    text-align: center;
    
    h2 {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 8px;
      color: #333;
      background: linear-gradient(135deg, #1e3c72 0%, #2b4c7c 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    p {
      color: #999;
      font-size: 14px;
    }
  }
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.teacher-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.teacher-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  
  .teacher-avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: linear-gradient(135deg, #e8f0fe 0%, #d4e0f5 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #1e3c72;
  }
  
  .teacher-info {
    flex: 1;
    
    .teacher-name {
      font-size: 20px;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }
    
    .teacher-department {
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

.teacher-detail {
  margin-bottom: 20px;
  
  .detail-row {
    display: flex;
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .detail-label {
      width: 90px;
      display: flex;
      align-items: flex-start;
      gap: 6px;
      font-size: 14px;
      color: #999;
      flex-shrink: 0;
      
      .el-icon {
        font-size: 16px;
      }
    }
    
    .detail-value {
      flex: 1;
      
      .research-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .el-tag {
          background: #f0f7ff;
          border: none;
          color: #1e3c72;
        }
      }
      
      .status-text {
        font-size: 14px;
        color: #333;
        margin-bottom: 8px;
        display: inline-block;
      }
    }
  }
}

.apply-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  margin-top: 8px;
  
  .apply-input {
    margin-bottom: 12px;
    
    :deep(.el-textarea__inner) {
      border-radius: 8px;
      border: 1px solid #e0e0e0;
      transition: all 0.2s;
      
      &:focus {
        border-color: #1e3c72;
        box-shadow: 0 0 0 2px rgba(30, 60, 114, 0.1);
      }
    }
  }
  
  .apply-btn {
    width: 100%;
    background: linear-gradient(135deg, #1e3c72 0%, #2b4c7c 100%);
    border: none;
    height: 40px;
    font-size: 14px;
    
    &:hover {
      background: linear-gradient(135deg, #2b4c7c 0%, #1e3c72 100%);
      transform: translateY(-1px);
    }
    
    &:disabled {
      background: #e0e0e0;
      cursor: not-allowed;
    }
  }
  
  .pending-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    margin-top: 12px;
    font-size: 12px;
    color: #e6a23c;
  }
  
  .accepted-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    margin-top: 12px;
    font-size: 12px;
    color: #67c23a;
  }
  
  .rejected-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    margin-top: 12px;
    font-size: 12px;
    color: #f56c6c;
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
  background: #fff;
  border-radius: 16px;
}

// 响应式
@media (max-width: 768px) {
  .teacher-recommend-container {
    padding: 16px;
  }
  
  .page-header .header-title h2 {
    font-size: 22px;
  }
  
  .teacher-header {
    flex-direction: column;
    text-align: center;
    
    .teacher-info {
      text-align: center;
      
      .teacher-name {
        justify-content: center;
      }
      
      .teacher-department {
        justify-content: center;
      }
    }
  }
  
  .teacher-detail .detail-row {
    flex-direction: column;
    
    .detail-label {
      width: auto;
      margin-bottom: 8px;
    }
  }
  
  .apply-section {
    padding: 12px;
  }
}
</style>