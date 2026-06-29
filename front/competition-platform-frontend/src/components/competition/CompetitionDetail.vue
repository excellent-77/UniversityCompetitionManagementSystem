<template>
  <div class="competition-detail">
    <div class="detail-header">
      <h1>{{ competition.title }}</h1>
      <el-tag :type="getStatusType(competition.status)" size="large">
        {{ getStatusText(competition.status) }}
      </el-tag>
    </div>
    
    <div class="detail-info">
      <div class="info-grid">
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
    
    <div class="action-buttons" v-if="showActions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const props = defineProps({
  competition: {
    type: Object,
    required: true
  }
})

const userStore = useUserStore()

const showActions = computed(() => {
  return userStore.isStudent && props.competition?.status === 1
})

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
</script>

<style scoped lang="scss">
.competition-detail {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
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
  margin-bottom: 24px;
  
  .info-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;
    
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

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}
</style>