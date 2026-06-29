<template>
  <div class="competition-create-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>发布新竞赛</h2>
      <p>填写竞赛信息，让学生们报名参加</p>
    </div>
    
    <div class="create-form" v-loading="submitting">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="竞赛名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入竞赛名称" />
        </el-form-item>
        
        <el-form-item label="竞赛类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择竞赛类别">
            <el-option label="学科竞赛" value="学科竞赛" />
            <el-option label="创新创业" value="创新创业" />
            <el-option label="程序设计" value="程序设计" />
            <el-option label="数学建模" value="数学建模" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="英语竞赛" value="英语竞赛" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="竞赛简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要介绍竞赛内容" />
        </el-form-item>
        
        <el-form-item label="详细内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="填写详细的竞赛规则、要求等信息" />
        </el-form-item>
        
        <el-form-item label="所需技能" prop="requiredSkills">
          <el-select v-model="form.requiredSkills" multiple filterable allow-create placeholder="请选择或输入所需技能">
            <el-option v-for="skill in skillOptions" :key="skill" :label="skill" :value="skill" />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报名开始时间" prop="startTime">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择日期时间" format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名结束时间" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择日期时间" format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="竞赛开始时间" prop="competitionTime">
              <el-date-picker v-model="form.competitionTime" type="datetime" placeholder="选择日期时间" format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="竞赛地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入竞赛地点" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最少人数/队" prop="minTeamMembers">
              <el-input-number v-model="form.minTeamMembers" :min="1" :max="10" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最多人数/队" prop="maxTeamMembers">
              <el-input-number v-model="form.maxTeamMembers" :min="form.minTeamMembers" :max="10" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布竞赛</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { competitionApi } from '@/api/competition'
import dayjs from 'dayjs'

const router = useRouter()
const submitting = ref(false)
const formRef = ref()

const skillOptions = ['Java', 'Python', 'Spring Boot', 'Vue', 'MySQL', '算法', '数据结构', '机器学习', '深度学习', '数据分析']

const form = reactive({
  title: '',
  category: '',
  description: '',
  content: '',
  requiredSkills: [],
  startTime: '',
  endTime: '',
  competitionTime: '',
  location: '',
  minTeamMembers: 1,
  maxTeamMembers: 5
})

const rules = {
  title: [{ required: true, message: '请输入竞赛名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择竞赛类别', trigger: 'change' }],
  description: [{ required: true, message: '请输入竞赛简介', trigger: 'blur' }],
  content: [{ required: true, message: '请输入竞赛详细内容', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择报名开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择报名结束时间', trigger: 'change' }]
}

const goBack = () => {
  router.go(-1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = {
          ...form,
          requiredSkills: JSON.stringify(form.requiredSkills),
          startTime: form.startTime ? dayjs(form.startTime).format('YYYY-MM-DD HH:mm:ss') : null,
          endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DD HH:mm:ss') : null,
          competitionTime: form.competitionTime ? dayjs(form.competitionTime).format('YYYY-MM-DD HH:mm:ss') : null
        }
        
        const res = await competitionApi.create(submitData)
        if (res.code === 200) {
          ElMessage.success('竞赛发布成功！')
          router.push('/home/competitions')
        } else {
          ElMessage.error(res.message)
        }
      } catch (error) {
        ElMessage.error('发布失败')
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.competition-create-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
  
  .el-button {
    position: absolute;
    left: 24px;
    top: 24px;
  }
  
  h2 {
    font-size: 28px;
    font-weight: 600;
    margin: 16px 0 8px;
    color: #333;
  }
  
  p {
    color: #999;
  }
}

.create-form {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>