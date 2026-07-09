<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>公告中心</h2>
          <p class="section-desc">系统与课程通知集中查看</p>
        </div>
        <el-input
          v-model="searchTitle"
          placeholder="搜索公告标题"
          clearable
          style="width: 260px"
          @keyup.enter="loadNotices"
          @clear="loadNotices"
        >
          <template #append>
            <el-button @click="loadNotices">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div v-loading="loading" class="notice-list">
        <button
          v-for="item in notices"
          :key="item.id"
          class="notice-item"
          type="button"
          @click="openDetail(item)"
        >
          <div class="notice-main">
            <h3>{{ item.title }}</h3>
            <p>{{ item.publisherName }} · {{ roleLabel(item.publisherRole) }}</p>
          </div>
          <time>{{ item.createdTime }}</time>
        </button>
      </div>
      <EmptyState v-if="!loading && notices.length === 0" description="暂无公告" :retryable="true" @retry="loadNotices" />
    </div>

    <el-dialog v-model="detailVisible" :title="currentNotice?.title" width="560px">
      <div v-if="currentNotice" class="notice-detail">
        <p class="notice-meta">
          {{ currentNotice.publisherName }} · {{ roleLabel(currentNotice.publisherRole) }} ·
          {{ currentNotice.createdTime }}
        </p>
        <div class="notice-content">{{ currentNotice.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNoticeList, getNoticeDetail } from '@/api/notice'
import { roleLabel } from '@/utils/user'
import EmptyState from '@/components/EmptyState.vue'
import type { Notice } from '@/types'

const loading = ref(false)
const searchTitle = ref('')
const notices = ref<Notice[]>([])
const detailVisible = ref(false)
const currentNotice = ref<Notice | null>(null)

async function loadNotices() {
  loading.value = true
  try {
    const res = await getNoticeList({ title: searchTitle.value || undefined })
    notices.value = res.data
  } catch {
    notices.value = []
  } finally {
    loading.value = false
  }
}

async function openDetail(row: Notice) {
  try {
    const res = await getNoticeDetail(row.id)
    currentNotice.value = res.data
  } catch {
    currentNotice.value = row
  }
  detailVisible.value = true
}

onMounted(loadNotices)
</script>

<style scoped>
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 120px;
}
.notice-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  width: 100%;
  text-align: left;
  padding: 16px 18px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}
.notice-item:hover {
  border-color: #99f6e4;
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}
.notice-main h3 {
  margin: 0 0 6px;
  font-size: 15px;
  font-family: var(--font-display);
  color: var(--text-primary);
}
.notice-main p,
.notice-item time {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}
.notice-item time {
  white-space: nowrap;
}
.notice-meta {
  color: var(--text-muted);
  margin: 0 0 14px;
  font-size: 13px;
}
.notice-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-regular);
}
</style>
