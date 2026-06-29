<template>
  <div class="announcement-manage-container">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 发布公告
      </el-button>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索公告标题" clearable style="width: 240px" @clear="fetchAnnouncements" @keyup.enter="fetchAnnouncements">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="searchStatus" placeholder="公告状态" clearable style="width: 120px" @change="fetchAnnouncements">
        <el-option label="全部" value="" />
        <el-option label="已发布" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchAnnouncements">搜索</el-button>
    </div>
    
    <el-table :data="announcements" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="公告标题" min-width="250" />
      <el-table-column label="置顶" width="80">
        <template #default="{ row }">
          <el-switch 
            v-model="row.isTop" 
            :active-value="1" 
            :inactive-value="0" 
            @change="toggleTop(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.publishTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="publishUserName" label="发布人" width="120" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="editAnnouncement(row)">编辑</el-button>
          <el-button type="danger" link @click="deleteAnnouncement(row)">删除</el-button>
          <el-button v-if="row.status === 1" type="warning" link @click="updateStatus(row, 0)">下架</el-button>
          <el-button v-else type="success" link @click="updateStatus(row, 1)">发布</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchAnnouncements"
      />
    </div>
    
    <!-- 添加/编辑公告对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
          <span class="tip">置顶后公告会显示在列表顶部</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">发布</el-radio>
            <el-radio :label="0">暂存</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { announcementApi } from '@/api/announcement'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const announcements = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchStatus = ref('')
const showDialog = ref(false)
const dialogTitle = ref('发布公告')
const isEdit = ref(false)
const formRef = ref()

// 表单数据
const form = ref({
  id: null,
  title: '',
  content: '',
  isTop: 0,
  status: 1
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度为2-100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

/**
 * 格式化日期时间
 */
const formatDateTime = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

/**
 * 获取公告列表
 */
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value
    }
    // 只有选择了具体状态时才传递 status
    if (searchStatus.value !== '' && searchStatus.value !== null) {
      params.status = searchStatus.value
    }
    const res = await announcementApi.getList(params)
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

/**
 * 显示添加公告对话框
 */
const showAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '发布公告'
  form.value = {
    id: null,
    title: '',
    content: '',
    isTop: 0,
    status: 1
  }
  // 重置表单验证
  if (formRef.value) {
    formRef.value.resetFields()
  }
  showDialog.value = true
}

/**
 * 编辑公告
 */
const editAnnouncement = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑公告'
  form.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    isTop: row.isTop,
    status: row.status
  }
  showDialog.value = true
}

/**
 * 提交表单（创建或更新）
 */
const submitForm = async () => {
  // 表单验证
  if (!form.value.title) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('请输入公告内容')
    return
  }
  
  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await announcementApi.update(form.value)
    } else {
      res = await announcementApi.create(form.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
      showDialog.value = false
      fetchAnnouncements()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error(error.response?.data?.message || error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 删除公告
 */
const deleteAnnouncement = async (row) => {
  ElMessageBox.confirm(`确定要删除公告 "${row.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await announcementApi.delete(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchAnnouncements()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

/**
 * 更新公告状态（发布/下架）
 */
const updateStatus = async (row, status) => {
  const action = status === 1 ? '发布' : '下架'
  ElMessageBox.confirm(`确定要${action}公告 "${row.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await announcementApi.updateStatus({ id: row.id, status })
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        fetchAnnouncements()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

/**
 * 置顶/取消置顶
 */
const toggleTop = async (row) => {
  try {
    const res = await announcementApi.toggleTop(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message)
      fetchAnnouncements()
    } else {
      ElMessage.error(res.message)
      // 恢复原状态
      row.isTop = row.isTop === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
    row.isTop = row.isTop === 1 ? 0 : 1
  }
}

// 页面加载时获取公告列表
onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped lang="scss">
.announcement-manage-container {
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

.tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}
</style>