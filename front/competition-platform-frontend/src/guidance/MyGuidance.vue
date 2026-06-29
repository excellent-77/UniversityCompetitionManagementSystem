<template>
  <div class="my-guidance-container">
    <div class="page-header">
      <h2>{{ isTeacher ? '我的指导团队' : '我的指导申请' }}</h2>
    </div>
    
    <!-- 教师视角：指导的团队 -->
    <div v-if="isTeacher" class="guidance-teams" v-loading="loading">
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
      
      <div v-if="guidanceTeams.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无指导团队" />
      </div>
    </div>
    
    <!-- 学生视角：指导申请记录 -->
    <div v-else class="applications-list" v-loading="loading">
      <el-card v-for="app in applications" :key="app.relationId" class="application-card">
        <div class="app-header">
          <h3>{{ app.teamName }}</h3>
          <el-tag :type="getStatusType(app.statusCode)" size="small">{{ app.status }}</el-tag>
        </div>
        <div class="app-detail">
          <div class="detail-item">
            <span class="label">申请教师：</span>
            <span>{{ app.teacherName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">申请理由：</span>
            <span>{{ app.applyReason || '无' }}</span>
          </div>
          <div v-if="app.teacherComment" class="detail-item">
            <span class="label">教师回复：</span>
            <span class="comment">{{ app.teacherComment }}</span>
          </div>
          <div class="detail-item">
            <span class="label">申请时间：</span>
            <span>{{ formatDate(app.createTime) }}</span>
          </div>
        </div>
        
        <div v-if="app.statusCode === 0" class="app-actions">
          <el-button type="danger" size="small" @click="cancelApplication(app.relationId)">取消申请</el-button>
        </div>
      </el-card>
      
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
import { useUserStore } from '@/store/user'
import { guidanceApi } from '@/api/guidance'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const guidanceTeams = ref([])
const applications = ref([])

const isTeacher = computed(() => userStore.isTeacher)

const getStatusType = (statusCode) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[statusCode] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchGuidanceTeams = async () => {
  loading.value = true
  try {
    const res = await guidanceApi.getMyGuidanceTeams()
    if (res.code === 200) {
      guidanceTeams.value = res.data
    }
  } catch (error) {
    console.error('获取指导团队失败', error)
  } finally {
    loading.value = false
  }
}

const fetchApplications = async () => {
  loading.value = true
  try {
    const res = await guidanceApi.getMyApplications()
    if (res.code === 200) {
      applications.value = res.data
    }
  } catch (error) {
    console.error('获取指导申请失败', error)
  } finally {
    loading.value = false
  }
}

const viewTeam = (teamId) => {
  router.push(`/team/${teamId}`)
}

const goToTeams = () => {
  router.push('/my-teams')
}

const cancelApplication = async (relationId) => {
  // 取消申请功能（需要后端支持）
  ElMessage.info('功能开发中')
}

onMounted(() => {
  if (isTeacher.value) {
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
  }
}

.team-card, .application-card {
  margin-bottom: 20px;
  border-radius: 12px;
  
  .team-header, .app-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .team-detail, .app-detail {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
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
        margin-left: 8px;
      }
      
      .comment {
        color: #e6a23c;
      }
    }
  }
  
  .team-actions, .app-actions {
    margin-top: 12px;
    text-align: right;
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}
</style>