<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>公告发布</h2>
          <p class="section-desc">向学生同步系统或课程通知</p>
        </div>
        <el-button type="primary" @click="openPublishDialog">发布公告</el-button>
      </div>

      <el-table v-loading="loading" :data="notices" border stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column prop="createdTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && notices.length === 0" description="暂无公告" />
    </div>

    <el-dialog v-model="publishVisible" title="发布公告" width="520px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="currentNotice?.title" width="560px">
      <div v-if="currentNotice" class="notice-content">
        {{ currentNotice.content }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNoticeList, publishNotice } from '@/api/notice'
import EmptyState from '@/components/EmptyState.vue'
import type { Notice } from '@/types'

const loading = ref(false)
const publishing = ref(false)
const notices = ref<Notice[]>([])
const publishVisible = ref(false)
const detailVisible = ref(false)
const currentNotice = ref<Notice | null>(null)
const form = reactive({ title: '', content: '' })

async function loadNotices() {
  loading.value = true
  try {
    const res = await getNoticeList()
    notices.value = res.data
  } catch {
    notices.value = []
  } finally {
    loading.value = false
  }
}

function openPublishDialog() {
  form.title = ''
  form.content = ''
  publishVisible.value = true
}

async function handlePublish() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  publishing.value = true
  try {
    await publishNotice(form)
    ElMessage.success('发布成功')
    publishVisible.value = false
    loadNotices()
  } catch {
    /* 错误已提示 */
  } finally {
    publishing.value = false
  }
}

function viewDetail(row: Notice) {
  currentNotice.value = row
  detailVisible.value = true
}

onMounted(loadNotices)
</script>

<style scoped>
.notice-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-regular);
}
</style>
