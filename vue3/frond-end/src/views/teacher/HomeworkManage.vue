<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>作业管理</h2>
          <p class="section-desc">发布作业、跟踪提交与批改进度</p>
        </div>
        <el-button type="primary" @click="openPublishDialog">发布作业</el-button>
      </div>

      <el-table v-loading="loading" :data="homeworkList" border stripe>
        <el-table-column prop="title" label="题目描述" show-overflow-tooltip min-width="220" />
        <el-table-column prop="courseName" label="所属课程" width="160" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column label="提交进度" width="140">
          <template #default="{ row }">
            <div class="progress-cell">
              <strong>{{ row.gradedCount ?? 0 }}</strong>
              <span>/ {{ row.submitCount ?? 0 }} 已批</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goGrade(row.id)">批改作业</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && homeworkList.length === 0" description="暂无作业，点击右上角发布" />
    </div>

    <el-dialog v-model="dialogVisible" title="发布作业" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="选择课程">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="c in courses"
              :key="c.id"
              :label="c.courseName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目描述">
          <el-input v-model="form.title" placeholder="作业标题" />
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="可选：补充说明" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTeacherHomeworkList, createHomework } from '@/api/homework'
import { getMyCourses } from '@/api/course'
import EmptyState from '@/components/EmptyState.vue'
import type { Homework, Course } from '@/types'

const router = useRouter()
const loading = ref(false)
const publishing = ref(false)
const homeworkList = ref<Homework[]>([])
const courses = ref<Course[]>([])
const dialogVisible = ref(false)
const form = reactive({
  courseId: undefined as number | undefined,
  title: '',
  description: '',
  deadline: '',
})

async function loadData() {
  loading.value = true
  try {
    const res = await getTeacherHomeworkList()
    homeworkList.value = res.data
  } catch {
    homeworkList.value = []
  } finally {
    loading.value = false
  }
}

async function loadCourses() {
  try {
    const res = await getMyCourses()
    courses.value = res.data
  } catch {
    courses.value = []
  }
}

function openPublishDialog() {
  form.courseId = undefined
  form.title = ''
  form.description = ''
  form.deadline = ''
  dialogVisible.value = true
}

async function handlePublish() {
  if (!form.courseId || !form.title.trim()) {
    ElMessage.warning('请选择课程并填写题目')
    return
  }
  if (!form.deadline) {
    ElMessage.warning('请选择截止时间')
    return
  }
  publishing.value = true
  try {
    await createHomework({
      courseId: form.courseId,
      title: form.title,
      description: form.description,
      deadline: form.deadline || undefined,
    })
    ElMessage.success('作业发布成功')
    dialogVisible.value = false
    loadData()
  } catch {
    /* 错误已提示 */
  } finally {
    publishing.value = false
  }
}

function goGrade(id: number) {
  router.push(`/teacher/homework/${id}/grade`)
}

onMounted(() => {
  loadData()
  loadCourses()
})
</script>

<style scoped>
.progress-cell {
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: var(--text-secondary);
  font-size: 13px;
}
.progress-cell strong {
  font-size: 18px;
  color: var(--primary-dark);
  font-family: var(--font-display);
}
</style>
