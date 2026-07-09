<template>
  <div class="page-container">
    <div class="hero-panel">
      <div>
        <p class="hero-kicker">学习空间</p>
        <h1>发现课程，跟进进度</h1>
        <p class="hero-desc">浏览在线课程、管理已选内容，把学习节奏掌握在自己手里。</p>
      </div>
      <div class="hero-stats">
        <div class="stat-item">
          <strong>{{ onlineCourses.length }}</strong>
          <span>在线课程</span>
        </div>
        <div class="stat-item">
          <strong>{{ enrolledCourses.length }}</strong>
          <span>已选课程</span>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="page-card course-tabs">
      <el-tab-pane label="在线课程" name="online">
        <div class="page-header">
          <div>
            <h2>全部课程</h2>
            <p class="section-desc">按名称搜索，快速找到想学的内容</p>
          </div>
          <div class="search-row">
            <el-input
              v-model="searchName"
              placeholder="搜索课程名称"
              clearable
              style="width: 240px"
              @keyup.enter="loadOnlineCourses"
            />
            <el-button type="primary" :loading="loading" @click="loadOnlineCourses">搜索</el-button>
          </div>
        </div>

        <el-row :gutter="20" v-loading="loading">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in onlineCourses" :key="course.id">
            <el-card shadow="never" class="course-card">
              <div class="course-card-cover" />
              <div class="course-card-inner">
                <div class="course-title-row">
                  <h3>{{ course.courseName }}</h3>
                  <el-tag v-if="isEnrolled(course.id)" type="success" size="small" effect="plain" round>已选</el-tag>
                </div>
                <p class="course-intro">{{ course.courseIntro }}</p>
                <p class="course-teacher">授课教师 · {{ course.teacherName || '—' }}</p>
                <el-button
                  v-if="isEnrolled(course.id)"
                  type="success"
                  size="small"
                  plain
                  @click="goToCourse(course.id)"
                >
                  进入学习
                </el-button>
                <el-button v-else type="primary" size="small" @click="openCourseDialog(course)">
                  查看并选课
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <EmptyState
          v-if="!loading && onlineCourses.length === 0"
          description="暂无在线课程"
          :retryable="true"
          @retry="loadOnlineCourses"
        />
      </el-tab-pane>

      <el-tab-pane label="已选课程" name="enrolled">
        <div class="page-header">
          <div>
            <h2>我的课程</h2>
            <p class="section-desc">查看学习进度，继续未完成的章节</p>
          </div>
        </div>
        <el-table v-loading="enrolledLoading" :data="enrolledCourses" border stripe>
          <el-table-column prop="courseName" label="课程名称" min-width="180" />
          <el-table-column prop="teacherName" label="授课教师" width="140" />
          <el-table-column label="学习进度" width="220">
            <template #default="{ row }">
              <el-progress :percentage="row.progress ?? 0" :stroke-width="10" striped />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="goToCourse(row.id)">进入学习</el-button>
            </template>
          </el-table-column>
        </el-table>
        <EmptyState
          v-if="!enrolledLoading && enrolledCourses.length === 0"
          description="暂无已选课程，去「在线课程」选一门吧"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" title="课程信息" width="480px" class="course-dialog">
      <div v-if="selectedCourse" class="dialog-body">
        <div class="dialog-cover" />
        <h3>{{ selectedCourse.courseName }}</h3>
        <p class="dialog-intro">{{ selectedCourse.courseIntro }}</p>
        <p class="dialog-meta">授课教师：{{ selectedCourse.teacherName || '—' }}</p>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="enrollLoading" @click="handleEnroll">确认选课</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllCourses, getEnrolledCourses, enrollCourse } from '@/api/course'
import EmptyState from '@/components/EmptyState.vue'
import type { Course } from '@/types'

const router = useRouter()
const activeTab = ref('online')
const loading = ref(false)
const enrolledLoading = ref(false)
const enrollLoading = ref(false)
const searchName = ref('')
const onlineCourses = ref<Course[]>([])
const enrolledCourses = ref<Course[]>([])
const dialogVisible = ref(false)
const selectedCourse = ref<Course | null>(null)

const enrolledIdSet = computed(() => new Set(enrolledCourses.value.map((c) => c.id)))

function isEnrolled(id: number) {
  return enrolledIdSet.value.has(id)
}

async function loadOnlineCourses() {
  loading.value = true
  try {
    const res = await getAllCourses({ courseName: searchName.value || undefined })
    onlineCourses.value = res.data
  } catch {
    onlineCourses.value = []
  } finally {
    loading.value = false
  }
}

async function loadEnrolledCourses() {
  enrolledLoading.value = true
  try {
    const res = await getEnrolledCourses()
    enrolledCourses.value = res.data
  } catch {
    enrolledCourses.value = []
  } finally {
    enrolledLoading.value = false
  }
}

function openCourseDialog(course: Course) {
  selectedCourse.value = course
  dialogVisible.value = true
}

async function handleEnroll() {
  if (!selectedCourse.value) return
  try {
    await ElMessageBox.confirm(
      `确认选修「${selectedCourse.value.courseName}」吗？`,
      '选课确认',
      { type: 'info', confirmButtonText: '确认选课', cancelButtonText: '取消' },
    )
  } catch {
    return
  }

  enrollLoading.value = true
  try {
    await enrollCourse(selectedCourse.value.id)
    ElMessage.success('选课成功')
    dialogVisible.value = false
    const id = selectedCourse.value.id
    await loadEnrolledCourses()
    router.push(`/student/courses/${id}`)
  } catch {
    /* 错误已提示 */
  } finally {
    enrollLoading.value = false
  }
}

function goToCourse(id: number) {
  router.push(`/student/courses/${id}`)
}

onMounted(() => {
  loadOnlineCourses()
  loadEnrolledCourses()
})
</script>

<style scoped>
.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-end;
  margin-bottom: 20px;
  padding: 28px 28px 26px;
  border-radius: var(--radius-xl);
  color: #fff;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.18), transparent 40%),
    linear-gradient(135deg, #0f766e 0%, #0d9488 55%, #14b8a6 100%);
  box-shadow: var(--shadow-md);
}

.hero-kicker {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.8;
  margin-bottom: 8px;
  font-weight: 600;
}

.hero-panel h1 {
  font-size: 28px;
  margin: 0 0 8px;
  font-family: var(--font-display);
}

.hero-desc {
  margin: 0;
  max-width: 420px;
  opacity: 0.88;
  line-height: 1.6;
  font-size: 14px;
}

.hero-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  min-width: 96px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.14);
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 24px;
  font-family: var(--font-display);
  margin-bottom: 4px;
}

.stat-item span {
  font-size: 12px;
  opacity: 0.85;
}

.course-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.search-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.course-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.course-title-row h3 {
  margin: 0;
  flex: 1;
}

.dialog-body h3 {
  margin: 16px 0 8px;
  font-size: 20px;
  font-family: var(--font-display);
}

.dialog-cover {
  height: 84px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0f766e, #2dd4bf);
}

.dialog-intro {
  margin: 0 0 12px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.dialog-meta {
  margin: 0;
  color: var(--text-regular);
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero-panel {
    flex-direction: column;
    align-items: stretch;
  }
  .hero-stats {
    width: 100%;
  }
  .stat-item {
    flex: 1;
  }
}
</style>
