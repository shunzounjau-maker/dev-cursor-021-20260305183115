<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">教师管理</h2>
      <el-button type="primary" icon="Plus" @click="openDialog()">新增教师</el-button>
    </div>
    <el-card>
      <el-table :data="teachers" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="staff_no" label="工号" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTeacher(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑教师' : '新增教师'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!form.id">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="form.id ? '不修改请留空' : ''" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="工号" prop="staff_no" v-if="!form.id">
          <el-input v-model="form.staff_no" />
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="form.department" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTeacher" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const teachers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', password: '', name: '', staff_no: '', department: '', email: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur',
    validator: (rule, val, cb) => form.id || val ? cb() : cb(new Error('请输入密码')) }],
  name: [{ required: true, message: '请输入姓名' }],
  staff_no: [{ required: true, message: '请输入工号' }],
}

async function loadTeachers() {
  loading.value = true
  try { teachers.value = await api.get('/teachers') } finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form, { id: null, username: '', password: '', name: '', staff_no: '', department: '', email: '' })
  if (row) Object.assign(form, { id: row.id, name: row.name, department: row.department, email: row.email, staff_no: row.staff_no })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function saveTeacher() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { username: form.username, password: form.password, name: form.name,
      staff_no: form.staff_no, department: form.department, email: form.email }
    if (form.id) {
      await api.put(`/teachers/${form.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await api.post('/teachers', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTeachers()
  } finally { saving.value = false }
}

async function deleteTeacher(id) {
  await ElMessageBox.confirm('确定删除该教师？', '警告', { type: 'warning' })
  await api.delete(`/teachers/${id}`)
  ElMessage.success('删除成功')
  loadTeachers()
}

onMounted(loadTeachers)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; color: #303133; }
</style>
