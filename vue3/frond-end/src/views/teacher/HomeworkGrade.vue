<template>
  <div class="page-container" v-loading="pageLoading">
    <div class="page-card">
      <div class="page-header">
        <div class="header-left">
          <el-button class="back-link" text type="primary" @click="router.push('/teacher/homework')">← 返回作业管理</el-button>
          <h2>批改作业</h2>
          <p class="section-desc">查看学生提交，支持人工打分与 AI 辅助评分</p>
        </div>
      </div>

      <el-alert
        v-if="loadError"
        :title="loadError"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-table :data="submissions" border stripe style="margin-top: 8px">
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="content" label="提交内容" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="分数" width="80">
          <template #default="{ row }">
            {{ row.score ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" link @click="openGradeDialog(row)">打分</el-button>
            <el-button type="success" link :loading="aiGradingId === row.id" @click="handleAiGrade(row)">
              AI 打分
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!pageLoading && submissions.length === 0" class="empty-tip">暂无学生提交</div>
    </div>

    <el-dialog v-model="dialogVisible" title="批改作业" width="480px">
      <el-form :model="gradeForm" label-width="60px">
        <el-form-item label="分数">
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="gradeForm.comment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="grading" @click="submitGrade">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSubmissions, gradeHomework, aiGradeHomework } from '@/api/homework'
import type { HomeworkSubmission } from '@/types'

const route = useRoute()
const router = useRouter()

const homeworkId = ref(0)
const pageLoading = ref(false)
const loadError = ref('')
const grading = ref(false)
const aiGradingId = ref<number | null>(null)
const submissions = ref<HomeworkSubmission[]>([])
const dialogVisible = ref(false)
const currentSubmission = ref<HomeworkSubmission | null>(null)
const gradeForm = reactive({ score: 0, comment: '' })

async function loadSubmissions(id: number) {
  if (!Number.isFinite(id) || id <= 0) {
    loadError.value = '无效的作业 ID'
    submissions.value = []
    homeworkId.value = 0
    return
  }

  homeworkId.value = id
  pageLoading.value = true
  loadError.value = ''
  try {
    const res = await getSubmissions(id)
    submissions.value = res.data
  } catch {
    loadError.value = '加载提交列表失败'
    submissions.value = []
  } finally {
    pageLoading.value = false
  }
}

function openGradeDialog(row: HomeworkSubmission) {
  currentSubmission.value = row
  gradeForm.score = row.score ?? 0
  gradeForm.comment = row.comment ?? ''
  dialogVisible.value = true
}

async function submitGrade() {
  if (!currentSubmission.value) return
  grading.value = true
  try {
    await gradeHomework({
      submissionId: currentSubmission.value.id,
      score: gradeForm.score,
      comment: gradeForm.comment,
    })
    ElMessage.success('批改成功')
    dialogVisible.value = false
    loadSubmissions(homeworkId.value)
  } catch {
    /* 错误已提示 */
  } finally {
    grading.value = false
  }
}

async function handleAiGrade(row: HomeworkSubmission) {
  aiGradingId.value = row.id
  try {
    const res = await aiGradeHomework(row.id)
    gradeForm.score = res.data.score
    gradeForm.comment = res.data.comment
    currentSubmission.value = row
    dialogVisible.value = true
    ElMessage.success('AI 打分完成，请确认后提交')
  } catch {
    /* 错误已提示 */
  } finally {
    aiGradingId.value = null
  }
}

watch(
  () => route.params.id,
  (id) => loadSubmissions(Number(id)),
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
