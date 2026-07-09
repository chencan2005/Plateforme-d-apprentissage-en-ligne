<template>
  <el-container class="layout-container">
    <el-aside :width="asideWidth" class="layout-aside" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <div class="logo-mark">
          <el-icon class="logo-icon"><Reading /></el-icon>
        </div>
        <div v-show="!isCollapsed" class="logo-text">
          <strong>智慧教学</strong>
          <span>LearnHub</span>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="side-menu"
        :collapse="isCollapsed"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
      <div v-show="!isCollapsed" class="aside-footer">
        <p>专注教与学</p>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-button class="collapse-btn" text @click="toggleAside">
            <el-icon :size="18"><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          </el-button>
          <div class="title-block">
            <span class="page-title">{{ currentTitle }}</span>
            <span class="page-crumb">{{ roleLabel(userStore.role ?? undefined) }}工作台</span>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="36" class="user-avatar">{{ avatarText }}</el-avatar>
              <span class="user-meta">
                <span class="username">{{ userStore.userInfo?.username }}</span>
                <el-tag size="small" effect="plain" round>
                  {{ roleLabel(userStore.role ?? undefined) }}
                </el-tag>
              </span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <div class="main-surface">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Reading, Document, Bell, ChatDotRound, Upload,
  Management, EditPen, User, Fold, Expand,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { roleLabel } from '@/utils/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

const studentMenus = [
  { path: '/student/courses', title: '课程中心', icon: Reading },
  { path: '/student/grades', title: '成绩查询', icon: Document },
  { path: '/student/notices', title: '公告中心', icon: Bell },
  { path: '/student/ai-chat', title: 'AI智能问答', icon: ChatDotRound },
  { path: '/student/summary', title: '知识点总结', icon: Upload },
  { path: '/profile', title: '个人中心', icon: User },
]

const teacherMenus = [
  { path: '/teacher/courses', title: '课程管理', icon: Management },
  { path: '/teacher/homework', title: '作业管理', icon: EditPen },
  { path: '/teacher/notices', title: '公告发布', icon: Bell },
  { path: '/profile', title: '个人中心', icon: User },
]

const menuItems = computed(() =>
  userStore.isTeacher ? teacherMenus : studentMenus,
)

const asideWidth = computed(() => (isCollapsed.value ? '72px' : '232px'))

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/student/courses/')) return '/student/courses'
  if (path.startsWith('/teacher/courses/')) return '/teacher/courses'
  if (path.startsWith('/teacher/homework/')) return '/teacher/homework'
  return path
})

const currentTitle = computed(() => (route.meta.title as string) || '')

const avatarText = computed(() =>
  userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U',
)

function toggleAside() {
  isCollapsed.value = !isCollapsed.value
}

function syncAsideByViewport() {
  isCollapsed.value = window.innerWidth < 768
}

function handleCommand(cmd: string) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'logout') userStore.logout()
}

onMounted(() => {
  syncAsideByViewport()
  window.addEventListener('resize', syncAsideByViewport)
})

onUnmounted(() => {
  window.removeEventListener('resize', syncAsideByViewport)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
  background: var(--bg-page);
}

.layout-container > .el-container {
  flex: 1;
  min-width: 0;
}

.layout-aside {
  background:
    radial-gradient(circle at top left, rgba(45, 212, 191, 0.18), transparent 40%),
    linear-gradient(180deg, #0b3d3a 0%, #0a2f2d 100%);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.logo {
  height: 68px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: rgba(45, 212, 191, 0.18);
  flex-shrink: 0;
}

.logo-icon {
  font-size: 20px;
  color: #5eead4;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  min-width: 0;
}

.logo-text strong {
  font-size: 15px;
  font-weight: 700;
  font-family: var(--font-display);
}

.logo-text span {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.side-menu {
  border-right: none;
  background: transparent;
  padding: 12px 8px;
  flex: 1;
}

.side-menu:not(.el-menu--collapse) {
  width: 232px;
}

.side-menu :deep(.el-menu-item) {
  height: 46px;
  line-height: 46px;
  margin: 4px 4px;
  border-radius: 10px;
  color: rgba(226, 232, 240, 0.72);
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: rgba(45, 212, 191, 0.16);
  color: #5eead4;
  font-weight: 600;
}

.aside-footer {
  padding: 16px 20px 20px;
  color: rgba(255, 255, 255, 0.35);
  font-size: 12px;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  height: 64px;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.collapse-btn {
  margin-right: 4px;
}

.title-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: var(--font-display);
}

.page-crumb {
  font-size: 12px;
  color: var(--text-muted);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 12px;
  transition: background 0.2s;
  border: 1px solid transparent;
}

.user-dropdown:hover {
  background: #f8fafc;
  border-color: var(--border-color);
}

.user-avatar {
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  font-size: 14px;
  font-weight: 600;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  color: var(--text-regular);
  font-size: 14px;
  font-weight: 500;
}

.layout-main {
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.05), transparent 28%),
    var(--bg-page);
  overflow-y: auto;
  padding: 0;
  width: 100%;
}

.main-surface {
  min-height: 100%;
}

@media (max-width: 768px) {
  .username,
  .page-crumb {
    display: none;
  }
  .layout-header {
    padding: 0 12px;
  }
  .logo {
    padding: 0 12px;
    justify-content: center;
  }
}
</style>
