import { del, post } from './request'
import type { SummaryResult } from '@/types'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? ''
const SESSION_KEY = 'aiChatSessionId'

function getSessionId(): string {
  let id = localStorage.getItem(SESSION_KEY)
  if (!id) {
    id = `session-${Date.now()}`
    localStorage.setItem(SESSION_KEY, id)
  }
  return id
}

function parseSseChunk(
  line: string,
  state: { eventName: string },
  onChunk: (text: string) => void,
) {
  if (line.startsWith('event:')) {
    state.eventName = line.slice(6).trim()
    return
  }
  if (line.startsWith('data:')) {
    const data = line.slice(5).trimStart()
    if (!data) return
    if (state.eventName === 'error') {
      throw new Error(data)
    }
    onChunk(data)
    state.eventName = ''
    return
  }
  if (line === '') {
    state.eventName = ''
  }
}

function looksLikeApiError(text: string): boolean {
  const trimmed = text.trim()
  return trimmed.startsWith('{"code":') || trimmed.startsWith('{"error":')
}

export async function streamChat(
  message: string,
  onChunk: (text: string) => void,
  signal?: AbortSignal,
): Promise<string> {
  const token = localStorage.getItem('token')
  const sessionId = getSessionId()

  const response = await fetch(`${baseURL}/api/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=UTF-8',
      Accept: 'text/event-stream',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ sessionId, message }),
    signal,
  })

  if (response.status === 401) {
    const { clearAuthStorage } = await import('@/utils/user')
    clearAuthStorage()
    location.href = '/login'
    throw new Error('登录已过期')
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (!response.ok || contentType.includes('application/json')) {
    const errBody = await response.json().catch(() => null)
    throw new Error(errBody?.message || `AI 服务错误 (${response.status})`)
  }

  const reader = response.body?.getReader()
  if (!reader) throw new Error('无法读取流式响应')

  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let fullText = ''
  const state = { eventName: '' }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() ?? ''

    for (const line of lines) {
      parseSseChunk(line, state, (chunk) => {
        fullText += chunk
        onChunk(chunk)
      })
    }
  }

  buffer += decoder.decode()
  if (buffer.trim()) {
    for (const line of buffer.split('\n')) {
      parseSseChunk(line, state, (chunk) => {
        fullText += chunk
        onChunk(chunk)
      })
    }
  }

  if (looksLikeApiError(fullText)) {
    try {
      const parsed = JSON.parse(fullText.trim())
      throw new Error(parsed.message || parsed.error || 'AI 服务异常')
    } catch (e) {
      if (e instanceof Error && e.message !== 'AI 服务异常') throw e
      throw new Error('AI 服务异常，请检查 API Key 配置')
    }
  }

  return fullText
}

export function clearChatHistory() {
  const sessionId = getSessionId()
  return del<void>(`/api/chat/history?sessionId=${encodeURIComponent(sessionId)}`)
}

interface SummaryResultDTO {
  title: string
  keyPoints: string
  summaryContent: string
}

function mapSummaryResult(dto: SummaryResultDTO): SummaryResult {
  return {
    title: dto.title,
    keyPoints: dto.keyPoints
      ? dto.keyPoints.split('\n').map((s) => s.trim()).filter(Boolean)
      : [],
    summary: dto.summaryContent,
  }
}

export async function summarizeFile(formData: FormData) {
  const res = await post<SummaryResultDTO>('/api/summary/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return { ...res, data: mapSummaryResult(res.data) }
}

export function saveSummary(data: {
  title: string
  keyPoints: string[]
  summary: string
  courseId?: number
}) {
  return post<void>('/api/summary/save', {
    title: data.title,
    keyPoints: data.keyPoints.join('\n'),
    summaryContent: data.summary,
    courseId: data.courseId,
  })
}
