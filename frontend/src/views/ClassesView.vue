<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">{{ pageTitle }}</h2>
      <el-button type="primary" icon="Plus" @click="openDialog()" v-if="auth.isAdmin">新增开课</el-button>
    </div>
    <el-card>
      <el-table :data="classes" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="course_name" label="课程名称" />
        <el-table-column prop="course_code" label="课程代码" width="100" />
        <el-table-column prop="credits" label="学分" width="70" />
        <el-table-column prop="teacher_name" label="教师" width="100" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="schedule" label="上课时间" />
        <el-table-column prop="capacity" label="容量" width="70" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="auth.isAdmin">
              <el-button size="small" @click="openDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteClass(row.id)">删除</el-button>
            </template>
            <template v-if="auth.isStudent">
              <el-button size="small" type="primary" @click="enroll(row.id)">选课</el-button>
            </template>
            <template v-if="auth.isTeacher">
              <el-button size="small" @click="viewStudents(row)">查看学生</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Admin: Create/Edit Class Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑开课' : '新增开课'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="课程" prop="course_id">
          <el-select v-model="form.course_id" placeholder="选择课程" style="width:100%">
            <el-option v-for="c in courses" :key="c.id" :label="`${c.name}(${c.code})`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师" prop="teacher_id">
          <el-select v-model="form.teacher_id" placeholder="选择教师" style="width:100%">
            <el-option v-for="t in teachers" :key="t.id" :label="`${t.name}(${t.staff_no})`" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="如: 2024-2025-2" />
        </el-form-item>
        <el-form-item label="上课时间" prop="schedule">
          <el-input v-model="form.schedule" placeholder="如: 周一 1-2节" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveClass" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- Teacher: View Students Dialog -->
    <el-dialog v-model="studentsDialogVisible" :title="`${currentClass?.course_name} - 学生名单`" width="600px">
      <el-table :data="classStudents" stripe v-loading="studentsLoading">
        <el-table-column prop="student_no" label="学号" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="class_name" label="班级" />
        <el-table-column prop="enrolled_at" label="选课时间" :formatter="formatDate" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const classes = ref([])
const courses = ref([])
const teachers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, course_id: null, teacher_id: null, semester: '', schedule: '', capacity: 30 })

const studentsDialogVisible = ref(false)
const classStudents = ref([])
const studentsLoading = ref(false)
const currentClass = ref(null)

const pageTitle = computed(() => {
  if (auth.isStudent) return '选课中心'
  if (auth.isTeacher) return '我的课程'
  return '开课管理'
})

const rules = {
  course_id: [{ required: true, message: '请选择课程' }],
  teacher_id: [{ required: true, message: '请选择教师' }],
  semester: [{ required: true, message: '请输入学期' }],
}

async function loadData() {
  loading.value = true
  try {
    classes.value = await api.get('/classes')
    if (auth.isAdmin) {
      [courses.value, teachers.value] = await Promise.all([
        api.get('/courses'), api.get('/teachers')
      ])
    }
  } finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form, { id: null, course_id: null, teacher_id: null, semester: '', schedule: '', capacity: 30 })
  if (row) Object.assign(form, { id: row.id, course_id: row.course_id, teacher_id: row.teacher_id,
    semester: row.semester, schedule: row.schedule, capacity: row.capacity })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function saveClass() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { course_id: form.course_id, teacher_id: form.teacher_id,
      semester: form.semester, schedule: form.schedule, capacity: form.capacity }
    if (form.id) {
      await api.put(`/classes/${form.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await api.post('/classes', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}

async function deleteClass(id) {
  await ElMessageBox.confirm('确定删除该开课记录？', '警告', { type: 'warning' })
  await api.delete(`/classes/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

async function enroll(classId) {
  try {
    await api.post('/enrollments', { class_id: classId })
    ElMessage.success('选课成功')
  } catch (e) {}
}

async function viewStudents(row) {
  currentClass.value = row
  studentsDialogVisible.value = true
  studentsLoading.value = true
  try {
    classStudents.value = await api.get(`/classes/${row.id}/students`)
  } finally { studentsLoading.value = false }
}

function formatDate(row, col, val) {
  return val ? new Date(val).toLocaleString('zh-CN') : ''
}

onMounted(loadData)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; color: #303133; }
</style>
