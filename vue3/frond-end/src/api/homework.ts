import { post, put } from './request'
import type { Homework, HomeworkSubmission, GradeRecord } from '@/types'

interface HomeworkTeacherDTO {
  id: number
  courseId: number
  courseName?: string
  question: string
  deadline?: string
  submitCount?: number
  gradedCount?: number
}

interface HomeworkStudentDTO {
  id: number
  question: string
  deadline?: string
  submitted?: boolean
  answer?: string
  score?: number
  feedback?: string
}

interface HomeworkSubmissionDTO {
  id: number
  studentId: number
  studentName: string
  answer: string
  completedTime?: string
  score?: number
  feedback?: string
}

interface GradeDTO {
  courseId: number
  courseName: string
  question: string
  score: number | null
  feedback?: string
  gradedTime?: string
}

function mapTeacherHomework(dto: HomeworkTeacherDTO): Homework {
  return {
    id: dto.id,
    courseId: dto.courseId,
    courseName: dto.courseName,
    title: dto.question,
    description: '',
    deadline: dto.deadline,
    submitCount: dto.submitCount ?? 0,
    gradedCount: dto.gradedCount ?? 0,
  }
}

function mapStudentHomework(dto: HomeworkStudentDTO, courseId: number): Homework {
  return {
    id: dto.id,
    courseId,
    title: dto.question,
    description: '',
    deadline: dto.deadline,
    submitted: !!dto.submitted,
    answer: dto.answer,
    score: dto.score,
    feedback: dto.feedback,
  }
}

function mapSubmission(dto: HomeworkSubmissionDTO, homeworkId: number): HomeworkSubmission {
  return {
    id: dto.id,
    homeworkId,
    studentId: dto.studentId,
    studentName: dto.studentName,
    content: dto.answer,
    score: dto.score,
    comment: dto.feedback,
    submitTime: dto.completedTime,
  }
}

function groupGrades(list: GradeDTO[]): GradeRecord[] {
  const map = new Map<number, GradeRecord>()
  list.forEach((g, index) => {
    if (!map.has(g.courseId)) {
      map.set(g.courseId, {
        courseId: g.courseId,
        courseName: g.courseName,
        homeworkGrades: [],
      })
    }
    map.get(g.courseId)!.homeworkGrades.push({
      homeworkId: g.courseId * 1000 + index,
      title: g.question,
      score: g.score,
    })
  })
  return Array.from(map.values())
}

export async function getTeacherHomeworkList(courseId?: number) {
  const res = await post<HomeworkTeacherDTO[]>(
    '/api/homework/teacherList',
    courseId != null ? { id: courseId } : {},
  )
  return { ...res, data: res.data.map(mapTeacherHomework) }
}

export async function getStudentHomeworkList(courseId: number) {
  const res = await post<HomeworkStudentDTO[]>('/api/homework/studentList', { id: courseId })
  return { ...res, data: res.data.map((h) => mapStudentHomework(h, courseId)) }
}

export function createHomework(data: {
  courseId: number
  title: string
  description: string
  deadline?: string
}) {
  const question = data.description.trim()
    ? `${data.title}\n\n${data.description}`
    : data.title
  return post<void>('/api/homework/publish', {
    courseId: data.courseId,
    question,
    deadline: data.deadline,
  })
}

export function submitHomework(data: { homeworkId: number; content: string }) {
  return post<void>('/api/homework/submit', {
    homeworkId: data.homeworkId,
    answer: data.content,
  })
}

export async function getSubmissions(homeworkId: number) {
  const res = await post<HomeworkSubmissionDTO[]>('/api/homework/submissions', { id: homeworkId })
  return { ...res, data: res.data.map((s) => mapSubmission(s, homeworkId)) }
}

export function gradeHomework(data: {
  submissionId: number
  score: number
  comment?: string
}) {
  return put<void>('/api/homework/grade', {
    submissionId: data.submissionId,
    score: data.score,
    feedback: data.comment,
  })
}

export async function aiGradeHomework(submissionId: number) {
  const res = await post<{ score: number; reason: string }>('/api/homework/aiGrade', {
    id: submissionId,
  })
  return {
    ...res,
    data: { score: res.data.score, comment: res.data.reason },
  }
}

export async function getGrades(params?: { courseId?: number }) {
  const res = await post<GradeDTO[]>(
    '/api/homework/grades',
    params?.courseId != null ? { id: params.courseId } : {},
  )
  return { ...res, data: groupGrades(res.data) }
}
