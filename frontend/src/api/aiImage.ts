import axios from 'axios'
import type { ApiResult, AiProcessRequest, AiProcessResponse, AiUserConfig } from '@/types'

const http = axios.create({
  baseURL: '/api',
  timeout: 180000, // AI 生成可能较慢
})

/**
 * AI 图像风格转换
 *
 * 前端传递用户配置的 AI 凭据，后端做无状态代理。
 *
 * @param imageUrl    原始图片 base64 data URI
 * @param templateId  模板 ID（用于读取 imagePrompt）
 * @param aiConfig    用户自有的 AI 配置
 * @param prompt      自定义提示词（可选，为空则后端用模板默认）
 * @returns           处理后的图片 data URI（优先用原始 URL）
 */
export async function processImageWithAi(
  imageUrl: string,
  templateId: string,
  aiConfig: AiUserConfig,
  prompt?: string,
): Promise<string> {
  const body: AiProcessRequest = {
    imageUrl,
    templateId,
    operation: 'style',
    ...(prompt ? { prompt } : {}),
    aiConfig: {
      endpoint: aiConfig.endpoint,
      apiKey: aiConfig.apiKey,
      model: aiConfig.model,
    },
  }

  const res = await http.post<ApiResult<AiProcessResponse>>('/image/process', body)

  if (res.data.code !== 200) {
    throw new Error(res.data.message || 'AI 图像处理失败')
  }

  // 优先使用 data URI（imageUrl，后端已带认证头下载），
  // 其次原始 URL（imageSource，浏览器直接加载时可能缺认证头）
  return res.data.data.imageUrl || res.data.data.imageSource || ''
}
