<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">学生管理</h2>
      <el-button type="primary" icon="Plus" @click="openDialog()">新增学生</el-button>
    </div>
    <el-card>
      <el-table :data="students" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="student_no" label="学号" />
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="class_name" label="班级" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteStudent(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学生' : '新增学生'" width="500px">
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
        <el-form-item label="学号" prop="student_no" v-if="!form.id">
          <el-input v-model="form.student_no" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" />
        </el-form-item>
        <el-form-item label="班级" prop="class_name">
          <el-input v-model="form.class_name" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStudent" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const students = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', password: '', name: '', student_no: '', grade: '', class_name: '', email: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  name: [{ required: true, message: '请输入姓名' }],
  student_no: [{ required: true, message: '请输入学号' }],
}

async function loadStudents() {
  loading.value = true
  try { students.value = await api.get('/students') } finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form, { id: null, username: '', password: '', name: '', student_no: '', grade: '', class_name: '', email: '' })
  if (row) Object.assign(form, { id: row.id, name: row.name, grade: row.grade, class_name: row.class_name, email: row.email })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function saveStudent() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { username: form.username, password: form.password, name: form.name,
      student_no: form.student_no, grade: form.grade, class_name: form.class_name, email: form.email }
    if (form.id) {
      await api.put(`/students/${form.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await api.post('/students', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadStudents()
  } finally { saving.value = false }
}

async function deleteStudent(id) {
  await ElMessageBox.confirm('确定删除该学生？', '警告', { type: 'warning' })
  await api.delete(`/students/${id}`)
  ElMessage.success('删除成功')
  loadStudents()
}

onMounted(loadStudents)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; color: #303133; }
</style>
