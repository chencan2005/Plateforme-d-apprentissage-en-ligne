export type UserRole = 'student' | 'teacher'

export interface UserInfo {
  id?: number
  username: string
  role: UserRole
  name?: string
  createdTime?: string
}

export interface Course {
  id: number
  courseName: string
  courseIntro: string
  teacherName?: string
  teacherId?: number
  status?: string
  createdTime?: string
  progress?: number
}

export interface Chapter {
  id: number
  courseId: number
  title: string
  content: string
  contentType: 'text' | 'video'
  learned?: boolean
  sortOrder?: number
}

export interface Homework {
  id: number
  courseId: number
  courseName?: string
  title: string
  description: string
  deadline?: string
  createdTime?: string
  submitted?: boolean
  answer?: string
  score?: number
  feedback?: string
  submitCount?: number
  gradedCount?: number
}

export interface HomeworkSubmission {
  id: number
  homeworkId: number
  studentId: number
  studentName: string
  content: string
  score?: number
  comment?: string
  submitTime?: string
}

export interface Notice {
  id: number
  title: string
  content: string
  publisherName?: string
  publisherRole?: string
  createdTime?: string
}

export interface GradeRecord {
  courseId: number
  courseName: string
  homeworkGrades: { homeworkId: number; title: string; score: number | null }[]
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface SummaryResult {
  title: string
  keyPoints: string[]
  summary: string
}

export interface ApiResponse<T = unknown> {
  code: number
  data: T
  message?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
}
