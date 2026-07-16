import axios from 'axios'
import type { ApiResult, Template } from '@/types'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

/** 获取所有模板列表 */
export async function fetchTemplates(): Promise<Template[]> {
  const res = await http.get<ApiResult<Template[]>>('/templates')
  return res.data.data
}

/** 获取单个模板详情 */
export async function fetchTemplate(id: string): Promise<Template> {
  const res = await http.get<ApiResult<Template>>(`/templates/${id}`)
  return res.data.data
}
