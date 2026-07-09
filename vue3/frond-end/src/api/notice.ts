import { get, post } from './request'
import type { Notice } from '@/types'
import { toFrontendRole } from '@/utils/role'

interface NoticeDTO {
  id: number
  title: string
  content: string
  publisherName?: string
  publisherRole?: string
  createdTime?: string
}

function mapNotice(dto: NoticeDTO): Notice {
  return {
    id: dto.id,
    title: dto.title,
    content: dto.content,
    publisherName: dto.publisherName,
    publisherRole: dto.publisherRole ? toFrontendRole(dto.publisherRole) : undefined,
    createdTime: dto.createdTime,
  }
}

export async function getNoticeList(params?: { title?: string }) {
  const res = await get<NoticeDTO[]>('/api/notices')
  let list = res.data.map(mapNotice)
  if (params?.title) {
    const keyword = params.title.trim().toLowerCase()
    list = list.filter((n) => n.title.toLowerCase().includes(keyword))
  }
  return { ...res, data: list }
}

export async function getNoticeDetail(id: number) {
  const res = await get<NoticeDTO>(`/api/notices/${id}`)
  return { ...res, data: mapNotice(res.data) }
}

export function publishNotice(data: { title: string; content: string; courseId?: number }) {
  return post<void>('/api/notices/publish', data)
}
