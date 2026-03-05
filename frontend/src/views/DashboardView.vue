<template>
  <div>
    <h2 class="page-title">仪表盘</h2>
    <el-row :gutter="20" v-if="auth.isAdmin">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon size="28" color="#fff"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card v-else>
      <template #header>欢迎使用校园管理系统</template>
      <p>您好，{{ auth.user?.name }}！</p>
      <p>角色：{{ roleLabel }}</p>
      <el-divider />
      <template v-if="auth.isStudent">
        <el-button type="primary" @click="$router.push('/classes')">浏览课程</el-button>
        <el-button @click="$router.push('/timetable')">查看课表</el-button>
        <el-button @click="$router.push('/grades')">查看成绩</el-button>
      </template>
      <template v-if="auth.isTeacher">
        <el-button type="primary" @click="$router.push('/classes')">我的课程</el-button>
        <el-button @click="$router.push('/grades')">成绩管理</el-button>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const dashData = ref({})

const stats = computed(() => [
  { label: '学生总数', value: dashData.value.total_students || 0, icon: 'User', color: '#409EFF' },
  { label: '教师总数', value: dashData.value.total_teachers || 0, icon: 'Avatar', color: '#67C23A' },
  { label: '课程总数', value: dashData.value.total_courses || 0, icon: 'Reading', color: '#E6A23C' },
  { label: '开课总数', value: dashData.value.total_classes || 0, icon: 'Calendar', color: '#F56C6C' },
])

const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[auth.user?.role] || ''
})

onMounted(async () => {
  if (auth.isAdmin) {
    try {
      dashData.value = await api.get('/dashboard')
    } catch (e) {}
  }
})
</script>

<style scoped>
.page-title { margin: 0 0 20px; color: #303133; }
.stat-card { margin-bottom: 20px; }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
