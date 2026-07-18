package com.gamecard.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 图片代理服务
 * <p>
 * 用于代理加载 AI API 返回的图片 URL，解决浏览器无法添加认证头的问题。
 * 将图片通过后端代理转为 data URI 返回给前端。
 * </p>
 *
 * @author GameCard Team
 */
@Service
public class ImageProxyService {

    private static final Logger log = LoggerFactory.getLogger(ImageProxyService.class);

    private final HttpClient httpClient;

    public ImageProxyService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 下载图片并转为 data URI
     *
     * @param imageUrl 图片 URL
     * @param apiKey   可选的 API Key（某些 CDN 需要认证）
     * @return data URI
     * @throws Exception 下载失败
     */
    public String downloadAsDataUri(String imageUrl, String apiKey) throws Exception {
        log.info("Proxying image: {}", imageUrl);

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "GameCard/1.0")
                .GET();

        // 如果图片 URL 和 AI API 同域，带上认证头
        if (apiKey != null && !apiKey.isBlank()) {
            reqBuilder.header("Authorization", "Bearer " + apiKey);
        }

        HttpResponse<byte[]> resp = httpClient.send(
                reqBuilder.build(),
                HttpResponse.BodyHandlers.ofByteArray());

        if (resp.statusCode() != 200) {
            throw new RuntimeException("图片代理下载失败 [HTTP " + resp.statusCode() + "]: " + imageUrl);
        }

        byte[] imageBytes = resp.body();
        if (imageBytes.length < 20) {
            throw new RuntimeException("图片数据为空");
        }

        // 检测 MIME
        String mime = "image/png";
        String contentType = resp.headers().firstValue("Content-Type").orElse("");
        if (contentType.contains("png")) mime = "image/png";
        else if (contentType.contains("jpeg") || contentType.contains("jpg")) mime = "image/jpeg";
        else if (contentType.contains("webp")) mime = "image/webp";
        else if (contentType.contains("gif")) mime = "image/gif";

        String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        String dataUri = "data:" + mime + ";base64," + base64;

        log.info("Image proxied: {} bytes → {} data URI", imageBytes.length, mime);
        return dataUri;
    }
}
