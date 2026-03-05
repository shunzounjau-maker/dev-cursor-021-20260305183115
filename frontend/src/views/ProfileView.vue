<template>
  <div>
    <h2 class="page-title">个人信息</h2>
    <el-card style="max-width:500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="roleTagType">{{ roleLabel }}</el-tag>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.password" type="password" placeholder="不修改请留空" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="saving">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const saving = ref(false)
const formRef = ref()
const form = reactive({ username: '', name: '', email: '', password: '' })

const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[auth.user?.role] || ''
})
const roleTagType = computed(() => {
  const map = { admin: 'danger', teacher: 'warning', student: 'success' }
  return map[auth.user?.role] || 'info'
})

onMounted(async () => {
  const data = await api.get('/auth/me')
  Object.assign(form, { username: data.username, name: data.name, email: data.email, password: '' })
})

async function save() {
  saving.value = true
  try {
    await api.put('/auth/me', { name: form.name, email: form.email, password: form.password })
    ElMessage.success('保存成功')
    auth.user = { ...auth.user, name: form.name, email: form.email }
    localStorage.setItem('user', JSON.stringify(auth.user))
    form.password = ''
  } finally { saving.value = false }
}
</script>

<style scoped>
.page-title { margin: 0 0 16px; color: #303133; }
</style>
