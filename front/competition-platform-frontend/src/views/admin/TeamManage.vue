<template>
  <div class="team-manage-container">
    <div class="page-header">
      <h2>团队管理</h2>
    </div>
    
    <div class="filter-bar">
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索团队名称" 
        clearable 
        style="width: 240px" 
        @clear="fetchTeams" 
        @keyup.enter="fetchTeams"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select 
        v-model="searchStatus" 
        placeholder="团队状态" 
        clearable 
        style="width: 120px" 
        @change="fetchTeams"
      >
        <el-option label="全部" :value="null" />
        <el-option label="招募中" :value="0" />
        <el-option label="已满员" :value="1" />
        <el-option label="已解散" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchTeams">搜索</el-button>
    </div>
    
    <el-table :data="teams" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="teamName" label="团队名称" min-width="150" />
      <el-table-column prop="competitionTitle" label="所属竞赛" min-width="200" />
      <el-table-column label="队长" width="120">
        <template #default="{ row }">
          {{ row.leaderName }}
        </template>
      </el-table-column>
      <el-table-column label="人数" width="100">
        <template #default="{ row }">
          {{ row.currentMembers }}/{{ row.maxMembers }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewTeam(row.id)">查看详情</el-button>
          <el-button 
            v-if="row.status !== 2" 
            type="danger" 
            link 
            @click="disbandTeam(row)"
          >
            解散
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchTeams"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { teamApi } from '@/api/team'

const router = useRouter()
const loading = ref(false)
const teams = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchStatus = ref(null)

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '招募中', 1: '已满员', 2: '已解散' }
  return texts[status] || '未知'
}

const formatDateTime = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 从后端获取真实团队数据
const fetchTeams = async () => {
  loading.value = true
  try {
    // 调用后端接口获取所有团队（管理员）
    const res = await teamApi.getAdminTeamList({
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value,
      status: searchStatus.value
    })
    if (res.code === 200) {
      teams.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('获取到团队数据:', teams.value)
    } else {
      ElMessage.error(res.message || '获取团队列表失败')
    }
  } catch (error) {
    console.error('获取团队列表失败', error)
    ElMessage.error('获取团队列表失败，请检查网络连接')
    teams.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const viewTeam = (teamId) => {
  router.push(`/home/team/${teamId}`)
}

const disbandTeam = async (team) => {
  ElMessageBox.confirm(`确定要解散团队 "${team.teamName}" 吗？此操作不可恢复！`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await teamApi.disbandTeam(team.id)
      if (res.code === 200) {
        ElMessage.success('团队已解散')
        fetchTeams()  // 刷新列表
      } else {
        ElMessage.error(res.message || '解散失败')
      }
    } catch (error) {
      console.error('解散失败:', error)
      ElMessage.error('解散失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchTeams()
})
</script>

<style scoped lang="scss">
.team-manage-container {
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

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>