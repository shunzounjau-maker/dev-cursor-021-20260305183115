<template>
  <div>
    <h2 class="page-title">{{ auth.isStudent ? '我的成绩' : '成绩管理' }}</h2>

    <!-- Student view -->
    <el-card v-if="auth.isStudent">
      <el-table :data="grades" stripe v-loading="loading">
        <el-table-column prop="course_name" label="课程名称" />
        <el-table-column prop="course_code" label="课程代码" width="100" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="score" label="成绩" width="80">
          <template #default="{ row }">
            <el-tag :type="scoreType(row.score)">{{ row.score }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Teacher view -->
    <template v-if="auth.isTeacher">
      <el-card>
        <el-select v-model="selectedClass" placeholder="选择开课" style="width:300px;margin-bottom:16px"
          @change="loadClassGrades">
          <el-option v-for="c in myClasses" :key="c.id"
            :label="`${c.course_name} (${c.semester})`" :value="c.id" />
        </el-select>
        <el-table :data="classStudents" stripe v-loading="loading">
          <el-table-column prop="student_no" label="学号" width="100" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column label="成绩" width="200">
            <template #default="{ row }">
              <el-input-number v-if="editingId === row.enrollment_id"
                v-model="editScore" :min="0" :max="100" size="small" />
              <el-tag v-else-if="row.score !== null && row.score !== undefined"
                :type="scoreType(row.score)">{{ row.score }}</el-tag>
              <el-text v-else type="info">未录入</el-text>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <template v-if="editingId === row.enrollment_id">
                <el-button size="small" type="primary" @click="saveGrade(row)">保存</el-button>
                <el-button size="small" @click="editingId = null">取消</el-button>
              </template>
              <template v-else>
                <el-button size="small" @click="startEdit(row)">
                  {{ row.score !== null && row.score !== undefined ? '修改' : '录入' }}
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const grades = ref([])
const loading = ref(false)
const myClasses = ref([])
const selectedClass = ref(null)
const classStudents = ref([])
const editingId = ref(null)
const editScore = ref(0)

function scoreType(score) {
  if (score >= 90) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

async function loadGrades() {
  loading.value = true
  try { grades.value = await api.get('/grades') } finally { loading.value = false }
}

async function loadMyClasses() {
  myClasses.value = await api.get('/classes')
}

async function loadClassGrades(classId) {
  loading.value = true
  try {
    const students = await api.get(`/classes/${classId}/students`)
    const gradesData = await api.get('/grades')
    classStudents.value = students.map(s => {
      const g = gradesData.find(gr => gr.enrollment_id === s.enrollment_id)
      return { ...s, score: g?.score ?? null, grade_id: g?.id ?? null }
    })
  } finally { loading.value = false }
}

function startEdit(row) {
  editingId.value = row.enrollment_id
  editScore.value = row.score ?? 0
}

async function saveGrade(row) {
  try {
    if (row.grade_id) {
      await api.put(`/grades/${row.grade_id}`, { score: editScore.value })
    } else {
      await api.post('/grades', { enrollment_id: row.enrollment_id, score: editScore.value })
    }
    ElMessage.success('成绩保存成功')
    editingId.value = null
    loadClassGrades(selectedClass.value)
  } catch (e) {}
}

onMounted(async () => {
  if (auth.isStudent) {
    loadGrades()
  } else if (auth.isTeacher) {
    loadMyClasses()
  }
})
</script>

<style scoped>
.page-title { margin: 0 0 16px; color: #303133; }
</style>
