<template>
  <div class="competition-manage-container">
    <div class="page-header">
      <h2>竞赛管理</h2>
      <el-button type="primary" @click="goToCreate">
        <el-icon><Plus /></el-icon> 发布竞赛
      </el-button>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索竞赛名称" clearable style="width: 240px" @clear="fetchCompetitions" @keyup.enter="fetchCompetitions">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="searchStatus" placeholder="竞赛状态" clearable style="width: 120px" @change="fetchCompetitions">
        <el-option label="未开始" :value="0" />
        <el-option label="报名中" :value="1" />
        <el-option label="进行中" :value="2" />
        <el-option label="已结束" :value="3" />
      </el-select>
      <el-button type="primary" @click="fetchCompetitions">搜索</el-button>
    </div>
    
    <el-table :data="competitions" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="竞赛名称" min-width="200" />
      <el-table-column prop="category" label="竞赛类别" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报名时间" width="180">
        <template #default="{ row }">
          从{{ formatDate(row.startTime) }}<br>
          至{{ formatDate(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="竞赛时间/地点" width="180">
        <template #default="{ row }">
          <div>{{ formatDate(row.competitionTime) }}</div>
          <div class="location-text">{{ row.location || '待定' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="editCompetition(row)">编辑</el-button>
          <el-button type="danger" link @click="deleteCompetition(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchCompetitions"
      />
    </div>
    
    <!-- 编辑竞赛对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑竞赛" width="600px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="竞赛名称" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入竞赛名称" />
        </el-form-item>
        
        <el-form-item label="竞赛类别" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择竞赛类别" style="width: 100%">
            <el-option label="学科竞赛" value="学科竞赛" />
            <el-option label="创新创业" value="创新创业" />
            <el-option label="程序设计" value="程序设计" />
            <el-option label="数学建模" value="数学建模" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="英语竞赛" value="英语竞赛" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="竞赛简介" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="请输入竞赛简介" />
        </el-form-item>
        
        <el-form-item label="所需技能" prop="requiredSkills">
          <el-select v-model="editForm.requiredSkills" multiple filterable allow-create placeholder="请选择或输入技能" style="width: 100%">
            <el-option v-for="skill in skillOptions" :key="skill" :label="skill" :value="skill" />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报名开始时间" prop="startTime">
              <el-date-picker 
                v-model="editForm.startTime" 
                type="datetime" 
                placeholder="选择日期时间" 
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名结束时间" prop="endTime">
              <el-date-picker 
                v-model="editForm.endTime" 
                type="datetime" 
                placeholder="选择日期时间" 
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="竞赛开始时间" prop="competitionTime">
              <el-date-picker 
                v-model="editForm.competitionTime" 
                type="datetime" 
                placeholder="选择日期时间" 
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="竞赛地点" prop="location">
              <el-input v-model="editForm.location" placeholder="请输入竞赛地点" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最小人数/队" prop="minTeamMembers">
              <el-input-number v-model="editForm.minTeamMembers" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数/队" prop="maxTeamMembers">
              <el-input-number v-model="editForm.maxTeamMembers" :min="editForm.minTeamMembers" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="竞赛状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择竞赛状态" style="width: 100%">
            <el-option label="未开始" :value="0" />
            <el-option label="报名中" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已结束" :value="3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="详细内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="6" placeholder="请填写竞赛详细内容（支持HTML）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { competitionApi } from '@/api/competition'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const competitions = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchStatus = ref(null)
const showEditDialog = ref(false)
const editFormRef = ref()

const skillOptions = ['Java', 'Python', 'Spring Boot', 'Vue', 'React', 'MySQL', '算法', '数据结构', '机器学习', '深度学习', '数据分析', '前端开发', '后端开发']

const editForm = ref({
  id: null,
  title: '',
  category: '',
  description: '',
  requiredSkills: [],
  startTime: '',
  endTime: '',
  competitionTime: '',
  location: '',
  minTeamMembers: 1,
  maxTeamMembers: 5,
  status: 0,
  content: ''
})

const editRules = {
  title: [{ required: true, message: '请输入竞赛名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择竞赛类别', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择报名开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择报名结束时间', trigger: 'change' }]
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '未开始', 1: '报名中', 2: '进行中', 3: '已结束' }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchCompetitions = async () => {
  loading.value = true
  try {
    const res = await competitionApi.getList({
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value,
      status: searchStatus.value
    })
    if (res.code === 200) {
      competitions.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取竞赛列表失败', error)
    ElMessage.error('获取竞赛列表失败')
  } finally {
    loading.value = false
  }
}

const goToCreate = () => {
  router.push('/home/competition/create')
}

const editCompetition = (row) => {
  editForm.value = {
    id: row.id,
    title: row.title,
    category: row.category,
    description: row.description || '',
    requiredSkills: parseSkillsArray(row.requiredSkills),
    startTime: row.startTime,
    endTime: row.endTime,
    competitionTime: row.competitionTime,
    location: row.location || '',
    minTeamMembers: row.minTeamMembers || 1,
    maxTeamMembers: row.maxTeamMembers || 5,
    status: row.status,
    content: row.content || ''
  }
  showEditDialog.value = true
}

const parseSkillsArray = (skills) => {
  if (!skills) return []
  try {
    return JSON.parse(skills)
  } catch {
    return skills.split(',').filter(s => s.trim())
  }
}

const saveEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        // 转换日期格式
        const formatDate = (date) => {
          if (!date) return null
          return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
        }
        
        const submitData = {
          id: editForm.value.id,
          title: editForm.value.title,
          category: editForm.value.category,
          description: editForm.value.description,
          requiredSkills: JSON.stringify(editForm.value.requiredSkills),
          startTime: formatDate(editForm.value.startTime),
          endTime: formatDate(editForm.value.endTime),
          competitionTime: formatDate(editForm.value.competitionTime),
          location: editForm.value.location,
          minTeamMembers: editForm.value.minTeamMembers,
          maxTeamMembers: editForm.value.maxTeamMembers,
          status: editForm.value.status,
          content: editForm.value.content
        }
        
        const res = await competitionApi.update(submitData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          showEditDialog.value = false
          fetchCompetitions()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } catch (error) {
        console.error('更新失败', error)
        ElMessage.error('更新失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const deleteCompetition = async (row) => {
  ElMessageBox.confirm(`确定要删除竞赛 "${row.title}" 吗？此操作不可恢复！`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await competitionApi.delete(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchCompetitions()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchCompetitions()
})
</script>

<style scoped lang="scss">
.competition-manage-container {
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

.location-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>