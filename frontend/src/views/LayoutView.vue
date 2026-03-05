<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="24" color="#fff"><School /></el-icon>
        <span>校园管理系统</span>
      </div>
      <el-menu :default-active="$route.path" router background-color="#304156"
        text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <template v-if="auth.isAdmin">
          <el-menu-item index="/teachers">
            <el-icon><Avatar /></el-icon><span>教师管理</span>
          </el-menu-item>
          <el-menu-item index="/students">
            <el-icon><User /></el-icon><span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="/courses">
            <el-icon><Reading /></el-icon><span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/classes">
            <el-icon><Calendar /></el-icon><span>开课管理</span>
          </el-menu-item>
        </template>
        <template v-if="auth.isTeacher">
          <el-menu-item index="/classes">
            <el-icon><Calendar /></el-icon><span>我的课程</span>
          </el-menu-item>
          <el-menu-item index="/grades">
            <el-icon><EditPen /></el-icon><span>成绩管理</span>
          </el-menu-item>
        </template>
        <template v-if="auth.isStudent">
          <el-menu-item index="/classes">
            <el-icon><Calendar /></el-icon><span>选课中心</span>
          </el-menu-item>
          <el-menu-item index="/enrollments">
            <el-icon><List /></el-icon><span>我的选课</span>
          </el-menu-item>
          <el-menu-item index="/timetable">
            <el-icon><Clock /></el-icon><span>我的课表</span>
          </el-menu-item>
          <el-menu-item index="/grades">
            <el-icon><Trophy /></el-icon><span>我的成绩</span>
          </el-menu-item>
        </template>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon><span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag :type="roleTagType">{{ roleLabel }}</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar size="small" icon="UserFilled" />
              <span>{{ auth.user?.name || auth.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const titleMap = {
  '/dashboard': '仪表盘',
  '/teachers': '教师管理',
  '/students': '学生管理',
  '/courses': '课程管理',
  '/classes': auth.isStudent ? '选课中心' : auth.isTeacher ? '我的课程' : '开课管理',
  '/enrollments': '我的选课',
  '/timetable': '我的课表',
  '/grades': auth.isStudent ? '我的成绩' : '成绩管理',
  '/profile': '个人信息'
}
const currentTitle = computed(() => titleMap[route.path] || '')

const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[auth.user?.role] || ''
})
const roleTagType = computed(() => {
  const map = { admin: 'danger', teacher: 'warning', student: 'success' }
  return map[auth.user?.role] || 'info'
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    auth.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.sidebar {
  background-color: #304156;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #3d5166;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
}
</style>
