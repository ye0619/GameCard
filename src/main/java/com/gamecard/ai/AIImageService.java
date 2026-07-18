package com.gamecard.ai;

import tools.jackson.databind.ObjectMapper;
import com.gamecard.template.model.Template;
import com.gamecard.template.model.TemplateImageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * AI 图像处理代理服务
 *
 * <p>
 * 将前端的 AI 处理请求透传到用户指定的 AI API 端点。
 * 设计原则：
 * <ul>
 *   <li>无状态：不存储任何用户 API 凭据</li>
 *   <li>透传代理：不修改请求/响应内容</li>
 *   <li>模板驱动：从模板 imageConfig 获取 prompt</li>
 * </ul>
 * </p>
 *
 * @author GameCard Team
 */
@Service
public class AIImageService {

    private static final Logger log = LoggerFactory.getLogger(AIImageService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ImageProxyService imageProxyService;

    /** 默认超时：180 秒（AI 生成可能较慢） */
    private static final int TIMEOUT_SECONDS = 180;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    public AIImageService(ImageProxyService imageProxyService) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
        this.imageProxyService = imageProxyService;
    }

    /**
     * 执行 AI 图像风格转换
     *
     * @param imageData   原始图片 base64 data URI
     * @param template    当前模板（含 imageConfig）
     * @param customPrompt 用户自定义提示词（若非空优先于模板默认）
     * @param aiEndpoint  用户配置的 AI API 端点
     * @param aiApiKey    用户配置的 API Key
     * @param aiModel     用户配置的模型名称
     * @return AiProcessResult（包含 imageUrl 和 imageSource）
     * @throws Exception 处理失败
     */
    public AiProcessResult processImage(
            String imageData,
            Template template,
            String customPrompt,
            String aiEndpoint,
            String aiApiKey,
            String aiModel
    ) throws Exception {

        // 1. 确定最终使用的 prompt：自定义优先，其次模板默认，最后全局兜底
        String prompt = customPrompt;
        if (prompt == null || prompt.isBlank()) {
            TemplateImageConfig imageConfig = template.getImageConfig();
            prompt = (imageConfig != null && imageConfig.getImagePrompt() != null)
                    ? imageConfig.getImagePrompt()
                    : "Convert this character into a game-style artwork, vibrant colors, clean illustration";
        }

        log.info("AI image processing: template={}, model={}, endpoint={}",
                template.getId(), aiModel, aiEndpoint);

        // 2. 标准化端点 URL
        String resolvedEndpoint = resolveImageEndpoint(aiEndpoint);

        // 3. 构建请求体
        Map<String, Object> requestBody = buildRequestBody(prompt, aiModel);

        // 4. 发送 HTTP 请求
        String requestJson = objectMapper.writeValueAsString(requestBody);
        log.info("AI request: POST {} | model={}", resolvedEndpoint, aiModel);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resolvedEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + aiApiKey)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        String responseBody = sendWithRetry(request, resolvedEndpoint, 1);

