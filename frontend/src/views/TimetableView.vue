<template>
  <div>
    <h2 class="page-title">我的课表</h2>
    <el-card>
      <div style="margin-bottom:16px">
        <el-select v-model="semester" placeholder="选择学期" clearable @change="loadTimetable">
          <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
        </el-select>
      </div>
      <el-table :data="timetable" stripe v-loading="loading">
        <el-table-column prop="course_name" label="课程名称" />
        <el-table-column prop="course_code" label="课程代码" width="100" />
        <el-table-column prop="credits" label="学分" width="70" />
        <el-table-column prop="teacher_name" label="教师" width="100" />
        <el-table-column prop="schedule" label="上课时间" />
        <el-table-column prop="semester" label="学期" width="120" />
      </el-table>
      <div v-if="timetable.length === 0 && !loading" style="text-align:center;padding:40px;color:#909399">
        暂无课程，请先选课
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../api'

const timetable = ref([])
const loading = ref(false)
const semester = ref('')

const semesters = computed(() => {
  const all = [...new Set(timetable.value.map(t => t.semester))]
  return all
})

async function loadTimetable() {
  loading.value = true
  try {
    const params = semester.value ? { semester: semester.value } : {}
    timetable.value = await api.get('/timetable', { params })
  } finally { loading.value = false }
}

onMounted(loadTimetable)
</script>

<style scoped>
.page-title { margin: 0 0 16px; color: #303133; }
</style>
