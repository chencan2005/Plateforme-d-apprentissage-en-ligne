<template>
  <div class="page-container" v-loading="pageLoading">
    <div class="page-card">
      <div class="page-header">
        <div class="header-left">
          <el-button class="back-link" text type="primary" @click="router.push('/student/courses')">← 返回课程中心</el-button>
          <h2>{{ course?.courseName || '课程详情' }}</h2>
          <p v-if="course?.courseIntro" class="section-desc">{{ course.courseIntro }}</p>
        </div>
        <div v-if="course" class="progress-chip">
          <span>学习进度</span>
          <strong>{{ progressPercent }}%</strong>
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

      <el-tabs v-model="activeTab">
        <el-tab-pane label="章节列表" name="chapters">
          <el-collapse v-if="chapters.length">
            <el-collapse-item
              v-for="chapter in chapters"
              :key="chapter.id"
              :name="chapter.id"
            >
              <template #title>
                <span>{{ chapter.title }}</span>
                <el-tag
                  :type="chapter.learned ? 'success' : 'info'"
                  size="small"
                  style="margin-left: 12px"
                >
                  {{ chapter.learned ? '已学' : '未学' }}
                </el-tag>
              </template>

              <div class="chapter-content">
                <template v-if="chapter.contentType === 'video'">
                  <p>视频链接：</p>
                  <el-link :href="chapter.content" target="_blank" type="primary">
                    {{ chapter.content }}
                  </el-link>
                </template>
                <template v-else>
                  <p style="white-space: pre-wrap">{{ chapter.content }}</p>
                </template>
                <el-button
                  v-if="!chapter.learned"
                  type="success"
                  size="small"
                  :loading="markingId === chapter.id"
                  style="margin-top: 12px"
                  @click="markLearned(chapter.id)"
                >
                  标记为已学
                </el-button>
              </div>
            </el-collapse-item>
          </el-collapse>
          <div v-else class="empty-tip">暂无章节</div>
        </el-tab-pane>

        <el-tab-pane label="作业" name="homework">
          <div v-for="hw in homeworkList" :key="hw.id" class="homework-item">
            <div class="hw-header">
              <h4>{{ hw.title }}</h4>
              <el-tag v-if="hw.score != null" type="success" size="small">已批改 {{ hw.score }} 分</el-tag>
              <el-tag v-else-if="hw.submitted" type="warning" size="small">已提交</el-tag>
              <el-tag v-else type="info" size="small">未提交</el-tag>
            </div>
            <p v-if="hw.deadline" class="hw-deadline">截止时间：{{ hw.deadline }}</p>

            <template v-if="hw.submitted">
              <p class="hw-label">我的作答</p>
              <p class="hw-answer">{{ hw.answer || '—' }}</p>
              <template v-if="hw.score != null">
                <p class="hw-label">教师评语</p>
                <p class="hw-feedback">{{ hw.feedback || '无评语' }}</p>
              </template>
              <p v-else class="hw-pending">等待教师批改</p>
            </template>
            <template v-else>
              <el-input
                v-model="submitContents[hw.id]"
                type="textarea"
                :rows="4"
                placeholder="请输入作业答案"
              />
              <el-button
                type="primary"
                size="small"
                :loading="submittingId === hw.id"
                style="margin-top: 8px"
                @click="submitHomework(hw.id)"
              >
                提交作业
              </el-button>
            </template>
          </div>
          <div v-if="homeworkList.length === 0" class="empty-tip">暂无作业</div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourseDetail, getChapters, markChapterLearned } from '@/api/course'
import { getStudentHomeworkList, submitHomework as submitHomeworkApi } from '@/api/homework'
import type { Course, Chapter, Homework } from '@/types'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const loadError = ref('')
const activeTab = ref('chapters')
const course = ref<Course | null>(null)
const chapters = ref<Chapter[]>([])
const homeworkList = ref<Homework[]>([])
const markingId = ref<number | null>(null)
const submittingId = ref<number | null>(null)
const submitContents = reactive<Record<number, string>>({})

const progressPercent = computed(() => {
  if (!chapters.value.length) return 0
  const learned = chapters.value.filter((c) => c.learned).length
  return Math.round((learned / chapters.value.length) * 100)
})

async function loadData(id: number) {
  if (!Number.isFinite(id) || id <= 0) {
    loadError.value = '无效的课程 ID'
    course.value = null
    chapters.value = []
    homeworkList.value = []
    return
  }

  pageLoading.value = true
  loadError.value = ''
  try {
    const [courseRes, chapterRes, hwRes] = await Promise.all([
      getCourseDetail(id),
      getChapters(id),
      getStudentHomeworkList(id),
    ])
    course.value = courseRes.data
    chapters.value = chapterRes.data
    homeworkList.value = hwRes.data
    for (const hw of hwRes.data) {
      if (hw.answer) submitContents[hw.id] = hw.answer
    }
  } catch {
    loadError.value = '加载课程失败，请稍后重试'
  } finally {
    pageLoading.value = false
  }
}

async function markLearned(chapterId: number) {
  markingId.value = chapterId
  try {
    await markChapterLearned(chapterId)
    const ch = chapters.value.find((c) => c.id === chapterId)
    if (ch) ch.learned = true
    ElMessage.success('已标记为已学')
  } catch {
    /* 错误已提示 */
  } finally {
    markingId.value = null
  }
}

async function submitHomework(homeworkId: number) {
  const content = submitContents[homeworkId]?.trim()
  if (!content) {
    ElMessage.warning('请输入作业内容')
    return
  }
  submittingId.value = homeworkId
  try {
    await submitHomeworkApi({ homeworkId, content })
    ElMessage.success('作业提交成功')
    const hw = homeworkList.value.find((h) => h.id === homeworkId)
    if (hw) {
      hw.submitted = true
      hw.answer = content
    }
  } catch {
    /* 错误已提示 */
  } finally {
    submittingId.value = null
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
  max-width: 720px;
}
.header-left h2 {
  margin: 0;
}
.progress-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  padding: 10px 14px;
  border-radius: 12px;
  background: var(--primary-light);
  color: var(--primary-dark);
  min-width: 88px;
}
.progress-chip span {
  font-size: 12px;
}
.progress-chip strong {
  font-size: 22px;
  font-family: var(--font-display);
}
.chapter-content {
  padding: 8px 0 4px;
  color: var(--text-regular);
  line-height: 1.7;
}
.homework-item {
  padding: 18px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
  background: linear-gradient(180deg, #fff, #f8fafc);
}
.hw-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.hw-header h4 {
  margin: 0;
  font-size: 16px;
  font-family: var(--font-display);
}
.hw-deadline {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 8px 0;
}
.hw-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 12px 0 4px;
}
.hw-answer,
.hw-feedback {
  white-space: pre-wrap;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.7;
}
.hw-pending {
  color: var(--warning-color);
  font-size: 13px;
  margin-top: 8px;
}
</style>