        // 5. 解析响应（传入 API Key 用于后续图片代理下载）
        return parseImageResponse(responseBody, aiApiKey);
    }

    /**
     * AI 处理结果
     */
    public static class AiProcessResult {
        private final String imageUrl;
        private final String imageSource;

        public AiProcessResult(String imageUrl, String imageSource) {
            this.imageUrl = imageUrl;
            this.imageSource = imageSource;
        }

        public String getImageUrl() { return imageUrl; }
        public String getImageSource() { return imageSource; }
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String prompt, String model) {
        return Map.of(
                "model", model != null && !model.isBlank() ? model : "dall-e-3",
                "prompt", prompt,
                "n", 1
        );
    }

    /**
     * 从 AI API 响应中提取图片数据
     * <p>
     * 返回 AiProcessResult，包含 imageUrl（data URI，用于持久化）和
     * imageSource（原始图片 URL，用于前端直接显示）。
     * 对于 URL 格式，直接返回 URL 而非下载到本地，避免大图 base64 编码失败。
     * </p>
     */
    @SuppressWarnings("unchecked")
    private AiProcessResult parseImageResponse(String responseBody, String apiKey) throws Exception {
        log.info("AI API raw response (first 300 chars): {}", truncate(responseBody, 300));

        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(responseBody, Map.class);
        } catch (Exception e) {
            // 响应不是 JSON？可能是纯文本错误
            throw new RuntimeException("AI 服务返回了非 JSON 格式响应: " + truncate(responseBody, 200));
        }

        // === 尝试所有可能的格式 ===

        // 标准 OpenAI 格式：{"data": [{"b64_json": "..."} | {"url": "..."}]}
        if (parsed.containsKey("data")) {
            Object dataObj = parsed.get("data");
            if (dataObj instanceof java.util.List) {
                java.util.List<?> dataList = (java.util.List<?>) dataObj;
                if (!dataList.isEmpty()) {
                    Object first = dataList.get(0);
                    if (first instanceof Map) {
                        Map<String, Object> item = (Map<String, Object>) first;

                        // b64_json → 转为 data URI（值必须非空）
                        if (item.containsKey("b64_json")) {
                            String b64 = (String) item.get("b64_json");
                            if (b64 != null && !b64.isBlank()) {
                                String dataUri = "data:image/png;base64," + b64;
                                return new AiProcessResult(dataUri, dataUri);
                            }
                        }
                        // url → 直接返回原始 URL（值必须非空）
                        if (item.containsKey("url")) {
                            String url = (String) item.get("url");
                            if (url != null && !url.isBlank()) {
                                if (url.startsWith("data:")) {
                                    return new AiProcessResult(url, url);
                                }
                                // 原始 URL（如 https://cdn.xx.com/img.png）
                                // 尝试补齐 data URI，但不要因为下载失败而影响主流程
                                String dataUri = tryDownloadAsDataUri(url, apiKey);
                                return new AiProcessResult(dataUri != null ? dataUri : url, url);
                            }
                        }
                    }
                    // data 是字符串数组：["base64..."]
                    if (first instanceof String) {
                        String val = (String) first;
                        if (val.length() > 100) {
                            // 大概率是 base64
                            String dataUri = (val.startsWith("data:") ? "" : "data:image/png;base64,") + val;
                            return new AiProcessResult(dataUri, dataUri);
                        }
                        // 短字符串，可能是 URL
                        return new AiProcessResult(val, val);
                    }
                }
            }
            // data 是单个对象而非数组：{"data": {"url": "..."}}
            if (dataObj instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                if (dataMap.containsKey("url")) {
                    String url = (String) dataMap.get("url");
                    return new AiProcessResult(url, url);
                }
            }
        }

        // 常见键名：image_url, image, url, output, result
        for (String key : new String[]{"image_url", "image", "url", "output", "result"}) {
            if (parsed.containsKey(key)) {
                Object val = parsed.get(key);
                if (val instanceof String) {
                    String s = (String) val;
                    return new AiProcessResult(s, s);
                }
                if (val instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) val;
                    if (!list.isEmpty() && list.get(0) instanceof String) {
                        String s = (String) list.get(0);
                        return new AiProcessResult(s, s);
                    }
                }
            }
        }

        // 彻底无法识别
        log.error("无法识别的 AI API 响应: {}", responseBody);
        throw new RuntimeException("无法解析 AI 服务响应。响应内容: " + truncate(responseBody, 300));
    }

    /**
     * 尝试下载 URL 并转为 data URI
     * <p>
     * 使用 ImageProxyService 代理下载，自动携带认证头。
     * 如果下载失败，返回 null，不影响主流程。
     * </p>
     */
    private String tryDownloadAsDataUri(String imageUrl, String apiKey) {
        try {
            return imageProxyService.downloadAsDataUri(imageUrl, apiKey);
        } catch (Exception e) {
            log.warn("Image download failed (non-fatal): {}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送 HTTP 请求，对临时错误自动重试
     */
    private String sendWithRetry(HttpRequest request, String endpoint, int attempt) throws Exception {
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        int code = response.statusCode();
        String body = response.body();

        if (code == 200) {
            return body;
        }

        boolean retryable = (code == 503 || code == 429 || (code >= 500 && code < 600));

        if (retryable && attempt < MAX_RETRIES) {
            long delay = RETRY_DELAY_MS * attempt;
            log.warn("AI API 临时错误 [{}] (第{}次), {}ms 后重试...", code, attempt, delay);
            try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            return sendWithRetry(request, endpoint, attempt + 1);
        }

        log.error("AI API returned error [{}] from POST {}: {}",
                code, endpoint, truncate(body, 500));
        throw new RuntimeException("AI 服务返回错误 [HTTP " + code + "]"
                + "\n请求地址: " + endpoint
                + "\n响应: " + truncate(body, 300));
    }

    /**
     * 标准化图片生成端点 URL
     */
    private String resolveImageEndpoint(String rawEndpoint) {
        if (rawEndpoint == null || rawEndpoint.isBlank()) {
            throw new IllegalArgumentException("端点地址不能为空");
        }

        String url = rawEndpoint.trim();

        if (url.endsWith("/images/generations")) {
            return url;
        }

        if (url.matches(".*/v\\d+/?$")) {
            String base = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            String resolved = base + "/images/generations";
            log.info("Endpoint auto-resolved: {} → {}", url, resolved);
            return resolved;
        }

        String base = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String resolved = base + "/images/generations";
        log.info("Endpoint fallback: {} → {}", url, resolved);
        return resolved;
    }

    /**
     * 验证 AI 配置是否可用
     */
    public Map<String, Object> verifyConnection(String endpoint, String apiKey, String model) {
        Map<String, Object> result = new java.util.HashMap<>();

        try {
            String modelsUrl = endpoint.endsWith("/") ? endpoint + "models" : endpoint + "/models";
            HttpRequest modelReq = HttpRequest.newBuilder()
                    .uri(URI.create(modelsUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> modelResp = httpClient.send(modelReq,
                    HttpResponse.BodyHandlers.ofString());

            if (modelResp.statusCode() == 200) {
                result.put("ok", true);
                result.put("provider", "openai-compatible");
                return result;
            }

            Map<String, Object> testBody = Map.of(
                    "model", model != null && !model.isBlank() ? model : "gpt-4o-mini",
                    "messages", java.util.List.of(Map.of("role", "user", "content", "hi")),
                    "max_tokens", 1
            );

            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(testBody)))
                    .build();

            HttpResponse<String> postResp = httpClient.send(postReq,
                    HttpResponse.BodyHandlers.ofString());

            if (postResp.statusCode() == 200) {
                result.put("ok", true);
                result.put("provider", "custom-endpoint");
                return result;
            }

            String errorInfo = String.format(
                    "GET %s → %d, POST %s → %d. 响应: %s",
                    modelsUrl, modelResp.statusCode(),
                    endpoint, postResp.statusCode(),
                    truncate(postResp.body(), 150)
            );
            result.put("ok", false);
            result.put("error", errorInfo);

        } catch (java.net.ConnectException e) {
            result.put("ok", false);
            result.put("error", "无法连接到 " + endpoint + " — 连接被拒绝，请检查端点地址");
        } catch (java.net.http.HttpConnectTimeoutException e) {
            result.put("ok", false);
            result.put("error", "连接超时 — " + endpoint + " 无响应");
        } catch (java.net.UnknownHostException e) {
            result.put("ok", false);
            result.put("error", "无法解析域名 — 请检查端点地址是否正确");
        } catch (javax.net.ssl.SSLException e) {
            result.put("ok", false);
            result.put("error", "SSL 握手失败 — 该端点要求 HTTPS 连接");
        } catch (Exception e) {
            result.put("ok", false);
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return result;
    }

    /** 截断超长字符串 */
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
