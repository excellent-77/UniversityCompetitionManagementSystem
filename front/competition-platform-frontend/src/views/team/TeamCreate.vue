<template>
  <div class="team-create-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>创建团队</h2>
      <p>为竞赛 "{{ competitionTitle }}" 组建您的团队</p>
    </div>
    
    <div class="create-form" v-loading="loading">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="form.teamName" placeholder="请输入团队名称，例如：代码先锋队" />
        </el-form-item>
        
        <el-form-item label="团队简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="介绍一下您的团队目标和所需成员" />
        </el-form-item>
        
        <el-form-item label="最大人数" prop="maxMembers">
          <el-input-number v-model="form.maxMembers" :min="minMembers" :max="maxMembers" />
          <span class="tip">（{{ minMembers }}-{{ maxMembers }}人/队）</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleCreate">创建团队</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 智能推荐提示 -->
    <div class="recommend-tip" v-if="!loading">
      <el-alert
        title="💡 智能组队小贴士"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <p>创建团队后，系统将根据您的技能标签智能推荐合适的队友！</p>
          <p>建议完善个人技能档案，提高匹配精准度。</p>
          <el-button type="primary" link @click="goToProfile">去完善个人资料</el-button>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { teamApi } from '@/api/team'
import { competitionApi } from '@/api/competition'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const competitionTitle = ref('')
const minMembers = ref(1)
const maxMembers = ref(5)

const competitionId = route.params.competitionId

const form = ref({
  teamName: '',
  description: '',
  maxMembers: 3
})

const rules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 30, message: '团队名称长度为2-30个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '简介不能超过200个字符', trigger: 'blur' }
  ]
}

const goBack = () => {
  router.push(`/competition/${competitionId}`)
}

const goToProfile = () => {
  router.push('/profile')
}

const handleCreate = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await teamApi.create({
          competitionId: parseInt(competitionId),
          teamName: form.value.teamName,
          description: form.value.description,
          maxMembers: form.value.maxMembers
        })
        if (res.code === 200) {
          ElMessage.success('团队创建成功！')
          router.push('/my-teams')
        } else {
          ElMessage.error(res.message)
        }
      } catch (error) {
        ElMessage.error('创建失败，请稍后重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

const fetchCompetitionDetail = async () => {
  loading.value = true
  try {
    const res = await competitionApi.getDetail(competitionId)
    if (res.code === 200) {
      competitionTitle.value = res.data.title
      minMembers.value = res.data.minTeamMembers || 1
      maxMembers.value = res.data.maxTeamMembers || 5
      form.value.maxMembers = Math.min(3, maxMembers.value)
    }
  } catch (error) {
    ElMessage.error('获取竞赛信息失败')
    router.push('/competitions')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (competitionId) {
    fetchCompetitionDetail()
  } else {
    ElMessage.error('参数错误')
    router.push('/competitions')
  }
})
</script>

<style scoped lang="scss">
.team-create-container {
  max-width: 700px;
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
  
  .tip {
    margin-left: 12px;
    font-size: 12px;
    color: #999;
  }
}

.recommend-tip {
  margin-top: 24px;
  
  p {
    margin: 4px 0;
    font-size: 14px;
  }
}
</style>