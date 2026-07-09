import { post, put, del } from './request'
import type { Course, Chapter } from '@/types'

interface CourseDTO {
  id: number
  courseName: string
  courseIntro: string
  teacherName?: string
  createdTime?: string
}

interface EnrolledCourseDTO {
  courseId: number
  courseName: string
  courseIntro: string
  teacherName?: string
  progress?: number
}

interface ChapterItemDTO {
  id: number
  chapterTitle: string
  contentType: string
  content: string
  sortOrder?: number
  studied?: boolean
}

interface CourseDetailDTO {
  courseId: number
  courseName: string
  courseIntro: string
  teacherName?: string
  chapters: ChapterItemDTO[]
}

interface StudentListDTO {
  id: number
  username: string
}

function mapCourse(dto: CourseDTO): Course {
  return {
    id: dto.id,
    courseName: dto.courseName,
    courseIntro: dto.courseIntro,
    teacherName: dto.teacherName,
    createdTime: dto.createdTime,
  }
}

function mapEnrolledCourse(dto: EnrolledCourseDTO): Course {
  return {
    id: dto.courseId,
    courseName: dto.courseName,
    courseIntro: dto.courseIntro,
    teacherName: dto.teacherName,
    progress: dto.progress,
  }
}

function mapChapter(dto: ChapterItemDTO, courseId: number): Chapter {
  return {
    id: dto.id,
    courseId,
    title: dto.chapterTitle,
    content: dto.content,
    contentType: dto.contentType === 'video' ? 'video' : 'text',
    learned: dto.studied,
    sortOrder: dto.sortOrder,
  }
}

function mapCourseDetail(dto: CourseDetailDTO): Course {
  return {
    id: dto.courseId,
    courseName: dto.courseName,
    courseIntro: dto.courseIntro,
    teacherName: dto.teacherName,
  }
}

export async function getAllCourses(params?: { courseName?: string }) {
  const res = await post<CourseDTO[]>('/api/courses/list')
  let list = res.data.map(mapCourse)
  if (params?.courseName) {
    const keyword = params.courseName.trim().toLowerCase()
    list = list.filter((c) => c.courseName.toLowerCase().includes(keyword))
  }
  return { ...res, data: list }
}

export async function getMyCourses() {
  const res = await post<CourseDTO[]>('/api/courses/my')
  return { ...res, data: res.data.map(mapCourse) }
}

export async function getEnrolledCourses() {
  const res = await post<EnrolledCourseDTO[]>('/api/courses/enrolled')
  return { ...res, data: res.data.map(mapEnrolledCourse) }
}

export async function getCourseDetail(id: number) {
  const res = await post<CourseDetailDTO>('/api/chapters/detail', { id })
  return { ...res, data: mapCourseDetail(res.data) }
}

export function createCourse(data: { courseName: string; courseIntro: string }) {
  return post<void>('/api/courses/publish', data)
}

export function updateCourse(id: number, data: { courseName: string; courseIntro: string }) {
  return put<void>('/api/courses/update', { id, ...data })
}

export function enrollCourse(courseId: number) {
  return post<void>('/api/courses/enroll', { courseId })
}

export async function getCourseStudents(courseId: number) {
  const res = await post<StudentListDTO[]>('/api/courses/students', { id: courseId })
  return {
    ...res,
    data: res.data.map((s) => ({ id: s.id, username: s.username })),
  }
}

export async function getChapters(courseId: number) {
  const res = await post<CourseDetailDTO>('/api/chapters/detail', { id: courseId })
  return {
    ...res,
    data: res.data.chapters.map((c) => mapChapter(c, courseId)),
  }
}

export function createChapter(data: {
  courseId: number
  title: string
  content: string
  contentType: 'text' | 'video'
  sortOrder?: number
}) {
  return post<void>('/api/chapters/add', {
    courseId: data.courseId,
    chapterTitle: data.title,
    contentType: data.contentType,
    content: data.content,
    sortOrder: data.sortOrder,
  })
}

export function updateChapter(
  id: number,
  data: Partial<Chapter> & { courseId?: number },
) {
  return put<void>('/api/chapters/update', {
    id,
    courseId: data.courseId,
    chapterTitle: data.title,
    contentType: data.contentType,
    content: data.content,
    sortOrder: data.sortOrder,
  })
}

export function deleteChapter(id: number) {
  return del<void>(`/api/chapters/${id}`)
}

export function markChapterLearned(chapterId: number) {
  return post<void>('/api/chapters/markStudied', { id: chapterId })
}
