<template>
  <div class="page-container">
    <div class="profile-hero page-card">
      <div class="avatar-wrap">
        <el-avatar :size="72" class="profile-avatar">{{ avatarText }}</el-avatar>
      </div>
      <div>
        <p class="hero-kicker">个人中心</p>
        <h2>{{ userInfo.username || '未登录用户' }}</h2>
        <p class="section-desc">{{ roleLabel(userInfo.role) }} · 管理账号信息与登录密码</p>
      </div>
    </div>

    <div class="profile-grid">
      <div class="page-card">
        <div class="page-header">
          <div>
            <h2>账号信息</h2>
            <p class="section-desc">当前登录身份与注册时间</p>
          </div>
        </div>
        <div class="info-list">
          <div class="info-row">
            <span>用户名</span>
            <strong>{{ userInfo.username }}</strong>
          </div>
          <div class="info-row">
            <span>角色</span>
            <strong>{{ roleLabel(userInfo.role) }}</strong>
          </div>
          <div class="info-row">
            <span>注册时间</span>
            <strong>{{ userInfo.createdTime || '—' }}</strong>
          </div>
        </div>
      </div>

      <div class="page-card">
        <div class="page-header">
          <div>
            <h2>修改密码</h2>
            <p class="section-desc">修改成功后需要重新登录</p>
          </div>
        </div>
        <el-form :model="pwdForm" label-width="100px" class="pwd-form">
          <el-form-item label="旧密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleChangePassword">
              确认修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserProfile, changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { roleLabel } from '@/utils/user'
import type { UserInfo } from '@/types'

const userStore = useUserStore()
const loading = ref(false)
const userInfo = ref<UserInfo>({ username: '', role: 'student' })
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const avatarText = computed(() => userInfo.value.username?.charAt(0)?.toUpperCase() || 'U')

onMounted(async () => {
  if (userStore.userInfo) {
    userInfo.value = { ...userStore.userInfo }
  }
  try {
    const res = await getUserProfile()
    userInfo.value = res.data
    userStore.updateUser(res.data)
  } catch {
    /* 使用本地缓存 */
  }
})

async function handleChangePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请完整填写密码项')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次新密码不一致')
    return
  }

  loading.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  } catch {
    /* 错误已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  background:
    radial-gradient(circle at right top, rgba(15, 118, 110, 0.1), transparent 40%),
    #fff;
}
.profile-hero::before {
  opacity: 1;
}
.hero-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--primary-color);
  font-weight: 600;
}
.profile-hero h2 {
  margin: 0;
  font-size: 26px;
}
.profile-avatar {
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  font-size: 28px;
  font-weight: 700;
}
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 12px;
  background: var(--bg-muted);
  border: 1px solid var(--border-color);
}
.info-row span {
  color: var(--text-secondary);
  font-size: 13px;
}
.info-row strong {
  color: var(--text-primary);
  font-weight: 600;
}
.pwd-form {
  max-width: 100%;
}
@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
