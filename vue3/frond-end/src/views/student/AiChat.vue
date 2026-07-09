<template>
  <div class="page-container">
    <div class="page-card chat-page">
      <div class="page-header">
        <div>
          <h2>AI 智能问答</h2>
          <p class="section-desc">围绕课程与学习方法提问，支持多轮对话</p>
        </div>
        <el-button type="danger" plain :loading="clearing" @click="handleClear">
          清空对话
        </el-button>
      </div>

      <div ref="chatContainer" class="chat-window">
        <div v-if="messages.length === 0" class="welcome-panel">
          <div class="welcome-icon">AI</div>
          <h3>你好，我是学习助手</h3>
          <p>可以问我：怎么学 Java、作业怎么拆解、某个概念怎么理解……</p>
          <div class="suggest-row">
            <button type="button" class="suggest-chip" @click="useSuggest('怎么制定一周的 Java 学习计划？')">制定学习计划</button>
            <button type="button" class="suggest-chip" @click="useSuggest('封装、继承、多态分别是什么？')">解释 OOP</button>
            <button type="button" class="suggest-chip" @click="useSuggest('如何高效复习考试？')">复习建议</button>
          </div>
        </div>
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['chat-bubble', msg.role]"
        >
          <div class="bubble-label">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div class="bubble-content">{{ msg.content }}</div>
        </div>
        <div v-if="streaming" class="chat-bubble assistant">
          <div class="bubble-label">AI</div>
          <div class="bubble-content">{{ streamText }}<span class="cursor">|</span></div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题，Ctrl + Enter 发送"
          :disabled="streaming"
          @keyup.ctrl.enter="sendMessage"
        />
        <el-button
          type="primary"
          :loading="streaming"
          :disabled="!inputText.trim()"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { streamChat, clearChatHistory } from '@/api/ai'
import type { ChatMessage } from '@/types'

interface UiMessage extends ChatMessage {
  id: string
}

const messages = ref<UiMessage[]>([])
const inputText = ref('')
const streaming = ref(false)
const streamText = ref('')
const clearing = ref(false)
const chatContainer = ref<HTMLElement>()
let abortController: AbortController | null = null
let msgSeq = 0

function nextId(prefix: string) {
  msgSeq += 1
  return `${prefix}-${Date.now()}-${msgSeq}`
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

function useSuggest(text: string) {
  inputText.value = text
  sendMessage()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || streaming.value) return

  messages.value.push({ id: nextId('u'), role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  streaming.value = true
  streamText.value = ''
  abortController = new AbortController()

  try {
    const reply = await streamChat(
      text,
      (chunk) => {
        streamText.value += chunk
        scrollToBottom()
      },
      abortController.signal,
    )
    if (reply.trim()) {
      messages.value.push({ id: nextId('a'), role: 'assistant', content: reply })
    }
  } catch (err) {
    if ((err as Error).name !== 'AbortError') {
      ElMessage.error((err as Error).message || 'AI 回答失败，请稍后重试')
    }
  } finally {
    streaming.value = false
    streamText.value = ''
    abortController = null
    scrollToBottom()
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定清空所有对话历史吗？', '提示', { type: 'warning' })
  } catch {
    return
  }

  clearing.value = true
  try {
    await clearChatHistory()
    messages.value = []
    ElMessage.success('对话历史已清空')
  } catch {
    ElMessage.error('清空失败，请稍后重试')
  } finally {
    clearing.value = false
  }
}

onUnmounted(() => {
  abortController?.abort()
  abortController = null
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  min-height: 560px;
}
.chat-window {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
  background:
    radial-gradient(circle at top left, rgba(15, 118, 110, 0.05), transparent 35%),
    #f8fafc;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  margin-bottom: 16px;
}
.welcome-panel {
  height: 100%;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
  color: var(--text-secondary);
}
.welcome-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  color: #fff;
  font-weight: 700;
  font-family: var(--font-display);
  margin-bottom: 6px;
}
.welcome-panel h3 {
  margin: 0;
  color: var(--text-primary);
  font-family: var(--font-display);
}
.welcome-panel p {
  margin: 0;
  max-width: 360px;
  line-height: 1.6;
}
.suggest-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 10px;
}
.suggest-chip {
  border: 1px solid var(--border-color);
  background: #fff;
  color: var(--text-regular);
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.suggest-chip:hover {
  border-color: #99f6e4;
  color: var(--primary-dark);
  background: var(--primary-light);
}
.chat-bubble {
  margin-bottom: 16px;
  max-width: 78%;
}
.chat-bubble.user {
  margin-left: auto;
}
.chat-bubble.user .bubble-content {
  background: linear-gradient(135deg, #0f766e, #0d9488);
  color: #fff;
}
.chat-bubble.assistant .bubble-content {
  background: #fff;
  border: 1px solid var(--border-color);
}
.bubble-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}
.bubble-content {
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}
.cursor {
  animation: blink 1s infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}
.chat-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.chat-input .el-textarea {
  flex: 1;
}
</style>
