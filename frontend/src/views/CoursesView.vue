<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">课程管理</h2>
      <el-button type="primary" icon="Plus" @click="openDialog()" v-if="auth.isAdmin">新增课程</el-button>
    </div>
    <el-card>
      <el-table :data="courses" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="code" label="课程代码" width="120" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="credits" label="学分" width="80" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="160" v-if="auth.isAdmin">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteCourse(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="课程代码" prop="code" v-if="!form.id">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="form.credits" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCourse" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const courses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, code: '', name: '', credits: 3, description: '' })

const rules = {
  code: [{ required: true, message: '请输入课程代码' }],
  name: [{ required: true, message: '请输入课程名称' }],
  credits: [{ required: true, message: '请输入学分' }],
}

async function loadCourses() {
  loading.value = true
  try { courses.value = await api.get('/courses') } finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form, { id: null, code: '', name: '', credits: 3, description: '' })
  if (row) Object.assign(form, { id: row.id, code: row.code, name: row.name, credits: row.credits, description: row.description })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function saveCourse() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await api.put(`/courses/${form.id}`, form)
      ElMessage.success('更新成功')
    } else {
      await api.post('/courses', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCourses()
  } finally { saving.value = false }
}

async function deleteCourse(id) {
  await ElMessageBox.confirm('确定删除该课程？', '警告', { type: 'warning' })
  await api.delete(`/courses/${id}`)
  ElMessage.success('删除成功')
  loadCourses()
}

onMounted(loadCourses)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; color: #303133; }
</style>
