<template>
  <el-card class="competition-card" shadow="hover" @click="handleClick">
    <div class="card-header">
      <h3>{{ competition.title }}</h3>
      <el-tag :type="getStatusType(competition.status)" size="small">
        {{ getStatusText(competition.status) }}
      </el-tag>
    </div>
    
    <p class="description">{{ competition.description }}</p>
    
    <div class="info">
      <div class="info-item">
        <el-icon><Calendar /></el-icon>
        <span>报名：{{ formatDate(competition.startTime) }} ~ {{ formatDate(competition.endTime) }}</span>
      </div>
      <div class="info-item">
        <el-icon><Location /></el-icon>
        <span>{{ competition.location || '待定' }}</span>
      </div>
      <div class="info-item">
        <el-icon><User /></el-icon>
        <span>{{ competition.minTeamMembers }}-{{ competition.maxTeamMembers }}人/队</span>
      </div>
    </div>
    
    <div class="skills">
      <el-tag 
        v-for="skill in parseSkills(competition.requiredSkills)" 
        :key="skill" 
        size="small" 
        type="info"
        class="skill-tag"
      >
        {{ skill }}
      </el-tag>
    </div>
  </el-card>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  competition: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

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

const handleClick = () => {
  emit('click', props.competition.id)
}
</script>

<style scoped lang="scss">
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
  
  .skills {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .skill-tag {
      background: #f0f2f5;
      border: none;
    }
  }
}
</style>