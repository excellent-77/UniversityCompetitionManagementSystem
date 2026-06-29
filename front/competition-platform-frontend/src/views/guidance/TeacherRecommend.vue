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
          >
            <el-icon><Promotion /></el-icon> 申请指导
          </el-button>
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
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { guidanceApi } from '@/api/guidance'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const applying = ref(false)
const currentTeacherId = ref(null)
const teachers = ref([])

// 使用对象存储每个教师的申请理由
const applyReasons = reactive({})

const teamId = route.params.teamId

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

const goBack = () => {
  router.push(`/home/team/${teamId}`)
}

const fetchRecommendTeachers = async () => {
  loading.value = true
  try {
    const res = await guidanceApi.recommendTeachers(teamId)
    if (res.code === 200) {
      teachers.value = res.data
      // 初始化申请理由对象
      teachers.value.forEach(teacher => {
        if (!applyReasons[teacher.teacherId]) {
          applyReasons[teacher.teacherId] = ''
        }
      })
    } else {
      ElMessage.error(res.message || '获取推荐教师失败')
    }
  } catch (error) {
    console.error('获取推荐教师失败', error)
    ElMessage.error('获取推荐教师失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const applyGuidance = async (teacherId) => {
  currentTeacherId.value = teacherId
  applying.value = true
  
  const reason = applyReasons[teacherId] || ''
  
  try {
    const res = await guidanceApi.apply({
      teacherId,
      teamId,
      applyReason: reason
    })
    if (res.code === 200) {
      ElMessage.success('申请已发送，等待教师确认')
      // 清空申请理由
      applyReasons[teacherId] = ''
    } else {
      ElMessage.error(res.message || '申请失败，请稍后重试')
    }
  } catch (error) {
    console.error('申请失败', error)
    const errorMsg = error.response?.data?.message || error.message || '申请失败，请稍后重试'
    ElMessage.error(errorMsg)
  } finally {
    applying.value = false
    currentTeacherId.value = null
  }
}

onMounted(() => {
  fetchRecommendTeachers()
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