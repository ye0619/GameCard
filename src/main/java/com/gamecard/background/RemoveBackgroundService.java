package com.gamecard.background;

import com.gamecard.common.properties.BackgroundRemovalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * 自动抠图服务 — 调用 Python rembg 微服务去除图片背景
 *
 * <p>架构：Vue → Spring Boot → Python rembg Service</p>
 *
 * @author GameCard Team
 */
@Service
public class RemoveBackgroundService {

    private static final Logger log = LoggerFactory.getLogger(RemoveBackgroundService.class);
    private static final String BOUNDARY = "----GameCardBgRemoverBoundary";

    private final BackgroundRemovalProperties properties;

    public RemoveBackgroundService(BackgroundRemovalProperties properties) {
        this.properties = properties;
    }

    /**
     * 对上传的图片执行去背景处理
     *
     * @param file       原始图片（用户上传）
     * @return 透明 PNG 字节数组
     * @throws Exception 调用失败或处理异常
     */
    public byte[] removeBackground(MultipartFile file) throws Exception {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("抠图功能未启用");
        }

        String urlStr = properties.getServiceUrl() + "/api/remove-background";
        log.info("Calling bg-remover service: {}", urlStr);

        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout((int) properties.getTimeoutMs());
        conn.setReadTimeout((int) properties.getTimeoutMs());
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        // 构建 multipart body
        byte[] body = buildMultipartBody(file);
        conn.setRequestProperty("Content-Length", String.valueOf(body.length));

        // 发送
        try (var os = conn.getOutputStream()) {
            os.write(body);
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        log.info("bg-remover response code: {}", responseCode);

        if (responseCode != 200) {
            String errorBody = readStream(conn.getErrorStream());
            throw new RuntimeException("抠图服务返回错误 [" + responseCode + "]: " + errorBody);
        }

        // 读取返回的 PNG 字节
        byte[] result;
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            is.transferTo(bos);
            result = bos.toByteArray();
        }

        if (result.length == 0) {
            throw new RuntimeException("抠图服务返回空结果");
        }

        log.info("Background removal success: {} -> {} bytes", file.getOriginalFilename(), result.length);
        return result;
    }

    /**
     * 构建 multipart/form-data 请求体
     */
    private byte[] buildMultipartBody(MultipartFile file) throws Exception {
        var bos = new ByteArrayOutputStream();
        String lineEnd = "\r\n";

        // 字段头
        bos.write(("--" + BOUNDARY + lineEnd).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"image\"; filename=\"" +
                sanitizeFilename(file.getOriginalFilename()) + "\"" + lineEnd).getBytes());
        bos.write(("Content-Type: " + file.getContentType()).getBytes());
        bos.write(lineEnd.getBytes());
        bos.write(lineEnd.getBytes());

        // 文件数据
        bos.write(file.getBytes());
        bos.write(lineEnd.getBytes());

        // 结束 boundary
        bos.write(("--" + BOUNDARY + "--" + lineEnd).getBytes());

        return bos.toByteArray();
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) return "image.png";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        return new String(stream.readAllBytes());
    }
}
