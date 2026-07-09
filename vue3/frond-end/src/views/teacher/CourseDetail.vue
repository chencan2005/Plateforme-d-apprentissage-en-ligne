<template>
  <div class="page-container" v-loading="pageLoading">
    <div class="page-card">
      <div class="page-header">
        <div class="header-left">
          <el-button class="back-link" text type="primary" @click="router.push('/teacher/courses')">← 返回课程管理</el-button>
          <h2>{{ courseName || '章节管理' }}</h2>
          <p class="section-desc">维护章节内容与排序，供学生按进度学习</p>
        </div>
        <el-button type="primary" :disabled="!courseId" @click="openDialog()">新增章节</el-button>
      </div>

      <el-alert
        v-if="loadError"
        :title="loadError"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-table :data="chapters" border stripe>
        <el-table-column prop="title" label="章节标题" />
        <el-table-column label="内容类型" width="100">
          <template #default="{ row }">
            {{ row.contentType === 'video' ? '视频' : '文本' }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!pageLoading && chapters.length === 0" class="empty-tip">暂无章节，请添加</div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑章节' : '新增章节'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="章节标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容类型">
          <el-radio-group v-model="form.contentType">
            <el-radio value="text">文本</el-radio>
            <el-radio value="video">视频链接</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.contentType === 'video' ? '视频链接' : '内容描述'">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="form.contentType === 'video' ? 2 : 6"
            :placeholder="form.contentType === 'video' ? '请输入视频 URL' : '请输入章节内容'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCourseDetail, getChapters, createChapter, updateChapter, deleteChapter,
} from '@/api/course'
import type { Chapter } from '@/types'

const route = useRoute()
const router = useRouter()

const courseId = ref(0)
const pageLoading = ref(false)
const loadError = ref('')
const saving = ref(false)
const courseName = ref('')
const chapters = ref<Chapter[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  title: '',
  content: '',
  contentType: 'text' as 'text' | 'video',
})

async function loadData(id: number) {
  if (!Number.isFinite(id) || id <= 0) {
    loadError.value = '无效的课程 ID'
    courseName.value = ''
    chapters.value = []
    courseId.value = 0
    return
  }

  courseId.value = id
  pageLoading.value = true
  loadError.value = ''
  try {
    const [courseRes, chapterRes] = await Promise.all([
      getCourseDetail(id),
      getChapters(id),
    ])
    courseName.value = courseRes.data.courseName
    chapters.value = chapterRes.data
  } catch {
    loadError.value = '加载章节失败，请稍后重试'
  } finally {
    pageLoading.value = false
  }
}

function openDialog(row?: Chapter) {
  if (row) {
    editingId.value = row.id
    form.title = row.title
    form.content = row.content
    form.contentType = row.contentType
  } else {
    editingId.value = null
    form.title = ''
    form.content = ''
    form.contentType = 'text'
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('请填写完整信息')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateChapter(editingId.value, { ...form, courseId: courseId.value })
      ElMessage.success('章节更新成功')
    } else {
      await createChapter({ ...form, courseId: courseId.value })
      ElMessage.success('章节添加成功')
    }
    dialogVisible.value = false
    loadData(courseId.value)
  } catch {
    /* 错误已提示 */
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该章节吗？', '警告', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteChapter(id)
    ElMessage.success('删除成功')
    loadData(courseId.value)
  } catch {
    /* 错误已提示 */
  }
}

watch(
  () => route.params.id,
  (id) => loadData(Number(id)),
  { immediate: true },
)
</script>

<style scoped>
.header-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.header-left h2 {
  margin: 0;
}
</style>
