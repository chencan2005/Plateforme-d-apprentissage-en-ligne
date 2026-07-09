<template>
  <div class="not-found">
    <div class="panel">
      <p class="kicker">404</p>
      <h1>页面走丢了</h1>
      <p>你访问的地址不存在，或内容已被移除。</p>
      <el-button type="primary" @click="goHome">返回工作台</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { getUserInfo } from '@/utils/user'

const router = useRouter()

function goHome() {
  const user = getUserInfo()
  if (user?.role === 'teacher') {
    router.push('/teacher/courses')
  } else if (user?.role === 'student') {
    router.push('/student/courses')
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at top, rgba(15, 118, 110, 0.12), transparent 40%),
    var(--bg-page);
}
.panel {
  width: min(440px, 100%);
  text-align: center;
  padding: 40px 28px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
}
.kicker {
  margin: 0 0 8px;
  font-size: 13px;
  letter-spacing: 0.12em;
  color: var(--primary-color);
  font-weight: 700;
}
.panel h1 {
  margin: 0 0 10px;
  font-size: 32px;
  font-family: var(--font-display);
}
.panel p {
  margin: 0 0 22px;
  color: var(--text-secondary);
}
</style>
