<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>知识点总结</h2>
          <p class="section-desc">上传学习材料，生成结构化摘要并可选保存</p>
        </div>
      </div>

      <div class="summary-layout">
        <el-form label-width="90px" class="summary-form">
          <el-form-item label="上传文件">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              accept=".txt,.pdf,.md"
              :on-change="handleFileChange"
              :on-exceed="() => ElMessage.warning('只能上传一个文件')"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">支持 txt / pdf / md，大小不超过 5MB</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="关联课程">
            <el-select v-model="selectedCourseId" placeholder="可选：关联到某门课程" clearable style="width: 100%">
              <el-option
                v-for="c in courses"
                :key="c.id"
                :label="c.courseName"
                :value="c.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="summarizing" :disabled="!selectedFile" @click="startSummary">
              开始总结
            </el-button>
            <el-button
              v-if="result"
              type="success"
              :loading="saving"
              @click="saveResult"
            >
              保存总结
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="result" class="summary-result">
          <p class="result-kicker">生成结果</p>
          <h3>{{ result.title }}</h3>
          <div class="key-points">
            <h4>关键点</h4>
            <ul>
              <li v-for="(point, i) in result.keyPoints" :key="i">{{ point }}</li>
            </ul>
          </div>
          <div class="detail-summary">
            <h4>详细摘要</h4>
            <p>{{ result.summary }}</p>
          </div>
        </div>
        <EmptyState v-else description="上传文件后，总结结果会显示在这里" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadInstance } from 'element-plus'
import { summarizeFile, saveSummary } from '@/api/ai'
import { getEnrolledCourses } from '@/api/course'
import EmptyState from '@/components/EmptyState.vue'
import type { Course, SummaryResult } from '@/types'

const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)
const selectedCourseId = ref<number | undefined>()
const courses = ref<Course[]>([])
const summarizing = ref(false)
const saving = ref(false)
const result = ref<SummaryResult | null>(null)

const MAX_SIZE = 5 * 1024 * 1024
const ALLOWED_TYPES = ['text/plain', 'application/pdf', 'text/markdown']
const ALLOWED_EXT = ['.txt', '.pdf', '.md']

function handleFileChange(file: UploadFile) {
  const raw = file.raw
  if (!raw) return

  const ext = raw.name.substring(raw.name.lastIndexOf('.')).toLowerCase()
  if (!ALLOWED_EXT.includes(ext) && !ALLOWED_TYPES.includes(raw.type)) {
    ElMessage.error('仅支持 txt、pdf、md 格式')
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    return
  }
  if (raw.size > MAX_SIZE) {
    ElMessage.error('文件大小不能超过 5MB')
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    return
  }
  selectedFile.value = raw
}

async function startSummary() {
  if (!selectedFile.value) return
  summarizing.value = true
  result.value = null
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const res = await summarizeFile(formData)
    result.value = res.data
    ElMessage.success('总结完成')
  } catch {
    /* 错误已提示 */
  } finally {
    summarizing.value = false
  }
}

async function saveResult() {
  if (!result.value) return
  saving.value = true
  try {
    await saveSummary({
      ...result.value,
      courseId: selectedCourseId.value,
    })
    ElMessage.success('总结已保存')
  } catch {
    /* 错误已提示 */
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getEnrolledCourses()
    courses.value = res.data
  } catch {
    courses.value = []
  }
})
</script>

<style scoped>
.summary-layout {
  display: grid;
  grid-template-columns: minmax(280px, 360px) 1fr;
  gap: 24px;
  align-items: start;
}
.summary-form {
  padding: 18px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: var(--bg-muted);
}
.summary-result {
  padding: 22px;
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.06), transparent 40%),
    #fff;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}
.result-kicker {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--primary-color);
  font-weight: 600;
}
.summary-result h3 {
  margin: 0 0 18px;
  font-size: 20px;
  font-family: var(--font-display);
}
.key-points,
.detail-summary {
  margin-top: 16px;
}
.key-points h4,
.detail-summary h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--text-secondary);
}
.key-points ul {
  margin: 0;
  padding-left: 18px;
  line-height: 1.9;
  color: var(--text-regular);
}
.detail-summary p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-regular);
}
@media (max-width: 900px) {
  .summary-layout {
    grid-template-columns: 1fr;
  }
}
</style>
