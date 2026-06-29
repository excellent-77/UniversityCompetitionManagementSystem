<template>
  <div class="announcement-list-container">
    <div class="page-header">
      <h2>系统公告</h2>
    </div>
    
    <div class="announcement-list" v-loading="loading">
      <div v-for="item in announcements" :key="item.id" class="announcement-item" @click="viewDetail(item)">
        <div class="announcement-header">
          <div class="announcement-title">
            <el-tag v-if="item.isTop === 1" type="danger" size="small" class="top-tag">置顶</el-tag>
            {{ item.title }}
          </div>
          <div class="announcement-time">{{ formatDateTime(item.publishTime) }}</div>
        </div>
        <div class="announcement-summary" v-html="getSummary(item.content)"></div>
      </div>
      
      <div v-if="announcements.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无公告" />
      </div>
    </div>
    
    <div class="pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchAnnouncements"
      />
    </div>
    
    <!-- 公告详情弹窗 -->
    <el-dialog v-model="showDetailDialog" :title="selectedAnnouncement?.title || '公告详情'" width="600px">
      <div class="announcement-detail" v-if="selectedAnnouncement">
        <div class="detail-meta">
          <span>发布人：{{ selectedAnnouncement.publishUserName || '系统管理员' }}</span>
          <span>发布时间：{{ formatDateTime(selectedAnnouncement.publishTime) }}</span>
        </div>
        <div class="detail-content" v-html="selectedAnnouncement.content"></div>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { announcementApi } from '@/api/announcement'
import dayjs from 'dayjs'

const loading = ref(false)
const announcements = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showDetailDialog = ref(false)
const selectedAnnouncement = ref(null)

const formatDateTime = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getSummary = (content) => {
  if (!content) return ''
  // 移除HTML标签，取前100个字符
  const text = content.replace(/<[^>]*>/g, '')
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    // 明确传递 status=1，只获取已发布的公告
    const res = await announcementApi.getList({ 
      page: page.value, 
      size: size.value,
      status: 1  // 只显示已发布的公告
    })
    if (res.code === 200) {
      announcements.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取公告列表失败', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (item) => {
  try {
    const res = await announcementApi.getDetail(item.id)
    if (res.code === 200) {
      selectedAnnouncement.value = res.data
      showDetailDialog.value = true
    }
  } catch (error) {
    ElMessage.error('获取公告详情失败')
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped lang="scss">
.announcement-list-container {
  max-width: 1000px;
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

.announcement-list {
  background: #fff;
  border-radius: 12px;
  padding: 0 20px;
  
  .announcement-item {
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #f8f9fa;
      padding-left: 12px;
    }
    
    .announcement-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      flex-wrap: wrap;
      gap: 8px;
      
      .announcement-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
        
        .top-tag {
          margin-right: 8px;
        }
      }
      
      .announcement-time {
        font-size: 12px;
        color: #999;
      }
    }
    
    .announcement-summary {
      font-size: 14px;
      color: #666;
      line-height: 1.5;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
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

.announcement-detail {
  .detail-meta {
    display: flex;
    justify-content: space-between;
    padding-bottom: 12px;
    margin-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    font-size: 13px;
    color: #999;
  }
  
  .detail-content {
    line-height: 1.8;
    font-size: 14px;
    color: #555;
    
    :deep(h1), :deep(h2), :deep(h3) {
      margin: 16px 0 8px;
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
</style>