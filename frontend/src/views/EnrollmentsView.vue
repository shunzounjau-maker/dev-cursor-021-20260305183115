<template>
  <div>
    <h2 class="page-title">我的选课</h2>
    <el-card>
      <el-table :data="enrollments" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="course_name" label="课程名称" />
        <el-table-column prop="course_code" label="课程代码" width="100" />
        <el-table-column prop="credits" label="学分" width="70" />
        <el-table-column prop="teacher_name" label="教师" width="100" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="schedule" label="上课时间" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="dropCourse(row.id)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card style="margin-top:16px">
      <el-statistic title="已选总学分" :value="totalCredits" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const enrollments = ref([])
const loading = ref(false)

const totalCredits = computed(() => enrollments.value.reduce((sum, e) => sum + (e.credits || 0), 0))

async function loadEnrollments() {
  loading.value = true
  try { enrollments.value = await api.get('/enrollments') } finally { loading.value = false }
}

async function dropCourse(id) {
  await ElMessageBox.confirm('确定退课？退课后成绩将无法恢复。', '警告', { type: 'warning' })
  try {
    await api.delete(`/enrollments/${id}`)
    ElMessage.success('退课成功')
    loadEnrollments()
  } catch (e) {}
}

onMounted(loadEnrollments)
</script>

<style scoped>
.page-title { margin: 0 0 16px; color: #303133; }
</style>
