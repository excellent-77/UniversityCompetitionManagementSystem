<template>
  <div class="profile-container">
    <div class="profile-sidebar">
      <div class="info-section">
        <h3>{{ user.realName || user.username }}</h3>
        <p>{{ roleText }}</p>
      </div>
      <el-menu :default-active="activeTab" @select="handleTabSelect">
        <el-menu-item index="basic">
          <el-icon><User /></el-icon>
          <span>基本信息</span>
        </el-menu-item>
        <el-menu-item v-if="isStudent" index="skills">
          <el-icon><Tools /></el-icon>
          <span>技能档案</span>
        </el-menu-item>
        <el-menu-item v-if="isTeacher" index="research">
          <el-icon><Reading /></el-icon>
          <span>研究方向</span>
        </el-menu-item>
      </el-menu>
    </div>
    
    <div class="profile-content">
      <div v-if="activeTab === 'basic'" class="basic-info">
        <h2>基本信息</h2>
        <el-form :model="basicForm" :rules="basicRules" ref="basicFormRef" label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="basicForm.username" disabled />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="basicForm.realName" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="basicForm.phone" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="basicForm.email" />
          </el-form-item>
          <el-form-item v-if="isStudent" label="学号">
            <el-input v-model="basicForm.studentNo" disabled />
          </el-form-item>
          <el-form-item v-if="isStudent" label="专业">
            <el-input v-model="basicForm.major" disabled />
          </el-form-item>
          <el-form-item v-if="isStudent" label="年级">
            <el-input v-model="basicForm.grade" disabled />
          </el-form-item>
          <el-form-item v-if="isTeacher" label="工号">
            <el-input v-model="basicForm.teacherNo" disabled />
          </el-form-item>
          <el-form-item v-if="isTeacher" label="所属学院">
            <el-input v-model="basicForm.department" disabled />
          </el-form-item>
          <el-form-item v-if="isTeacher" label="职称">
            <el-input v-model="basicForm.title" disabled />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveBasicInfo">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div v-if="activeTab === 'skills' && isStudent" class="skills-info">
        <h2>技能档案</h2>
        <el-form :model="skillsForm" ref="skillsFormRef" label-width="100px">
          <el-form-item label="技能标签">
            <el-select v-model="skillsForm.skills" multiple filterable allow-create placeholder="请选择或输入技能">
              <el-option v-for="skill in skillOptions" :key="skill" :label="skill" :value="skill" />
            </el-select>
          </el-form-item>
          <el-form-item label="竞赛兴趣">
            <el-select v-model="skillsForm.interests" multiple filterable allow-create placeholder="请选择竞赛兴趣方向">
              <el-option v-for="interest in interestOptions" :key="interest" :label="interest" :value="interest" />
            </el-select>
          </el-form-item>
          <el-form-item label="获奖经历">
            <el-input v-model="skillsForm.honors" type="textarea" :rows="4" placeholder="请填写获奖经历" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveSkills">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div v-if="activeTab === 'research' && isTeacher" class="research-info">
        <h2>研究方向</h2>
        <el-form :model="researchForm" ref="researchFormRef" label-width="100px">
          <el-form-item label="研究方向">
            <el-select v-model="researchForm.researchDirection" multiple filterable allow-create placeholder="请选择或输入研究方向">
              <el-option v-for="dir in directionOptions" :key="dir" :label="dir" :value="dir" />
            </el-select>
          </el-form-item>
          <el-form-item label="可指导队伍数">
            <el-input-number v-model="researchForm.maxTeams" :min="1" :max="10" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveResearch">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()
const user = computed(() => userStore.user)
const isStudent = computed(() => userStore.isStudent)
const isTeacher = computed(() => userStore.isTeacher)

const activeTab = ref('basic')
const saving = ref(false)

const roleText = computed(() => {
  if (user.value.role === 0) return '管理员'
  if (user.value.role === 1) return '教师'
  return '学生'
})

const basicForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  studentNo: '',
  major: '',
  grade: '',
  teacherNo: '',
  department: '',
  title: ''
})

const skillsForm = reactive({
  skills: [],
  interests: [],
  honors: ''
})

const researchForm = reactive({
  researchDirection: [],
  maxTeams: 5
})

const skillOptions = ['Java', 'Python', 'Spring Boot', 'Vue', 'MySQL', '算法', '数据结构', '机器学习', '前端开发', '后端开发']
const interestOptions = ['程序设计', '数学建模', '人工智能', '创新创业', '大数据', '网络安全']
const directionOptions = ['软件工程', '人工智能', '数据挖掘', '机器学习', '计算机视觉', '自然语言处理', '网络安全']

const basicRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const loadUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo()
    if (res.code === 200) {
      const data = res.data
      basicForm.username = data.username
      basicForm.realName = data.realName
      basicForm.phone = data.phone
      basicForm.email = data.email
      
      if (isStudent.value) {
        basicForm.studentNo = data.studentNo
        basicForm.major = data.major
        basicForm.grade = data.grade
        
        if (data.skills) {
          try {
            skillsForm.skills = JSON.parse(data.skills)
          } catch { skillsForm.skills = data.skills ? data.skills.split(',') : [] }
        }
        if (data.interests) {
          try {
            skillsForm.interests = JSON.parse(data.interests)
          } catch { skillsForm.interests = data.interests ? data.interests.split(',') : [] }
        }
        skillsForm.honors = data.honors || ''
      }
      
      if (isTeacher.value) {
        basicForm.teacherNo = data.teacherNo
        basicForm.department = data.department
        basicForm.title = data.title
        
        if (data.researchDirection) {
          try {
            researchForm.researchDirection = JSON.parse(data.researchDirection)
          } catch { researchForm.researchDirection = data.researchDirection ? data.researchDirection.split(',') : [] }
        }
        if (data.maxTeams) {
          researchForm.maxTeams = data.maxTeams
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

const saveBasicInfo = async () => {
  saving.value = true
  try {
    const res = await userApi.updateProfile({
      realName: basicForm.realName,
      phone: basicForm.phone,
      email: basicForm.email
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      userStore.updateUserInfo({ realName: basicForm.realName })
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const saveSkills = async () => {
  saving.value = true
  try {
    const res = await userApi.updateProfile({
      skills: JSON.stringify(skillsForm.skills),
      interests: JSON.stringify(skillsForm.interests),
      honors: skillsForm.honors
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const saveResearch = async () => {
  saving.value = true
  try {
    const res = await userApi.updateProfile({
      researchDirection: JSON.stringify(researchForm.researchDirection),
      maxTeams: researchForm.maxTeams
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleTabSelect = (index) => {
  activeTab.value = index
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.profile-sidebar {
  width: 280px;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  padding: 24px;
  
  .info-section {
    text-align: center;
    margin-bottom: 24px;
    
    h3 {
      font-size: 18px;
      color: #333;
      margin-bottom: 8px;
    }
    
    p {
      color: #999;
      font-size: 14px;
    }
  }
}

.profile-content {
  flex: 1;
  padding: 24px;
  
  h2 {
    font-size: 20px;
    margin-bottom: 24px;
    color: #333;
  }
  
  .basic-info, .skills-info, .research-info {
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    max-width: 600px;
  }
}
</style>