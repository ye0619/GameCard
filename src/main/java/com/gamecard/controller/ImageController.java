package com.gamecard.controller;

import com.gamecard.ai.AIImageService;
import com.gamecard.background.RemoveBackgroundService;
import com.gamecard.common.result.Result;
import com.gamecard.template.manager.TemplateManager;
import com.gamecard.template.model.Template;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片处理接口 — 提供抠图、AI 风格转换等图片处理能力
 *
 * @author GameCard Team
 */
@RestController
@RequestMapping("/api/image")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final RemoveBackgroundService removeBackgroundService;
    private final AIImageService aiImageService;
    private final TemplateManager templateManager;

    public ImageController(
            RemoveBackgroundService removeBackgroundService,
            AIImageService aiImageService,
            TemplateManager templateManager
    ) {
        this.removeBackgroundService = removeBackgroundService;
        this.aiImageService = aiImageService;
        this.templateManager = templateManager;
    }

    /**
     * 自动去除图片背景
     *
     * @param image 用户上传的图片文件
     * @return 透明 PNG 图片（直接返回字节流）
     */
    @PostMapping(value = "/remove-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> removeBackground(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return Result.error("请上传图片文件");
        }

        // 校验文件类型
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("仅支持图片文件");
        }

        try {
            // 调用 Python 服务抠图
            byte[] pngBytes = removeBackgroundService.removeBackground(image);

            // 将 PNG 字节转为 Base64 数据 URI，方便前端直接使用
            String base64 = java.util.Base64.getEncoder().encodeToString(pngBytes);
            String dataUri = "data:image/png;base64," + base64;

            log.info("Background removal completed for: {}", image.getOriginalFilename());
            return Result.success(dataUri);

        } catch (Exception e) {
            log.error("Background removal failed for {}: {}", image.getOriginalFilename(), e.getMessage(), e);
            return Result.error("图片去背景失败: " + e.getMessage());
        }
    }

    /**
     * AI 图像风格转换
     *
     * <p>
     * 接收前端传递的用户图片 + AI 配置，读取模板的 imageConfig 获取 prompt，
     * 透传到用户指定的 AI API 端点，返回处理后的图片。
     * </p>
     *
     * <p>
     * 设计原则：
     * <ul>
     *   <li>后端不做 AI 配置持久化，所有凭据由前端传递</li>
     *   <li>后端仅做无状态代理，不存储任何 API Key</li>
     *   <li>模板的 imagePrompt 驱动生成风格</li>
     * </ul>
     * </p>
     *
     * @param request AI 处理请求体（含图片、模板ID、用户 AI 配置）
     * @return 处理后的图片 base64 data URI
     */
    @PostMapping(value = "/process", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Map<String, String>> processWithAi(@RequestBody AiProcessRequest request) {
        // 参数校验
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            return Result.error("请提供需要处理的图片");
        }
        if (request.getTemplateId() == null || request.getTemplateId().isBlank()) {
            return Result.error("请提供模板 ID");
        }
        if (request.getAiConfig() == null) {
            return Result.error("请配置 AI 服务参数");
        }
        if (request.getAiConfig().getEndpoint() == null || request.getAiConfig().getEndpoint().isBlank()) {
            return Result.error("请提供 AI API 端点地址");
        }
        if (request.getAiConfig().getApiKey() == null || request.getAiConfig().getApiKey().isBlank()) {
            return Result.error("请提供 AI API Key");
        }

        // 查找模板
        Template template = templateManager.getTemplate(request.getTemplateId());
        if (template == null) {
            return Result.error("未找到模板: " + request.getTemplateId());
        }

        try {
            var result = aiImageService.processImage(
                    request.getImageUrl(),
                    template,
                    request.getPrompt(),
                    request.getAiConfig().getEndpoint(),
                    request.getAiConfig().getApiKey(),
                    request.getAiConfig().getModel()
            );

            log.info("AI image processing completed for template: {}", request.getTemplateId());

            Map<String, String> data = new HashMap<>();
            data.put("imageUrl", result.getImageUrl());
            data.put("imageSource", result.getImageSource());
            return Result.success(data);

        } catch (Exception e) {
            log.error("AI image processing failed: {}", e.getMessage(), e);
            return Result.error("AI 图像处理失败: " + e.getMessage());
        }
    }

    /**
     * 验证 AI 配置是否可用
     *
     * <p>
     * 通过后端代理发送一个轻量请求到用户配置的 AI 端点，
     * 验证 endpoint、apiKey 和 model 是否有效。
     * 避免浏览器 CORS 限制导致误判。
     * </p>
     */
    @PostMapping(value = "/verify-ai", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Map<String, Object>> verifyAiConfig(@RequestBody Map<String, String> config) {
        String endpoint = config.get("endpoint");
        String apiKey = config.get("apiKey");
        String model = config.get("model");

        if (endpoint == null || endpoint.isBlank()) {
            return Result.error("请提供端点地址");
        }
        if (apiKey == null || apiKey.isBlank()) {
            return Result.error("请提供 API Key");
        }

        try {
            Map<String, Object> result = aiImageService.verifyConnection(endpoint, apiKey, model);
            return Result.success(result);
        } catch (Exception e) {
            log.warn("AI config verification failed: {}", e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("ok", false);
            errorResult.put("error", e.getMessage());
            return Result.error("连接验证失败: " + e.getMessage());
        }
    }
}
