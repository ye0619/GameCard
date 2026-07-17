import axios from 'axios'
import type { ApiResult } from '@/types'

const http = axios.create({
  baseURL: '/api',
  timeout: 120000,
})

/**
 * 自动去除图片背景
 *
 * @param file 用户上传的图片文件
 * @returns 透明 PNG 的 base64 data URI
 */
export async function removeBackground(file: File): Promise<string> {
  const form = new FormData()
  form.append('image', file)

  const res = await http.post<ApiResult<string>>('/image/remove-background', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

  if (res.data.code !== 200) {
    throw new Error(res.data.message || '抠图失败')
  }

  return res.data.data
}
