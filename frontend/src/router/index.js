import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../views/LayoutView.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('../views/DashboardView.vue') },
      { path: 'teachers', component: () => import('../views/TeachersView.vue') },
      { path: 'students', component: () => import('../views/StudentsView.vue') },
      { path: 'courses', component: () => import('../views/CoursesView.vue') },
      { path: 'classes', component: () => import('../views/ClassesView.vue') },
      { path: 'enrollments', component: () => import('../views/EnrollmentsView.vue') },
      { path: 'grades', component: () => import('../views/GradesView.vue') },
      { path: 'timetable', component: () => import('../views/TimetableView.vue') },
      { path: 'profile', component: () => import('../views/ProfileView.vue') },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }
  if (to.path === '/login' && auth.isLoggedIn) {
    return '/'
  }
})

export default router
