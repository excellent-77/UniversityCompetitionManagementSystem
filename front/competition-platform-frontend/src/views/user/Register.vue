<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>注册账号</h2>
        <p>选择您的身份</p>
      </div>
      
      <el-radio-group v-model="userType" class="user-type-selector">
        <el-radio-button value="student">学生</el-radio-button>
        <el-radio-button value="teacher">教师</el-radio-button>
      </el-radio-group>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <!-- 学生特有字段 -->
        <template v-if="userType === 'student'">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" placeholder="请输入专业" />
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-select v-model="form.grade" placeholder="请选择年级">
              <el-option label="2024级" value="2024级" />
              <el-option label="2023级" value="2023级" />
              <el-option label="2022级" value="2022级" />
              <el-option label="2021级" value="2021级" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级" prop="className">
            <el-input v-model="form.className" placeholder="请输入班级" />
          </el-form-item>
          <el-form-item label="技能标签" prop="skills">
            <el-select v-model="form.skills" multiple filterable allow-create placeholder="请选择或输入技能">
              <el-option v-for="skill in skillOptions" :key="skill" :label="skill" :value="skill" />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- 教师特有字段 -->
        <template v-if="userType === 'teacher'">
          <el-form-item label="工号" prop="teacherNo">
            <el-input v-model="form.teacherNo" placeholder="请输入工号" />
          </el-form-item>
          <el-form-item label="所属学院" prop="department">
            <el-input v-model="form.department" placeholder="请输入所属学院" />
          </el-form-item>
          <el-form-item label="职称" prop="title">
            <el-select v-model="form.title" placeholder="请选择职称">
              <el-option label="教授" value="教授" />
              <el-option label="副教授" value="副教授" />
              <el-option label="讲师" value="讲师" />
              <el-option label="助教" value="助教" />
            </el-select>
          </el-form-item>
          <el-form-item label="研究方向" prop="researchDirection">
            <el-select v-model="form.researchDirection" multiple filterable allow-create placeholder="请选择或输入研究方向">
              <el-option v-for="dir in directionOptions" :key="dir" :label="dir" :value="dir" />
            </el-select>
          </el-form-item>
        </template>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" class="register-btn">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="goToLogin">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const formRef = ref()
const userType = ref('student')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  // 学生字段
  studentNo: '',
  major: '',
  grade: '',
  className: '',
  skills: [],
  // 教师字段
  teacherNo: '',
  department: '',
  title: '',
  researchDirection: []
})

const skillOptions = ['Java', 'Python', 'Spring Boot', 'Vue', 'MySQL', '算法', '数据结构', '机器学习', '前端开发', '后端开发']
const directionOptions = ['软件工程', '人工智能', '数据挖掘', '机器学习', '计算机视觉', '自然语言处理', '网络安全']

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  department: [{ required: true, message: '请输入所属学院', trigger: 'blur' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const registerData = { ...form }
      registerData.skills = JSON.stringify(form.skills)
      registerData.researchDirection = JSON.stringify(form.researchDirection)
      
      let res
      if (userType.value === 'student') {
        res = await userApi.registerStudent(registerData)
      } else {
        res = await userApi.registerTeacher(registerData)
      }
      
      loading.value = false
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(res.message)
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 0;
}

.register-card {
  width: 550px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  
  .register-header {
    text-align: center;
    margin-bottom: 24px;
    
    h2 {
      font-size: 28px;
      color: #333;
      margin-bottom: 8px;
    }
    
    p {
      color: #999;
    }
  }
  
  .user-type-selector {
    display: flex;
    justify-content: center;
    margin-bottom: 24px;
  }
  
  .register-btn {
    width: 100%;
  }
  
  .register-footer {
    text-align: center;
    margin-top: 16px;
  }
}
</style>