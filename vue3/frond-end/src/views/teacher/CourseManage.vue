<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>我发布的课程</h2>
          <p class="section-desc">管理课程内容、章节与选课学生</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">发布新课程</el-button>
      </div>

      <el-table v-loading="loading" :data="courses" border stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="courseIntro" label="简介" show-overflow-tooltip min-width="220" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goChapters(row.id)">管理章节</el-button>
            <el-button type="primary" link @click="viewStudents(row)">学生名单</el-button>
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && courses.length === 0" description="暂无发布的课程，点击右上角开始创建" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="课程名称">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="form.courseIntro" type="textarea" :rows="4" placeholder="请输入简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="studentDialogVisible" title="学生名单" width="420px">
      <el-table v-loading="studentLoading" :data="students" border>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="username" label="用户名" />
      </el-table>
      <EmptyState v-if="!studentLoading && students.length === 0" description="暂无选课学生" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getMyCourses, createCourse, updateCourse, getCourseStudents,
} from '@/api/course'
import EmptyState from '@/components/EmptyState.vue'
import type { Course } from '@/types'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const studentLoading = ref(false)
const courses = ref<Course[]>([])
const students = ref<{ id: number; username: string }[]>([])
const dialogVisible = ref(false)
const studentDialogVisible = ref(false)
const dialogTitle = ref('发布新课程')
const editingId = ref<number | null>(null)
const form = reactive({ courseName: '', courseIntro: '' })

async function loadCourses() {
  loading.value = true
  try {
    const res = await getMyCourses()
    courses.value = res.data
  } catch {
    courses.value = []
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  dialogTitle.value = '发布新课程'
  editingId.value = null
  form.courseName = ''
  form.courseIntro = ''
  dialogVisible.value = true
}

function openEditDialog(row: Course) {
  dialogTitle.value = '编辑课程信息'
  editingId.value = row.id
  form.courseName = row.courseName
  form.courseIntro = row.courseIntro
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.courseName.trim()) {
    ElMessage.warning('请输入课程名称')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateCourse(editingId.value, form)
      ElMessage.success('课程更新成功')
    } else {
      await createCourse(form)
      ElMessage.success('课程发布成功')
    }
    dialogVisible.value = false
    loadCourses()
  } catch {
    /* 错误已提示 */
  } finally {
    saving.value = false
  }
}

function goChapters(id: number) {
  router.push(`/teacher/courses/${id}`)
}

async function viewStudents(row: Course) {
  studentDialogVisible.value = true
  studentLoading.value = true
  try {
    const res = await getCourseStudents(row.id)
    students.value = res.data
  } catch {
    students.value = []
  } finally {
    studentLoading.value = false
  }
}

onMounted(loadCourses)
</script>
