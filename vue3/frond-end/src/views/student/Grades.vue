<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>成绩查询</h2>
          <p class="section-desc">按课程查看已批改作业成绩</p>
        </div>
        <el-select
          v-model="filterCourseId"
          placeholder="按课程筛选"
          clearable
          style="width: 220px"
          @change="loadGrades"
        >
          <el-option
            v-for="c in courseOptions"
            :key="c.id"
            :label="c.courseName"
            :value="c.id"
          />
        </el-select>
      </div>

      <div v-loading="loading" class="grade-list">
        <div v-for="item in grades" :key="item.courseId" class="grade-card">
          <div class="grade-card-head">
            <h3>{{ item.courseName }}</h3>
            <span>{{ item.homeworkGrades.length }} 条成绩</span>
          </div>
          <div class="grade-tags">
            <div
              v-for="hw in item.homeworkGrades"
              :key="hw.homeworkId"
              class="grade-pill"
              :class="hw.score != null ? 'scored' : 'pending'"
            >
              <span class="pill-title">{{ hw.title }}</span>
              <strong>{{ hw.score != null ? `${hw.score} 分` : '未批改' }}</strong>
            </div>
            <p v-if="!item.homeworkGrades?.length" class="empty-inline">暂无作业成绩</p>
          </div>
        </div>
      </div>
      <EmptyState v-if="!loading && grades.length === 0" description="暂无成绩数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGrades } from '@/api/homework'
import { getEnrolledCourses } from '@/api/course'
import EmptyState from '@/components/EmptyState.vue'
import type { GradeRecord, Course } from '@/types'

const loading = ref(false)
const filterCourseId = ref<number | undefined>()
const grades = ref<GradeRecord[]>([])
const courseOptions = ref<Course[]>([])

async function loadGrades() {
  loading.value = true
  try {
    const res = await getGrades({ courseId: filterCourseId.value })
    grades.value = res.data
  } catch {
    grades.value = []
  } finally {
    loading.value = false
  }
}

async function loadCourses() {
  try {
    const res = await getEnrolledCourses()
    courseOptions.value = res.data
  } catch {
    courseOptions.value = []
  }
}

onMounted(() => {
  loadCourses()
  loadGrades()
})
</script>

<style scoped>
.grade-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 120px;
}
.grade-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  background: #fff;
}
.grade-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  gap: 12px;
}
.grade-card-head h3 {
  margin: 0;
  font-size: 17px;
  font-family: var(--font-display);
}
.grade-card-head span {
  font-size: 12px;
  color: var(--text-muted);
}
.grade-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.grade-pill {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 160px;
  max-width: 280px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
}
.grade-pill.scored {
  background: #ecfdf5;
  border-color: #a7f3d0;
}
.grade-pill.pending {
  background: #f8fafc;
}
.pill-title {
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.grade-pill strong {
  font-size: 16px;
  font-family: var(--font-display);
  color: var(--text-primary);
}
.empty-inline {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
}
</style>
