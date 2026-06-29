<template>
  <div class="competition-list-container">
    <div class="page-header">
      <h2>竞赛列表</h2>
      <el-button v-if="canCreate" type="primary" @click="goToCreate">
        <el-icon><Plus /></el-icon> 发布竞赛
      </el-button>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="searchParams.keyword" placeholder="搜索竞赛名称" clearable style="width: 240px" @clear="handleSearch" @keyup.enter="handleSearch">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="searchParams.status" placeholder="竞赛状态" clearable style="width: 120px" @change="handleSearch">
        <el-option label="未开始" :value="0" />
        <el-option label="报名中" :value="1" />
        <el-option label="进行中" :value="2" />
        <el-option label="已结束" :value="3" />
      </el-select>
      <el-select v-model="searchParams.category" placeholder="竞赛类别" clearable style="width: 140px" @change="handleSearch">
        <el-option label="学科竞赛" value="学科竞赛" />
        <el-option label="创新创业" value="创新创业" />
        <el-option label="程序设计" value="程序设计" />
        <el-option label="数学建模" value="数学建模" />
        <el-option label="人工智能" value="人工智能" />
        <el-option label="英语竞赛" value="英语竞赛" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>
    
    <div class="competition-grid" v-loading="loading">
      <el-card v-for="item in competitionList" :key="item.id" class="competition-card" shadow="hover" @click="goToDetail(item.id)">
        <div class="card-header">
          <h3>{{ item.title }}</h3>
          <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
        </div>
        <p class="description">{{ item.description }}</p>
        <div class="info">
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>报名：{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</span>
          </div>
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ item.location || '待定' }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>{{ item.minTeamMembers }}-{{ item.maxTeamMembers }}人/队</span>
          </div>
        </div>
        <div class="card-footer">
          <el-tag v-for="skill in parseSkills(item.requiredSkills)" :key="skill" size="small" type="info" class="skill-tag">
            {{ skill }}
          </el-tag>
        </div>
      </el-card>
    </div>
    
    <div v-if="competitionList.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无竞赛信息" />
    </div>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchCompetitions"
        @current-change="fetchCompetitions"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { competitionApi } from '@/api/competition'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const competitionList = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

// 从路由参数获取初始状态，处理 NaN 的情况
const getInitialStatus = () => {
  const statusParam = route.query.status
  if (statusParam === undefined || statusParam === null || statusParam === '') {
    return null
  }
  const status = parseInt(statusParam)
  // 如果是 NaN 或者不是有效的状态值，返回 null
  if (isNaN(status) || status < 0 || status > 3) {
    return null
  }
  return status
}

const searchParams = ref({
  keyword: '',
  status: getInitialStatus(),
  category: null
})

const canCreate = computed(() => {
  const role = userStore.user?.role
  return role === 0 || role === 1
})

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '未开始', 1: '报名中', 2: '进行中', 3: '已结束' }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return '待定'
  return dayjs(date).format('MM-DD')
}

const parseSkills = (skills) => {
  if (!skills) return []
  try {
    return JSON.parse(skills)
  } catch {
    return skills.split(',').filter(s => s.trim())
  }
}

const fetchCompetitions = async () => {
  loading.value = true
  try {
    // 构建请求参数，过滤掉无效值
    const params = {
      page: page.value,
      size: size.value
    }
    
    if (searchParams.value.keyword) {
      params.keyword = searchParams.value.keyword
    }
    if (searchParams.value.status !== null && searchParams.value.status !== undefined) {
      params.status = searchParams.value.status
    }
    if (searchParams.value.category) {
      params.category = searchParams.value.category
    }
    
    const res = await competitionApi.getList(params)
    if (res.code === 200) {
      competitionList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取竞赛列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchCompetitions()
}

const goToDetail = (id) => {
  router.push(`/home/competition/${id}`)
}

const goToCreate = () => {
  router.push('/home/competition/create')
}

// 监听路由参数变化
const watchRouteQuery = () => {
  const newStatus = getInitialStatus()
  if (newStatus !== searchParams.value.status) {
    searchParams.value.status = newStatus
    fetchCompetitions()
  }
}

// 监听路由变化
import { watch } from 'vue'
watch(() => route.query.status, () => {
  watchRouteQuery()
})

onMounted(() => {
  fetchCompetitions()
})
</script>

<style scoped lang="scss">
.competition-list-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
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
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.competition-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.competition-card {
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      flex: 1;
      margin-right: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .description {
    font-size: 14px;
    color: #666;
    margin-bottom: 16px;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 42px;
  }
  
  .info {
    margin-bottom: 16px;
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #999;
      margin-bottom: 8px;
      
      .el-icon {
        font-size: 14px;
      }
    }
  }
  
  .card-footer {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .skill-tag {
      background: #f0f2f5;
      border: none;
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
</style>