package com.gamecard.controller;

import com.gamecard.background.RemoveBackgroundService;
import com.gamecard.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片处理接口 — 提供抠图等 AI 图片处理能力
 *
 * @author GameCard Team
 */
@RestController
@RequestMapping("/api/image")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final RemoveBackgroundService removeBackgroundService;

    public ImageController(RemoveBackgroundService removeBackgroundService) {
        this.removeBackgroundService = removeBackgroundService;
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
}
