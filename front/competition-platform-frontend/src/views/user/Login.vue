<template>
  <div class="login-container">
    <div class="login-card">
      <h1>高校竞赛管理平台</h1>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
        <div style="text-align: center">
          <span>还没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
        </div>
        <!-- <el-divider>测试账号</el-divider> -->
        <!-- <div style="display: flex; gap: 8px; justify-content: center; flex-wrap: wrap">
          <el-tag @click="fillDemo('admin', '123456')">管理员: admin</el-tag>
          <el-tag @click="fillDemo('teacher_zhang', '123456')">教师: teacher_zhang</el-tag>
          <el-tag @click="fillDemo('student_wang', '123456')">学生: student_wang</el-tag>
        </div> -->
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  const result = await userStore.login(form.value.username, form.value.password)
  loading.value = false
  
  if (result.success) {
    ElMessage.success('登录成功')
    router.push('/home')
  } else {
    ElMessage.error(result.message)
  }
}

// const fillDemo = (username, password) => {
//   form.value.username = username
//   form.value.password = password
// }
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.login-card h1 {
  text-align: center;
  margin-bottom: 32px;
  font-size: 24px;
  color: #333;
}

.el-tag {
  cursor: pointer;
}

.el-tag:hover {
  background: #1e3c72;
  color: #fff;
}
</style>