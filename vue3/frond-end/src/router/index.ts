import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { clearAuthStorage, getUserInfo } from '@/utils/user'

const MainLayout = () => import('@/components/layout/MainLayout.vue')
const Login = () => import('@/views/Login.vue')
const Profile = () => import('@/views/Profile.vue')
const NotFound = () => import('@/views/NotFound.vue')

const studentRoutes: RouteRecordRaw[] = [
  {
    path: '/student',
    component: MainLayout,
    meta: { role: 'student' },
    children: [
      { path: '', redirect: '/student/courses' },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('@/views/student/CourseCenter.vue'),
        meta: { title: '课程中心', role: 'student' },
      },
      {
        path: 'courses/:id',
        name: 'StudentCourseDetail',
        component: () => import('@/views/student/CourseDetail.vue'),
        meta: { title: '课程详情', role: 'student' },
      },
      {
        path: 'grades',
        name: 'StudentGrades',
        component: () => import('@/views/student/Grades.vue'),
        meta: { title: '成绩查询', role: 'student' },
      },
      {
        path: 'notices',
        name: 'StudentNotices',
        component: () => import('@/views/student/NoticeCenter.vue'),
        meta: { title: '公告中心', role: 'student' },
      },
      {
        path: 'ai-chat',
        name: 'StudentAiChat',
        component: () => import('@/views/student/AiChat.vue'),
        meta: { title: 'AI智能问答', role: 'student' },
      },
      {
        path: 'summary',
        name: 'StudentSummary',
        component: () => import('@/views/student/KnowledgeSummary.vue'),
        meta: { title: '知识点总结', role: 'student' },
      },
    ],
  },
]

const teacherRoutes: RouteRecordRaw[] = [
  {
    path: '/teacher',
    component: MainLayout,
    meta: { role: 'teacher' },
    children: [
      { path: '', redirect: '/teacher/courses' },
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: () => import('@/views/teacher/CourseManage.vue'),
        meta: { title: '课程管理', role: 'teacher' },
      },
      {
        path: 'courses/:id',
        name: 'TeacherCourseDetail',
        component: () => import('@/views/teacher/CourseDetail.vue'),
        meta: { title: '章节管理', role: 'teacher' },
      },
      {
        path: 'homework',
        name: 'TeacherHomework',
        component: () => import('@/views/teacher/HomeworkManage.vue'),
        meta: { title: '作业管理', role: 'teacher' },
      },
      {
        path: 'homework/:id/grade',
        name: 'TeacherHomeworkGrade',
        component: () => import('@/views/teacher/HomeworkGrade.vue'),
        meta: { title: '批改作业', role: 'teacher' },
      },
      {
        path: 'notices',
        name: 'TeacherNotices',
        component: () => import('@/views/teacher/NoticePublish.vue'),
        meta: { title: '公告发布', role: 'teacher' },
      },
    ],
  },
]

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login, meta: { public: true } },
  {
    path: '/profile',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Profile',
        component: Profile,
        meta: { title: '个人中心' },
      },
    ],
  },
  ...studentRoutes,
  ...teacherRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { public: true, title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const loginTimeRaw = localStorage.getItem('loginTime')
  const loginTime = Number(loginTimeRaw)
  const expireTime = 24 * 60 * 60 * 1000
  const expired = !loginTimeRaw || Number.isNaN(loginTime) || Date.now() - loginTime > expireTime

  if (to.name === 'NotFound') {
    return next()
  }

  if (to.meta.public) {
    return token && !expired ? redirectByRole(next) : next()
  }

  if (!token || expired) {
    clearAuthStorage()
    return next('/login')
  }

  const user = getUserInfo()
  if (!user) {
    clearAuthStorage()
    return next('/login')
  }

  const requiredRole = to.meta.role as string | undefined
  if (requiredRole && user.role !== requiredRole) {
    return redirectByRole(next)
  }

  next()
})

function redirectByRole(next: (path?: string) => void) {
  const user = getUserInfo()
  if (user?.role === 'teacher') return next('/teacher/courses')
  if (user?.role === 'student') return next('/student/courses')
  clearAuthStorage()
  return next('/login')
}

export default router
