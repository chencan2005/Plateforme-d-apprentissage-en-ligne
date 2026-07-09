<template>
  <div class="auth-page">
    <div class="auth-banner">
      <div class="banner-glow" />
      <div class="banner-grid" />
      <div class="banner-content">
        <div class="brand-mark">
          <el-icon class="banner-icon"><Reading /></el-icon>
          <span>LearnHub</span>
        </div>
        <h1>智慧教学平台</h1>
        <p class="banner-lead">把课程、作业与 AI 辅导放在同一处，让教与学更顺畅。</p>
        <ul class="feature-list">
          <li>
            <span class="feature-dot" />
            学生在线选课、跟进度、交作业
          </li>
          <li>
            <span class="feature-dot" />
            教师发布课程、批改与公告
          </li>
          <li>
            <span class="feature-dot" />
            AI 问答与知识点总结随时可用
          </li>
        </ul>
      </div>
    </div>

    <div class="auth-panel">
      <div class="auth-box" v-if="showLogin">
        <p class="auth-kicker">欢迎回来</p>
        <h2 class="auth-title">登录账号</h2>
        <p class="auth-subtitle">使用演示账号或你的注册账号进入平台</p>

        <div class="auth-input" :class="{ error: errors.username }">
          <el-icon><User /></el-icon>
          <input
            v-model="loginForm.username"
            name="username"
            autocomplete="username"
            placeholder="请输入用户名"
            @keyup.enter="handleLogin"
          />
        </div>
        <div v-if="errors.username" class="error-msg">{{ errors.username }}</div>

        <div class="auth-input" :class="{ error: errors.password }">
          <el-icon><Lock /></el-icon>
          <input
            v-model="loginForm.password"
            type="password"
            name="password"
            autocomplete="current-password"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </div>
        <div v-if="errors.password" class="error-msg">{{ errors.password }}</div>

        <el-button type="primary" class="auth-btn" :loading="loading" @click="handleLogin">
          进入平台
        </el-button>
        <div class="switch-link" @click="showLogin = false">没有账号？立即注册</div>
        <p class="demo-hint">演示：teacher_zhang / student_wang · 密码 123456</p>
      </div>

      <div class="auth-box" v-else>
        <div class="back-btn" @click="showLogin = true">
          <el-icon><ArrowLeft /></el-icon>
          返回登录
        </div>
        <p class="auth-kicker">创建账号</p>
        <h2 class="auth-title">欢迎注册</h2>
        <p class="auth-subtitle">选择角色后即可开始使用</p>

        <div class="auth-input">
          <el-icon><User /></el-icon>
          <input
            v-model="registerForm.username"
            name="username"
            autocomplete="username"
            placeholder="请输入用户名"
          />
        </div>

        <div class="auth-input">
          <el-icon><Lock /></el-icon>
          <input
            v-model="registerForm.password"
            type="password"
            name="new-password"
            autocomplete="new-password"
            placeholder="密码（至少6位）"
          />
        </div>

        <div class="auth-input">
          <el-icon><Lock /></el-icon>
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            name="confirm-password"
            autocomplete="new-password"
            placeholder="确认密码"
          />
        </div>

        <div class="auth-input">
          <el-icon><Avatar /></el-icon>
          <select v-model="registerForm.role" class="role-select">
            <option value="">请选择角色</option>
            <option value="student">学生</option>
            <option value="teacher">教师</option>
          </select>
        </div>

        <el-button type="primary" class="auth-btn" :loading="loading" @click="handleRegister">
          立即注册
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar, ArrowLeft, Reading } from '@element-plus/icons-vue'
import { login, register } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const showLogin = ref(true)
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: '' as '' | 'student' | 'teacher',
})
const errors = reactive({ username: '', password: '' })

function validateLogin() {
  errors.username = ''
  errors.password = ''
  let valid = true
  if (!loginForm.username.trim()) {
    errors.username = '用户名不能为空'
    valid = false
  }
  if (!loginForm.password) {
    errors.password = '密码不能为空'
    valid = false
  } else if (loginForm.password.length < 6) {
    errors.password = '密码长度不能小于6位'
    valid = false
  }
  return valid
}

async function handleLogin() {
  if (!validateLogin()) return
  loading.value = true
  try {
    const res = await login(loginForm)
    userStore.setUser(res.data.userInfo, res.data.token)
    ElMessage.success('登录成功')
    if (res.data.userInfo.role === 'teacher') {
      router.push('/teacher/courses')
    } else {
      router.push('/student/courses')
    }
  } catch {
    /* 错误已在拦截器中提示 */
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!registerForm.password || registerForm.password.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  if (!registerForm.role) {
    ElMessage.warning('请选择角色')
    return
  }

  loading.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      role: registerForm.role,
    })
    ElMessage.success('注册成功，请登录')
    showLogin.value = true
    loginForm.username = registerForm.username
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.role = ''
  } catch {
    /* 错误已在拦截器中提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  background: #f4f7f6;
}

.auth-banner {
  flex: 1.15;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #064e3b 0%, #0f766e 48%, #14b8a6 100%);
  padding: 48px;
}

.banner-glow {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.22), transparent 70%);
  top: -80px;
  right: -60px;
  pointer-events: none;
}

.banner-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(circle at center, black 30%, transparent 80%);
  pointer-events: none;
}

.banner-content {
  position: relative;
  max-width: 440px;
  color: #fff;
  z-index: 1;
  animation: fade-up 0.5s ease;
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  opacity: 0.95;
}

.banner-icon {
  font-size: 28px;
}

.banner-content h1 {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 14px;
  line-height: 1.15;
  font-family: var(--font-display);
}

.banner-lead {
  font-size: 16px;
  line-height: 1.7;
  opacity: 0.88;
  margin-bottom: 32px;
  max-width: 360px;
}

.feature-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.9;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(6px);
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fbbf24;
  flex-shrink: 0;
}

.auth-panel {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.06), transparent 40%),
    #fff;
}

.auth-box {
  width: 100%;
  max-width: 360px;
  animation: fade-up 0.45s ease;
}

.auth-kicker {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--primary-color);
  margin-bottom: 8px;
}

.auth-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-family: var(--font-display);
}

.auth-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 28px;
  line-height: 1.5;
}

.auth-input {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 0 14px;
  margin-bottom: 14px;
  height: 48px;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
  background: #f8fafc;
}

.auth-input:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.12);
  background: #fff;
}

.auth-input.error {
  border-color: var(--danger-color);
}

.auth-input .el-icon {
  color: var(--text-secondary);
  font-size: 18px;
}

.auth-input input,
.role-select {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  background: transparent;
  height: 100%;
  color: var(--text-primary);
  font-family: var(--font-body);
}

.role-select {
  cursor: pointer;
}

.error-msg {
  color: var(--danger-color);
  font-size: 12px;
  margin: -8px 0 10px 4px;
}

.auth-btn {
  width: 100%;
  height: 48px;
  margin-top: 8px;
  font-size: 15px;
  border-radius: 12px;
  font-weight: 600;
}

.switch-link {
  text-align: center;
  margin-top: 20px;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.switch-link:hover {
  color: var(--primary-dark);
}

.demo-hint {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
  cursor: pointer;
  margin-bottom: 16px;
  font-size: 14px;
}

.back-btn:hover {
  color: var(--primary-color);
}

@media (max-width: 900px) {
  .auth-banner {
    display: none;
  }
  .auth-panel {
    width: 100%;
  }
}
</style>
