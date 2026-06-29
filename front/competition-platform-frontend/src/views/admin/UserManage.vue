<template>
  <div class="user-manage-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon> 添加用户
      </el-button>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索用户名/姓名" clearable style="width: 240px" @clear="fetchUsers" @keyup.enter="fetchUsers">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="searchRole" placeholder="用户角色" clearable style="width: 120px" @change="fetchUsers">
        <el-option label="全部" :value="null" />
        <el-option label="管理员" :value="0" />
        <el-option label="教师" :value="1" />
        <el-option label="学生" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchUsers">搜索</el-button>
    </div>
    
    <el-table :data="users" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="updateStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="editUser(row)">编辑</el-button>
          <el-button type="danger" link @click="deleteUser(row)">删除</el-button>
          <el-button type="warning" link @click="resetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
      />
    </div>
    
    <!-- 添加用户对话框 -->
    <el-dialog v-model="showAddDialog" title="添加用户" width="500px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" placeholder="请选择角色">
            <el-option label="管理员" :value="0" />
            <el-option label="教师" :value="1" />
            <el-option label="学生" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="addForm.role === 2" label="学号">
          <el-input v-model="addForm.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item v-if="addForm.role === 2" label="专业">
          <el-input v-model="addForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item v-if="addForm.role === 1" label="工号">
          <el-input v-model="addForm.teacherNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item v-if="addForm.role === 1" label="学院">
          <el-input v-model="addForm.department" placeholder="请输入学院" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="addUser">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑用户对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { userApi } from '@/api/user'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const users = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchRole = ref(null)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const addFormRef = ref()

// 从路由参数获取初始角色筛选
const initRoleFromQuery = () => {
  const roleParam = route.query.role
  if (roleParam !== undefined && roleParam !== null) {
    searchRole.value = parseInt(roleParam)
  }
}

const addForm = ref({
  username: '',
  password: '123456',
  realName: '',
  role: 2,
  phone: '',
  email: '',
  studentNo: '',
  major: '',
  teacherNo: '',
  department: ''
})

const editForm = ref({
  id: null,
  realName: '',
  phone: '',
  email: ''
})

const addRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleType = (role) => {
  const types = { 0: 'danger', 1: 'success', 2: 'primary' }
  return types[role] || 'info'
}

const getRoleText = (role) => {
  const texts = { 0: '管理员', 1: '教师', 2: '学生' }
  return texts[role] || '未知'
}

const formatDateTime = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.getUserList({
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value,
      role: searchRole.value
    })
    if (res.code === 200) {
      users.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
    // 模拟数据
    users.value = [
      { id: 1, username: 'admin', realName: '系统管理员', role: 0, phone: '13800000000', email: 'admin@competition.com', status: 1, createTime: '2026-03-24 10:00:00' },
      { id: 2, username: 'teacher_zhang', realName: '张明', role: 1, phone: '13800000001', email: 'zhangming@univ.edu', status: 1, createTime: '2026-03-24 10:00:00' },
      { id: 3, username: 'teacher_li', realName: '李芳', role: 1, phone: '13800000002', email: 'lifang@univ.edu', status: 1, createTime: '2026-03-24 10:00:00' },
      { id: 4, username: 'student_wang', realName: '王小明', role: 2, phone: '13800000003', email: 'wangxm@univ.edu', status: 1, createTime: '2026-03-24 10:00:00' },
      { id: 5, username: 'student_zhao', realName: '赵丽颖', role: 2, phone: '13800000004', email: 'zhaoly@univ.edu', status: 1, createTime: '2026-03-24 10:00:00' },
      { id: 6, username: 'student_liu', realName: '刘强', role: 2, phone: '13800000005', email: 'liuqiang@univ.edu', status: 1, createTime: '2026-03-24 10:00:00' },
      { id: 7, username: 'student_chen', realName: '陈思思', role: 2, phone: '13800000006', email: 'chensisi@univ.edu', status: 1, createTime: '2026-03-24 10:00:00' },
      { id: 8, username: 'student_zhou', realName: '周杰', role: 2, phone: '13800000007', email: 'zhoujie@univ.edu', status: 1, createTime: '2026-03-24 10:00:00' }
    ]
    total.value = 8
  } finally {
    loading.value = false
  }
}

const updateStatus = async (user) => {
  try {
    await userApi.updateUserStatus(user.id, user.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    user.status = user.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const editUser = (user) => {
  editForm.value = {
    id: user.id,
    realName: user.realName,
    phone: user.phone,
    email: user.email
  }
  showEditDialog.value = true
}

const saveEdit = async () => {
  submitting.value = true
  try {
    await userApi.updateUser(editForm.value)
    ElMessage.success('更新成功')
    showEditDialog.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

const deleteUser = async (user) => {
  ElMessageBox.confirm(`确定要删除用户 "${user.realName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await userApi.deleteUser(user.id)
      ElMessage.success('删除成功')
      fetchUsers()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const resetPassword = async (user) => {
  ElMessageBox.confirm(`确定要重置用户 "${user.realName}" 的密码为 123456 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await userApi.resetPassword(user.id)
      ElMessage.success('密码已重置为 123456')
    } catch (error) {
      ElMessage.error('重置失败')
    }
  }).catch(() => {})
}

const addUser = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let res
        if (addForm.value.role === 2) {
          res = await userApi.registerStudent({
            username: addForm.value.username,
            password: addForm.value.password,
            realName: addForm.value.realName,
            phone: addForm.value.phone,
            email: addForm.value.email,
            studentNo: addForm.value.studentNo,
            major: addForm.value.major
          })
        } else if (addForm.value.role === 1) {
          res = await userApi.registerTeacher({
            username: addForm.value.username,
            password: addForm.value.password,
            realName: addForm.value.realName,
            phone: addForm.value.phone,
            email: addForm.value.email,
            teacherNo: addForm.value.teacherNo,
            department: addForm.value.department
          })
        } else {
          res = await userApi.registerAdmin({
            username: addForm.value.username,
            password: addForm.value.password,
            realName: addForm.value.realName,
            phone: addForm.value.phone,
            email: addForm.value.email
          })
        }
        
        if (res.code === 200) {
          ElMessage.success('添加成功')
          showAddDialog.value = false
          fetchUsers()
          addForm.value = {
            username: '',
            password: '123456',
            realName: '',
            role: 2,
            phone: '',
            email: '',
            studentNo: '',
            major: '',
            teacherNo: '',
            department: ''
          }
        } else {
          ElMessage.error(res.message)
        }
      } catch (error) {
        ElMessage.error('添加失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 监听路由参数变化
import { watch } from 'vue'
watch(() => route.query.role, (newRole) => {
  if (newRole !== undefined) {
    searchRole.value = newRole ? parseInt(newRole) : null
    page.value = 1
    fetchUsers()
  }
})

onMounted(() => {
  initRoleFromQuery()
  fetchUsers()
})
</script>

<style scoped lang="scss">
.user-manage-container {
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
</style>